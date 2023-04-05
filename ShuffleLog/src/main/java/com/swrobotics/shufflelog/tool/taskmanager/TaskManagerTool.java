package com.swrobotics.shufflelog.tool.taskmanager;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.shufflelog.ShuffleLog;
import com.swrobotics.shufflelog.tool.Tool;
import com.swrobotics.shufflelog.tool.ToolConstants;
import com.swrobotics.shufflelog.tool.taskmanager.file.RemoteDirectory;
import com.swrobotics.shufflelog.tool.taskmanager.file.RemoteFileView;
import com.swrobotics.shufflelog.util.Cooldown;

import imgui.ImGui;
import imgui.flag.ImGuiTableFlags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TaskManagerTool implements Tool {
    // Tasks API
    public static final String MSG_LIST_TASKS = ":ListTasks";
    public static final String MSG_CREATE_TASK = ":CreateTask";
    public static final String MSG_DELETE_TASK = ":DeleteTask";
    public static final String MSG_TASKS = ":Tasks";

    // Logging
    public static final String MSG_STDOUT_PREFIX = ":StdOut:";
    public static final String MSG_STDERR_PREFIX = ":StdErr:";

    private final ShuffleLog log;
    private final MessengerClient msg;
    private final String name;

    private final Cooldown reqTasksCooldown;
    private final List<Task> tasks;
    private boolean receivedTasks;

    private final RemoteFileView remoteFiles;
    private final Map<String, TaskLogTool> logTools;

    public TaskManagerTool(ShuffleLog log, String name) {
        this.log = log;
        this.msg = log.getMessenger();
        this.name = name;

        reqTasksCooldown = new Cooldown(ToolConstants.MSG_QUERY_COOLDOWN_TIME);
        tasks = new ArrayList<>();

        msg.addHandler(name + MSG_TASKS, this::onTasks);
        msg.addHandler(name + MSG_STDOUT_PREFIX + "*", this::onStdOut);
        msg.addHandler(name + MSG_STDERR_PREFIX + "*", this::onStdErr);

        remoteFiles = new RemoteFileView(log, name);

        logTools = new HashMap<>();
        receivedTasks = false;

        msg.addDisconnectHandler(
                () -> {
                    receivedTasks = false;
                    tasks.clear();
                });
    }

    private TaskLogTool getLog(String task) {
        return logTools.computeIfAbsent(task, (n) -> new TaskLogTool(n, log));
    }

    private void onStdOut(String type, MessageReader reader) {
        getLog(type.substring(name.length() + MSG_STDOUT_PREFIX.length()))
                .addEntry(false, reader.readString());
    }

    private void onStdErr(String type, MessageReader reader) {
        getLog(type.substring(name.length() + MSG_STDERR_PREFIX.length()))
                .addEntry(true, reader.readString());
    }

    private void showTasks() {
        Task deletion = null;
        for (Task task : tasks) {
            String name = task.name.get();
            ImGui.pushID(task.uuid);

            boolean open = ImGui.treeNode(task.uuid, name);
            if (ImGui.beginPopupContextItem("task_ctx")) {
                if (ImGui.selectable("Open Log")) {
                    TaskLogTool tool = getLog(name);
                    if (!tool.isOpen()) {
                        tool.setOpen();
                        log.addTool(tool);
                    }
                }
                if (ImGui.selectable("Delete")) {
                    msg.prepare(this.name + MSG_DELETE_TASK).addString(name).send();
                    deletion = task;
                    ImGui.closeCurrentPopup();
                }
                ImGui.endPopup();
            }
            if (task.edited) {
                ImGui.sameLine();
                ImGui.textDisabled("- Edited");
            }

            if (open) {
                ImGui.indent();
                if (ImGui.beginTable("params_layout", 2, ImGuiTableFlags.SizingStretchProp)) {
                    ImGui.tableNextColumn();
                    ImGui.text("Name:");
                    ImGui.tableNextColumn();
                    ImGui.setNextItemWidth(-1);
                    task.nameEdited |= ImGui.inputText("##task_name", task.name);
                    task.edited |= task.nameEdited;

                    ImGui.tableNextColumn();
                    ImGui.text("Working Dir:");
                    ImGui.tableNextColumn();
                    ImGui.setNextItemWidth(-1);
                    task.edited |= ImGui.inputText("##task_workingDir", task.workingDirectory);
                    if (ImGui.beginDragDropTarget()) {
                        RemoteDirectory payload =
                                ImGui.acceptDragDropPayload("TM_" + this.name + "_DRAG_DIR");
                        if (payload != null) {
                            task.workingDirectory.set(payload.getFullPath());
                            task.edited = true;
                        }
                        ImGui.endDragDropTarget();
                    }

                    ImGui.tableNextColumn();
                    ImGui.text("Command:");
                    ImGui.tableNextColumn();
                    ImGui.setNextItemWidth(-1);
                    task.edited |= ImGui.inputText("##task_cmd", task.command);

                    ImGui.tableNextColumn();
                    ImGui.text("Enabled:");
                    ImGui.tableNextColumn();
                    task.edited |= ImGui.checkbox("##task_enabled", task.enabled);

                    ImGui.endTable();
                }

                ImGui.beginDisabled(!task.edited);
                if (ImGui.button("Save")) {
                    if (task.nameEdited) {
                        msg.prepare(this.name + MSG_DELETE_TASK).addString(task.syncedName).send();
                    }

                    MessageBuilder builder = msg.prepare(this.name + MSG_CREATE_TASK);
                    builder.addString(task.name.get());
                    builder.addString(task.workingDirectory.get());
                    String[] cmdSplit = task.command.get().split(" ");
                    builder.addInt(cmdSplit.length);
                    for (String str : cmdSplit) builder.addString(str);
                    builder.addBoolean(task.enabled.get());
                    builder.send();

                    task.markSynced();
                }
                ImGui.endDisabled();

                ImGui.unindent();
                ImGui.treePop();
            }

            ImGui.popID();
        }
        if (deletion != null) tasks.remove(deletion);

        ImGui.spacing();

        if (ImGui.button("Add new task")) {
            String name = "New Task";
            int i = 0;
            while (taskExists(name)) {
                i++;
                name = "New Task (" + i + ")";
            }

            Task task = new Task(name, "", "", false);
            task.edited = true;
            tasks.add(task);
        }
    }

    private boolean taskExists(String name) {
        for (Task task : tasks) {
            if (task.name.get().equals(name)) return true;
        }
        return false;
    }

    private void onTasks(String type, MessageReader reader) {
        int count = reader.readInt();
        tasks.clear();
        for (int i = 0; i < count; i++) {
            String name = reader.readString();
            String workDir = reader.readString();
            int cmdLen = reader.readInt();
            StringBuilder cmdBuilder = new StringBuilder();
            boolean space = false;
            for (int j = 0; j < cmdLen; j++) {
                if (space) cmdBuilder.append(" ");
                else space = true;
                cmdBuilder.append(reader.readString());
            }
            boolean enabled = reader.readBoolean();

            Task task = new Task(name, workDir, cmdBuilder.toString(), enabled);
            tasks.add(task);
        }
        receivedTasks = true;
    }

    @Override
    public void process() {
        if (ImGui.begin("Task Manager [" + name + "]")) {
            if (!msg.isConnected()) {
                ImGui.textDisabled("Not connected");
                ImGui.end();
                return;
            }

            if (!receivedTasks && reqTasksCooldown.request()) {
                msg.send(name + MSG_LIST_TASKS);
            }

            if (ImGui.beginTable("tm_layout", 2, ImGuiTableFlags.BordersInner)) {
                ImGui.tableNextColumn();
                ImGui.tableHeader("Tasks:");
                ImGui.tableNextColumn();
                ImGui.tableHeader("Files:");

                ImGui.tableNextColumn();
                showTasks();
                ImGui.tableNextColumn();
                remoteFiles.process();
                ImGui.endTable();
            }
        }
        ImGui.end();
    }
}

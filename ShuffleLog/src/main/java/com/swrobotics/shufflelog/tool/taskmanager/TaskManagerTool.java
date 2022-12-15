package com.swrobotics.shufflelog.tool.taskmanager;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.shufflelog.ShuffleLog;
import com.swrobotics.shufflelog.tool.Tool;
import com.swrobotics.shufflelog.tool.ToolConstants;
import com.swrobotics.shufflelog.util.Cooldown;
import com.swrobotics.shufflelog.util.FileChooser;
import imgui.ImGuiViewport;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiTableFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static imgui.ImGui.*;

public final class TaskManagerTool implements Tool {
    // Filesystem API
    public static final String MSG_LIST_FILES     = ":ListFiles";
    public static final String MSG_READ_FILE      = ":ReadFile";
    public static final String MSG_WRITE_FILE     = ":WriteFile";
    public static final String MSG_DELETE_FILE    = ":DeleteFile";
    public static final String MSG_MOVE_FILE      = ":MoveFile";
    public static final String MSG_MKDIR          = ":Mkdir";
    public static final String MSG_FILES          = ":Files";
    public static final String MSG_FILE_CONTENT   = ":FileContent";
    public static final String MSG_WRITE_CONFIRM  = ":WriteConfirm";
    public static final String MSG_DELETE_CONFIRM = ":DeleteConfirm";
    public static final String MSG_MOVE_CONFIRM   = ":MoveConfirm";
    public static final String MSG_MKDIR_CONFIRM  = ":MkdirConfirm";

    // Tasks API
    public static final String MSG_LIST_TASKS  = ":ListTasks";
    public static final String MSG_CREATE_TASK = ":CreateTask";
    public static final String MSG_DELETE_TASK = ":DeleteTask";
    public static final String MSG_TASKS       = ":Tasks";

    // Logging
    public static final String MSG_STDOUT_PREFIX = ":StdOut:";
    public static final String MSG_STDERR_PREFIX = ":StdErr:";

    private final ShuffleLog log;
    private final MessengerClient msg;
    private final String name;

    private final Cooldown reqContentCooldown;
    private final RemoteDirectory remoteRoot;
    private final ImString mkdirName;

    private final Cooldown reqTasksCooldown;
    private final List<Task> tasks;
    private boolean receivedTasks;

    private final Map<String, TaskLogTool> logTools;

    public TaskManagerTool(ShuffleLog log, String name) {
        this.log = log;
        this.msg = log.getMessenger();
        this.name = name;

        reqContentCooldown = new Cooldown(ToolConstants.MSG_QUERY_COOLDOWN_TIME);
        remoteRoot = new RemoteDirectory("");

        reqTasksCooldown = new Cooldown(ToolConstants.MSG_QUERY_COOLDOWN_TIME);
        tasks = new ArrayList<>();

        msg.addHandler(name + MSG_FILES, this::onFiles);
        msg.addHandler(name + MSG_DELETE_CONFIRM, this::onDeleteConfirm);
        msg.addHandler(name + MSG_MKDIR_CONFIRM, this::onMkdirConfirm);
        msg.addHandler(name + MSG_WRITE_CONFIRM, this::onWriteConfirm);
        msg.addHandler(name + MSG_MOVE_CONFIRM, this::onMoveConfirm);
        msg.addHandler(name + MSG_TASKS, this::onTasks);
        msg.addHandler(name + MSG_STDOUT_PREFIX + "*", this::onStdOut);
        msg.addHandler(name + MSG_STDERR_PREFIX + "*", this::onStdErr);
        msg.addHandler(name + MSG_FILE_CONTENT, this::onFileContent);

        mkdirName = new ImString(64);
        logTools = new HashMap<>();

        receivedTasks = false;
    }

    private RemoteNode evalPath(String path) {
        if (path.equals(""))
            return remoteRoot;

        String[] parts = path.split("/");
        RemoteNode node = remoteRoot;
        for (String part : parts) {
            node = ((RemoteDirectory) node).getChild(part);
        }
        return node;
    }

    private byte[] readFile(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) > 0) {
            b.write(buffer, 0, read);
        }
        in.close();
        b.close();
        return b.toByteArray();
    }

    private void uploadFile(File file, String targetDirPath) {
        String path = targetDirPath.equals("") ? file.getName() : targetDirPath + "/" + file.getName();
        if (file.isFile()) {
            try {
                byte[] data = readFile(file);
                msg.prepare(name + MSG_WRITE_FILE)
                        .addString(path)
                        .addInt(data.length)
                        .addRaw(data)
                        .send();
            } catch (IOException e) {
                System.out.println("Failed to read file to upload: " + file);
                e.printStackTrace();
            }
        } else if (file.isDirectory()) {
            msg.prepare(name + MSG_MKDIR)
                    .addString(path)
                    .send();

            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    uploadFile(child, path);
                }
            }
        }
    }

    private boolean isChild(RemoteNode parent, RemoteNode child) {
        return child.getFullPath().startsWith(parent.getFullPath());
    }

    private void showDirectory(RemoteDirectory dir, boolean isRoot) {
        String dirName = isRoot ? "Tasks Root" : dir.getName();

        boolean open = treeNodeEx(dirName, ImGuiTreeNodeFlags.SpanFullWidth | (isRoot ? ImGuiTreeNodeFlags.DefaultOpen : 0));
        if (beginDragDropSource()) {
            text(dirName);
            setDragDropPayload("TM_" + name + "_DRAG_DIR", dir);
            endDragDropSource();
        }
        if (beginDragDropTarget()) {
            RemoteNode node = acceptDragDropPayload("TM_" + name + "_DRAG_FILE");
            if (node == null)
                node = acceptDragDropPayload("TM_" + name + "_DRAG_DIR");

            if (node != null && !isChild(node, dir)) {
                String dstPath = dir.getFullPath();
                if (!dstPath.equals(""))
                    dstPath += "/";

                msg.prepare(name + MSG_MOVE_FILE)
                        .addString(node.getFullPath())
                        .addString(dstPath + node.getName())
                        .send();
            }

            endDragDropTarget();
        }

        pushID(dir.getName());

        boolean openNewDirPopup = false;
        if (beginPopupContextItem("context_menu")) {
            if (!isRoot && selectable("Delete")) {
                msg.prepare(name + MSG_DELETE_FILE)
                        .addString(dir.getFullPath())
                        .send();
                closeCurrentPopup();
            }
            if (selectable("New directory")) {
                closeCurrentPopup();
                openNewDirPopup = true;
            }
            if (selectable("Upload file(s)")) {
                closeCurrentPopup();
                FileChooser.chooseFileOrFolder((file) -> uploadFile(file, dir.getFullPath()));
            }
            endPopup();
        }

        if (openNewDirPopup) {
            mkdirName.set("");
            openPopup("New Directory");
        }

        if (beginPopupModal("New Directory", ImGuiWindowFlags.NoMove)) {
            // Center the popup
            ImGuiViewport vp = getWindowViewport();
            setWindowPos(vp.getCenterX() - getWindowWidth() / 2, vp.getCenterY() - getWindowHeight() / 2);

            text("New directory name:");
            setNextItemWidth(300);
            boolean submit = inputText("##name", mkdirName, ImGuiInputTextFlags.EnterReturnsTrue);
            setItemDefaultFocus();

            setNextItemWidth(300);
            submit |= button("Create");

            if (submit) {
                String path = isRoot ? mkdirName.get() : (dir.getFullPath() + "/" + mkdirName.get());
                msg.prepare(name + MSG_MKDIR)
                        .addString(path)
                        .send();
                closeCurrentPopup();
            }

            endPopup();
        }

        popID();

        if (open) {
            if (dir.needsRefreshContent()) {
                indent(getTreeNodeToLabelSpacing());
                textDisabled("Fetching...");
                unindent(getTreeNodeToLabelSpacing());

                if (reqContentCooldown.request()) {
                    msg.prepare(name + MSG_LIST_FILES)
                            .addString(dir.getFullPath())
                            .send();
                }
            } else {
                for (RemoteNode node : dir.getChildren()) {
                    showNode(node);
                }
            }
            treePop();
        }
    }

    private void showFile(RemoteFile file) {
        treeNodeEx(file.getName(), ImGuiTreeNodeFlags.NoTreePushOnOpen | ImGuiTreeNodeFlags.Leaf);
        pushID(file.getName());
        if (beginPopupContextItem()) {
            if (selectable("Delete")) {
                msg.prepare(name + MSG_DELETE_FILE)
                        .addString(file.getFullPath())
                        .send();
                closeCurrentPopup();
            }
            if (selectable("Edit")) {
                msg.prepare(name + MSG_READ_FILE)
                        .addString(file.getFullPath())
                        .send();
            }
            endPopup();
        }
        if (beginDragDropSource()) {
            text(file.getName());
            setDragDropPayload("TM_" + this.name + "_DRAG_FILE", file);
            endDragDropSource();
        }
        popID();
    }

    private void showNode(RemoteNode node) {
        if (node instanceof RemoteDirectory) {
            showDirectory((RemoteDirectory) node, false);
        } else {
            showFile((RemoteFile) node);
        }
    }

    private void onFiles(String type, MessageReader reader) {
        String path = reader.readString();
        boolean success = reader.readBoolean();
        if (!success) {
            System.err.println("File query failed on " + path);
            return;
        }

        RemoteDirectory dir = (RemoteDirectory) evalPath(path);
        dir.setNeedsRefreshContent(false);
        dir.clearChildren();
        int count = reader.readInt();
        for (int i = 0; i < count; i++) {
            String name = reader.readString();
            boolean isDir = reader.readBoolean();

            RemoteNode node;
            if (isDir)
                node = new RemoteDirectory(name);
            else
                node = new RemoteFile(name);

            dir.addChild(node);
        }
    }

    private void onDeleteConfirm(String type, MessageReader reader) {
        String path = reader.readString();
        boolean success = reader.readBoolean();
        if (!success) {
            System.err.println("Delete failed on " + path);
            return;
        }

        RemoteNode node = evalPath(path);
        node.remove();
    }

    private void onMkdirConfirm(String type, MessageReader reader) {
        String path = reader.readString();
        boolean success = reader.readBoolean();
        if (!success) {
            System.err.println("Mkdir failed on " + path);
            return;
        }

        String[] entries = path.split("/");
        RemoteDirectory dir = remoteRoot;

        for (String entry : entries) {
            RemoteNode node = dir.getChild(entry);
            if (node == null) {
                RemoteDirectory newDir = new RemoteDirectory(entry);
                dir.addChild(newDir);
                dir = newDir;
            } else {
                dir = (RemoteDirectory) node;
            }
        }
    }

    private void onMoveConfirm(String type, MessageReader reader) {
        String srcPath = reader.readString();
        String dstPath = reader.readString();
        boolean success = reader.readBoolean();
        if (!success) {
            System.err.println("Move failed from " + srcPath + " to " + dstPath);
            return;
        }

        RemoteNode srcNode = evalPath(srcPath);
        srcNode.remove();

        createLocalFile(dstPath, srcNode instanceof RemoteDirectory);
    }

    private void createLocalFile(String path, boolean directory) {
        String[] entries = path.split("/");

        // Ensure all directories are present locally
        RemoteDirectory dir = remoteRoot;
        for (int i = 0; i < entries.length - (directory ? 0 : 1); i++) {
            String entry = entries[i];
            RemoteNode node = dir.getChild(entry);
            if (node == null) {
                RemoteDirectory newDir = new RemoteDirectory(entry);
                dir.addChild(newDir);
                dir = newDir;
            } else {
                dir = (RemoteDirectory) node;
            }
        }

        if (!directory) {
            // Create the file locally
            String fileName = entries[entries.length - 1];
            if (dir.getChild(fileName) == null) {
                RemoteFile file = new RemoteFile(fileName);
                dir.addChild(file);
            }
        }
    }

    private void onWriteConfirm(String type, MessageReader reader) {
        String path = reader.readString();
        boolean success = reader.readBoolean();
        if (!success) {
            System.err.println("Write failed on " + path);
            return;
        }

        createLocalFile(path, false);
    }

    private TaskLogTool getLog(String task) {
        return logTools.computeIfAbsent(task, (n) -> new TaskLogTool(n, log));
    }

    private void onStdOut(String type, MessageReader reader) {
        getLog(type.substring(name.length() + MSG_STDOUT_PREFIX.length())).addEntry(false, reader.readString());
    }

    private void onStdErr(String type, MessageReader reader) {
        getLog(type.substring(name.length() + MSG_STDERR_PREFIX.length())).addEntry(true, reader.readString());
    }

    private void onFileContent(String type, MessageReader reader) {
        String filePath = reader.readString();
        boolean success = reader.readBoolean();
        if (!success)
            return;

        int dataLen = reader.readInt();
        byte[] data = reader.readRaw(dataLen);
        String dataStr = new String(data, StandardCharsets.UTF_8);

        FileEditorTool editor = new FileEditorTool(filePath, dataStr, msg, name + MSG_WRITE_FILE, log);
        log.addTool(editor);
    }

    private void showTasks() {
        Task deletion = null;
        for (Task task : tasks) {
            String name = task.name.get();
            pushID(task.uuid);

            boolean open = treeNode(task.uuid, name);
            if (beginPopupContextItem("task_ctx")) {
                if (selectable("Open Log")) {
                    TaskLogTool tool = getLog(name);
                    if (!tool.isOpen()) {
                        tool.setOpen();
                        log.addTool(tool);
                    }
                }
                if (selectable("Delete")) {
                    msg.prepare(this.name + MSG_DELETE_TASK)
                            .addString(name)
                            .send();
                    deletion = task;
                    closeCurrentPopup();
                }
                endPopup();
            }
            if (task.edited) {
                sameLine();
                textDisabled("- Edited");
            }

            if (open) {
                indent();
                if (beginTable("params_layout", 2, ImGuiTableFlags.SizingStretchProp)) {
                    tableNextColumn();
                    text("Name:");
                    tableNextColumn();
                    setNextItemWidth(-1);
                    task.nameEdited |= inputText("##task_name", task.name);
                    task.edited |= task.nameEdited;

                    tableNextColumn();
                    text("Working Dir:");
                    tableNextColumn();
                    setNextItemWidth(-1);
                    task.edited |= inputText("##task_workingDir", task.workingDirectory);
                    if (beginDragDropTarget()) {
                        RemoteDirectory payload = acceptDragDropPayload("TM_" + this.name + "_DRAG_DIR");
                        if (payload != null) {
                            task.workingDirectory.set(payload.getFullPath());
                            task.edited = true;
                        }
                        endDragDropTarget();
                    }

                    tableNextColumn();
                    text("Command:");
                    tableNextColumn();
                    setNextItemWidth(-1);
                    task.edited |= inputText("##task_cmd", task.command);

                    tableNextColumn();
                    text("Enabled:");
                    tableNextColumn();
                    task.edited |= checkbox("##task_enabled", task.enabled);

                    endTable();
                }

                beginDisabled(!task.edited);
                if (button("Save")) {
                    if (task.nameEdited) {
                        msg.prepare(this.name + MSG_DELETE_TASK)
                                .addString(task.syncedName)
                                .send();
                    }

                    MessageBuilder builder = msg.prepare(this.name + MSG_CREATE_TASK);
                    builder.addString(task.name.get());
                    builder.addString(task.workingDirectory.get());
                    String[] cmdSplit = task.command.get().split(" ");
                    builder.addInt(cmdSplit.length);
                    for (String str : cmdSplit)
                        builder.addString(str);
                    builder.addBoolean(task.enabled.get());
                    builder.send();

                    task.markSynced();
                }
                endDisabled();

                unindent();
                treePop();
            }

            popID();
        }
        if (deletion != null)
            tasks.remove(deletion);

        spacing();

        if (button("Add new task")) {
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
            if (task.name.get().equals(name))
                return true;
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
        if (begin("Task Manager [" + name + "]")) {
            if (!receivedTasks && reqTasksCooldown.request()) {
                msg.send(name + MSG_LIST_TASKS);
            }

            if (beginTable("tm_layout", 2, ImGuiTableFlags.BordersInner)) {
                tableNextColumn();
                tableHeader("Tasks:");
                tableNextColumn();
                tableHeader("Files:");

                tableNextColumn();
                showTasks();
                tableNextColumn();
                showDirectory(remoteRoot, true);
                endTable();
            }
        }
        end();
    }
}

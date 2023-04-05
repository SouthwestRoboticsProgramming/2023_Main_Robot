package com.swrobotics.taskmanager;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.taskmanager.filesystem.FileSystemAPI;

import java.io.File;
import java.util.Map;

public final class TaskManagerAPI {
    // Tasks API
    private static final String MSG_LIST_TASKS = ":ListTasks";
    private static final String MSG_CREATE_TASK = ":CreateTask";
    private static final String MSG_DELETE_TASK = ":DeleteTask";
    private static final String MSG_TASKS = ":Tasks";

    // Logging
    private static final String MSG_STDOUT = ":StdOut:";
    private static final String MSG_STDERR = ":StdErr:";

    private final TaskManager mgr;
    private final TaskManagerConfiguration config;
    private final MessengerClient msg;

    private final String msgTasks;
    private final String msgStdOut;
    private final String msgStdErr;

    private final File tasksRoot;

    public TaskManagerAPI(TaskManager mgr, TaskManagerConfiguration config) {
        this.mgr = mgr;
        this.config = config;

        System.out.println(
                "Connecting to Messenger at "
                        + config.getMessengerHost()
                        + ":"
                        + config.getMessengerPort()
                        + " as "
                        + config.getMessengerName());
        msg =
                new MessengerClient(
                        config.getMessengerHost(),
                        config.getMessengerPort(),
                        config.getMessengerName());

        String prefix = config.getMessengerName();
        new FileSystemAPI(msg, prefix, config.getTasksRoot());

        String msgListTasks = prefix + MSG_LIST_TASKS;
        String msgCreateTask = prefix + MSG_CREATE_TASK;
        String msgDeleteTask = prefix + MSG_DELETE_TASK;

        msgTasks = prefix + MSG_TASKS;
        msgStdOut = prefix + MSG_STDOUT;
        msgStdErr = prefix + MSG_STDERR;

        tasksRoot = config.getTasksRoot();
        if (!tasksRoot.exists()) tasksRoot.mkdirs();

        msg.addHandler(msgListTasks, this::onListTasks);
        msg.addHandler(msgCreateTask, this::onCreateTask);
        msg.addHandler(msgDeleteTask, this::onDeleteTask);
    }

    private String removeTrailingSeparator(String path) {
        if (path.endsWith(File.separator)) return path.substring(0, path.length() - 1);
        return path;
    }

    private String getTaskPath(File file) {
        String rootAbsolute = removeTrailingSeparator(tasksRoot.getAbsolutePath());
        String fileAbsolute = removeTrailingSeparator(file.getAbsolutePath());

        if (!fileAbsolute.startsWith(rootAbsolute))
            throw new AssertionError("File is not a task tile: " + file);

        return fileAbsolute.substring(rootAbsolute.length());
    }

    private void onListTasks(String type, MessageReader reader) {
        MessageBuilder out = msg.prepare(msgTasks);
        Map<String, Task> tasks = mgr.getTasks();

        out.addInt(tasks.size());
        for (Map.Entry<String, Task> entry : tasks.entrySet()) {
            out.addString(entry.getKey());

            Task task = entry.getValue();
            out.addString(getTaskPath(task.getWorkingDirectory()));
            String[] command = task.getCommand();
            out.addInt(command.length);
            for (String token : command) out.addString(token);
            out.addBoolean(task.isEnabled());
        }

        out.send();
    }

    // Can also be used to modify a task by overwriting an existing one
    private void onCreateTask(String type, MessageReader reader) {
        String name = reader.readString();
        String workingDirPath = reader.readString();
        File workingDir = new File(tasksRoot, workingDirPath);
        int commandSize = reader.readInt();
        String[] command = new String[commandSize];
        for (int i = 0; i < commandSize; i++) {
            command[i] = reader.readString();
        }
        boolean enabled = reader.readBoolean();
        Task task = new Task(workingDir, command, enabled, this, config.getMaxFailCount(), name);

        // Remove old task
        if (mgr.getTask(name) != null) mgr.removeTask(name);

        mgr.addTask(task);
    }

    private void onDeleteTask(String type, MessageReader reader) {
        mgr.removeTask(reader.readString());
    }

    public void broadcastTaskOutput(Task task, LogOutputType type, String line) {
        msg.prepare((type == LogOutputType.STDOUT ? msgStdOut : msgStdErr) + task.getName())
                .addString(line)
                .send();
    }

    public void read() {
        msg.readMessages();
    }
}

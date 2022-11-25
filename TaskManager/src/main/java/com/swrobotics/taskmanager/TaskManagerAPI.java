package com.swrobotics.taskmanager;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.messenger.client.MessengerClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public final class TaskManagerAPI {
    // Filesystem API
    private static final String MSG_LIST_FILES     = ":ListFiles";
    private static final String MSG_READ_FILE      = ":ReadFile";
    private static final String MSG_WRITE_FILE     = ":WriteFile";
    private static final String MSG_DELETE_FILE    = ":DeleteFile";
    private static final String MSG_MOVE_FILE      = ":MoveFile";
    private static final String MSG_MKDIR          = ":Mkdir";
    private static final String MSG_FILES          = ":Files";
    private static final String MSG_FILE_CONTENT   = ":FileContent";
    private static final String MSG_WRITE_CONFIRM  = ":WriteConfirm";
    private static final String MSG_DELETE_CONFIRM = ":DeleteConfirm";
    private static final String MSG_MOVE_CONFIRM   = ":MoveConfirm";
    private static final String MSG_MKDIR_CONFIRM  = ":MkdirConfirm";

    // Tasks API
    private static final String MSG_LIST_TASKS  = ":ListTasks";
    private static final String MSG_CREATE_TASK = ":CreateTask";
    private static final String MSG_DELETE_TASK = ":DeleteTask";
    private static final String MSG_TASKS       = ":Tasks";

    // Logging
    private static final String MSG_STDOUT = ":StdOut:";
    private static final String MSG_STDERR = ":StdErr:";

    private final TaskManager mgr;
    private final TaskManagerConfiguration config;
    private final MessengerClient msg;

    private final String msgFiles;
    private final String msgFileContent;
    private final String msgWriteConfirm;
    private final String msgDeleteConfirm;
    private final String msgMoveConfirm;
    private final String msgMkdirConfirm;
    private final String msgTasks;
    private final String msgStdOut;
    private final String msgStdErr;

    private final File tasksRoot;

    public TaskManagerAPI(TaskManager mgr, TaskManagerConfiguration config) {
        this.mgr = mgr;
        this.config = config;

        System.out.println("Connecting to Messenger at " + config.getMessengerHost() + ":" + config.getMessengerPort() + " as " + config.getMessengerName());
        msg = new MessengerClient(
                config.getMessengerHost(),
                config.getMessengerPort(),
                config.getMessengerName()
        );

        String prefix = config.getMessengerName();
        String msgListFiles  = prefix + MSG_LIST_FILES;
        String msgReadFile   = prefix + MSG_READ_FILE;
        String msgWriteFile  = prefix + MSG_WRITE_FILE;
        String msgDeleteFile = prefix + MSG_DELETE_FILE;
        String msgMoveFile   = prefix + MSG_MOVE_FILE;
        String msgMkdir      = prefix + MSG_MKDIR;
        String msgListTasks  = prefix + MSG_LIST_TASKS;
        String msgCreateTask = prefix + MSG_CREATE_TASK;
        String msgDeleteTask = prefix + MSG_DELETE_TASK;

        msgFiles         = prefix + MSG_FILES;
        msgFileContent   = prefix + MSG_FILE_CONTENT;
        msgWriteConfirm  = prefix + MSG_WRITE_CONFIRM;
        msgDeleteConfirm = prefix + MSG_DELETE_CONFIRM;
        msgMoveConfirm   = prefix + MSG_MOVE_CONFIRM;
        msgMkdirConfirm  = prefix + MSG_MKDIR_CONFIRM;
        msgTasks         = prefix + MSG_TASKS;
        msgStdOut        = prefix + MSG_STDOUT;
        msgStdErr        = prefix + MSG_STDERR;

        tasksRoot = config.getTasksRoot();

        msg.addHandler(msgListFiles,  this::onListFiles);
        msg.addHandler(msgReadFile,   this::onReadFile);
        msg.addHandler(msgWriteFile,  this::onWriteFile);
        msg.addHandler(msgMoveFile,   this::onMoveFile);
        msg.addHandler(msgDeleteFile, this::onDeleteFile);
        msg.addHandler(msgMkdir,      this::onMkdir);
        msg.addHandler(msgListTasks,  this::onListTasks);
        msg.addHandler(msgCreateTask, this::onCreateTask);
        msg.addHandler(msgDeleteTask, this::onDeleteTask);
    }

    private String localizePath(String path) {
        return path.replace('/', File.separatorChar);
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

    private boolean deleteFile(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (!Files.isSymbolicLink(f.toPath())) {
                    if (!deleteFile(f))
                        return false;
                }
            }
        }
        return file.delete();
    }

    private boolean moveFile(File src, File dst) {
        return src.renameTo(dst);
    }

    private String removeTrailingSeparator(String path) {
        if (path.endsWith(File.separator))
            return path.substring(0, path.length() - 1);
        return path;
    }

    private String getTaskPath(File file) {
        String rootAbsolute = removeTrailingSeparator(tasksRoot.getAbsolutePath());
        String fileAbsolute = removeTrailingSeparator(file.getAbsolutePath());

        if (!fileAbsolute.startsWith(rootAbsolute))
            throw new AssertionError("File is not a task tile: " + file);

        return fileAbsolute.substring(rootAbsolute.length());
    }

    private void onListFiles(String type, MessageReader reader) {
        MessageBuilder out = msg.prepare(msgFiles);

        String dirPath = reader.readString();
        File dir = new File(tasksRoot, localizePath(dirPath));
        out.addString(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            out.addBoolean(false);
            out.send();
            return;
        }

        File[] children = dir.listFiles();
        out.addBoolean(true);
        if (children == null) {
            out.addInt(0);
        } else {
            out.addInt(children.length);
            for (File child : children) {
                out.addString(child.getName());
                out.addBoolean(child.isDirectory());
            }
        }

        out.send();
    }

    private void onReadFile(String type, MessageReader reader) {
        MessageBuilder out = msg.prepare(msgFileContent);

        String path = reader.readString();
        File file = new File(tasksRoot, localizePath(path));
        out.addString(path);
        if (!file.exists() || !file.isFile()) {
            out.addBoolean(false);
            out.send();
            return;
        }

        System.out.println("Sending contents of " + path);
        try {
            byte[] fileContent = readFile(file);
            out.addBoolean(true);
            out.addInt(fileContent.length);
            out.addRaw(fileContent);
        } catch (IOException e) {
            out.addBoolean(false);
            System.err.println("Reading file content failed for " + path);
            e.printStackTrace();
        }

        out.send();
    }

    private void onWriteFile(String type, MessageReader reader) {
        String path = reader.readString();
        File file = new File(tasksRoot, localizePath(path));
        if (file.exists() && !file.isFile()) {
            msg.prepare(msgWriteConfirm)
                    .addString(path)
                    .addBoolean(false)
                    .send();
            return;
        }

        int dataLen = reader.readInt();
        byte[] data = reader.readRaw(dataLen);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            System.out.println("Receiving file data for " + path);
            fos.write(data);
            fos.flush();

            msg.prepare(msgWriteConfirm)
                    .addString(path)
                    .addBoolean(true)
                    .send();
        } catch (IOException e) {
            System.err.println("File write failed for " + path);
            e.printStackTrace();

            msg.prepare(msgWriteConfirm)
                    .addString(path)
                    .addBoolean(false)
                    .send();
        }
    }

    private void onDeleteFile(String type, MessageReader reader) {
        String path = reader.readString();
        File file = new File(tasksRoot, localizePath(path));
        if (!file.exists()) {
            msg.prepare(msgDeleteConfirm)
                    .addString(path)
                    .addBoolean(false)
                    .send();
            return;
        }

        System.out.println("Deleting " + path);
        boolean result = deleteFile(file);
        if (!result)
            System.err.println("File delete failed for " + path);

        msg.prepare(msgDeleteConfirm)
                .addString(path)
                .addBoolean(result)
                .send();
    }

    private void onMoveFile(String type, MessageReader reader) {
        String srcPath = reader.readString();
        String dstPath = reader.readString();
        File srcFile = new File(tasksRoot, localizePath(srcPath));
        File dstFile = new File(tasksRoot, localizePath(dstPath));

        if (!srcFile.exists() || dstFile.exists()) {
            msg.prepare(msgMoveConfirm)
                    .addString(srcPath)
                    .addString(dstPath)
                    .addBoolean(false)
                    .send();
            return;
        }

        System.out.println("Moving " + srcPath + " to " + dstPath);
        boolean result = moveFile(srcFile, dstFile);
        if (!result)
            System.err.println("Failed to move " + srcPath + " to " + dstPath);

        msg.prepare(msgMoveConfirm)
                .addString(srcPath)
                .addString(dstPath)
                .addBoolean(result)
                .send();
    }

    private void onMkdir(String type, MessageReader reader) {
        String path = reader.readString();
        File file = new File(tasksRoot, localizePath(path));
        if (file.exists() && !file.isDirectory()) {
            msg.prepare(msgMkdirConfirm)
                    .addString(path)
                    .addBoolean(false)
                    .send();
            return;
        }

        System.out.println("Creating directory " + path);
        boolean result = file.mkdirs();
        if (!result)
            System.err.println("Mkdir failed for " + path);

        msg.prepare(msgMkdirConfirm)
                .addString(path)
                .addBoolean(result)
                .send();
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
            for (String token : command)
                out.addString(token);
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
        if (mgr.getTask(name) != null)
            mgr.removeTask(name);

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

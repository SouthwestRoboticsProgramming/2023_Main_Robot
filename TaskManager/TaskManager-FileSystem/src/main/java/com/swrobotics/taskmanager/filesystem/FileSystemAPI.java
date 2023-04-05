package com.swrobotics.taskmanager.filesystem;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.messenger.client.MessengerClient;

import java.io.*;
import java.nio.file.Files;

public final class FileSystemAPI {
    private static final String MSG_LIST_FILES = ":ListFiles";
    private static final String MSG_READ_FILE = ":ReadFile";
    private static final String MSG_WRITE_FILE = ":WriteFile";
    private static final String MSG_DELETE_FILE = ":DeleteFile";
    private static final String MSG_MOVE_FILE = ":MoveFile";
    private static final String MSG_MKDIR = ":Mkdir";
    private static final String MSG_FILES = ":Files";
    private static final String MSG_FILE_CONTENT = ":FileContent";
    private static final String MSG_WRITE_CONFIRM = ":WriteConfirm";
    private static final String MSG_DELETE_CONFIRM = ":DeleteConfirm";
    private static final String MSG_MOVE_CONFIRM = ":MoveConfirm";
    private static final String MSG_MKDIR_CONFIRM = ":MkdirConfirm";

    private final MessengerClient msg;
    private final File rootDir;

    private final String msgFiles;
    private final String msgFileContent;
    private final String msgWriteConfirm;
    private final String msgDeleteConfirm;
    private final String msgMoveConfirm;
    private final String msgMkdirConfirm;

    public FileSystemAPI(MessengerClient msg, String prefix, File rootDir) {
        this.msg = msg;
        this.rootDir = rootDir;

        String msgListFiles = prefix + MSG_LIST_FILES;
        String msgReadFile = prefix + MSG_READ_FILE;
        String msgWriteFile = prefix + MSG_WRITE_FILE;
        String msgDeleteFile = prefix + MSG_DELETE_FILE;
        String msgMoveFile = prefix + MSG_MOVE_FILE;
        String msgMkdir = prefix + MSG_MKDIR;

        msgFiles = prefix + MSG_FILES;
        msgFileContent = prefix + MSG_FILE_CONTENT;
        msgWriteConfirm = prefix + MSG_WRITE_CONFIRM;
        msgDeleteConfirm = prefix + MSG_DELETE_CONFIRM;
        msgMoveConfirm = prefix + MSG_MOVE_CONFIRM;
        msgMkdirConfirm = prefix + MSG_MKDIR_CONFIRM;

        msg.addHandler(msgListFiles, this::onListFiles);
        msg.addHandler(msgReadFile, this::onReadFile);
        msg.addHandler(msgWriteFile, this::onWriteFile);
        msg.addHandler(msgMoveFile, this::onMoveFile);
        msg.addHandler(msgDeleteFile, this::onDeleteFile);
        msg.addHandler(msgMkdir, this::onMkdir);
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
                    if (!deleteFile(f)) return false;
                }
            }
        }
        return file.delete();
    }

    private boolean moveFile(File src, File dst) {
        return src.renameTo(dst);
    }

    private void onListFiles(String type, MessageReader reader) {
        MessageBuilder out = msg.prepare(msgFiles);

        String dirPath = reader.readString();
        File dir = new File(rootDir, localizePath(dirPath));
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
        File file = new File(rootDir, localizePath(path));
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
        File file = new File(rootDir, localizePath(path));
        if (file.exists() && !file.isFile()) {
            msg.prepare(msgWriteConfirm).addString(path).addBoolean(false).send();
            return;
        }

        int dataLen = reader.readInt();
        byte[] data = reader.readRaw(dataLen);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            System.out.println("Receiving file data for " + path);
            fos.write(data);
            fos.flush();

            msg.prepare(msgWriteConfirm).addString(path).addBoolean(true).send();
        } catch (IOException e) {
            System.err.println("File write failed for " + path);
            e.printStackTrace();

            msg.prepare(msgWriteConfirm).addString(path).addBoolean(false).send();
        }
    }

    private void onDeleteFile(String type, MessageReader reader) {
        String path = reader.readString();
        File file = new File(rootDir, localizePath(path));
        if (!file.exists()) {
            msg.prepare(msgDeleteConfirm).addString(path).addBoolean(false).send();
            return;
        }

        System.out.println("Deleting " + path);
        boolean result = deleteFile(file);
        if (!result) System.err.println("File delete failed for " + path);

        msg.prepare(msgDeleteConfirm).addString(path).addBoolean(result).send();
    }

    private void onMoveFile(String type, MessageReader reader) {
        String srcPath = reader.readString();
        String dstPath = reader.readString();
        File srcFile = new File(rootDir, localizePath(srcPath));
        File dstFile = new File(rootDir, localizePath(dstPath));

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
        if (!result) System.err.println("Failed to move " + srcPath + " to " + dstPath);

        msg.prepare(msgMoveConfirm).addString(srcPath).addString(dstPath).addBoolean(result).send();
    }

    private void onMkdir(String type, MessageReader reader) {
        String path = reader.readString();
        File file = new File(rootDir, localizePath(path));
        if (file.exists() && !file.isDirectory()) {
            msg.prepare(msgMkdirConfirm).addString(path).addBoolean(false).send();
            return;
        }

        System.out.println("Creating directory " + path);
        boolean result = file.mkdirs();
        if (!result) System.err.println("Mkdir failed for " + path);

        msg.prepare(msgMkdirConfirm).addString(path).addBoolean(result).send();
    }
}

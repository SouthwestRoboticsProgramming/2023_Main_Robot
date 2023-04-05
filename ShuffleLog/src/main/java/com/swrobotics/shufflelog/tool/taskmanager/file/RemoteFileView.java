package com.swrobotics.shufflelog.tool.taskmanager.file;

import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.shufflelog.ShuffleLog;
import com.swrobotics.shufflelog.tool.ToolConstants;
import com.swrobotics.shufflelog.util.Cooldown;
import com.swrobotics.shufflelog.util.FileChooser;

import imgui.ImGui;
import imgui.ImGuiViewport;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class RemoteFileView {
    // Filesystem API
    public static final String MSG_LIST_FILES = ":ListFiles";
    public static final String MSG_READ_FILE = ":ReadFile";
    public static final String MSG_WRITE_FILE = ":WriteFile";
    public static final String MSG_DELETE_FILE = ":DeleteFile";
    public static final String MSG_MOVE_FILE = ":MoveFile";
    public static final String MSG_MKDIR = ":Mkdir";
    public static final String MSG_FILES = ":Files";
    public static final String MSG_FILE_CONTENT = ":FileContent";
    public static final String MSG_WRITE_CONFIRM = ":WriteConfirm";
    public static final String MSG_DELETE_CONFIRM = ":DeleteConfirm";
    public static final String MSG_MOVE_CONFIRM = ":MoveConfirm";
    public static final String MSG_MKDIR_CONFIRM = ":MkdirConfirm";

    private final ShuffleLog log;
    private final MessengerClient msg;
    private final String name;

    private final Cooldown reqContentCooldown;
    private final RemoteDirectory remoteRoot;
    private final ImString mkdirName;

    public RemoteFileView(ShuffleLog log, String name) {
        this.log = log;
        this.msg = log.getMessenger();
        this.name = name;

        reqContentCooldown = new Cooldown(ToolConstants.MSG_QUERY_COOLDOWN_TIME);
        remoteRoot = new RemoteDirectory("");
        mkdirName = new ImString(64);

        msg.addHandler(name + MSG_FILES, this::onFiles);
        msg.addHandler(name + MSG_DELETE_CONFIRM, this::onDeleteConfirm);
        msg.addHandler(name + MSG_MKDIR_CONFIRM, this::onMkdirConfirm);
        msg.addHandler(name + MSG_WRITE_CONFIRM, this::onWriteConfirm);
        msg.addHandler(name + MSG_MOVE_CONFIRM, this::onMoveConfirm);
        msg.addHandler(name + MSG_FILE_CONTENT, this::onFileContent);
    }

    private RemoteNode evalPath(String path) {
        if (path.equals("")) return remoteRoot;

        String[] parts = path.split("/");
        RemoteNode node = remoteRoot;
        for (String part : parts) {
            node = ((RemoteDirectory) node).getChild(part);
        }
        return node;
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
            if (isDir) node = new RemoteDirectory(name);
            else node = new RemoteFile(name);

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

    private void onFileContent(String type, MessageReader reader) {
        String filePath = reader.readString();
        boolean success = reader.readBoolean();
        if (!success) return;

        int dataLen = reader.readInt();
        byte[] data = reader.readRaw(dataLen);
        String dataStr = new String(data, StandardCharsets.UTF_8);

        FileEditorTool editor =
                new FileEditorTool(filePath, dataStr, msg, name + MSG_WRITE_FILE, log);
        log.addTool(editor);
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
        String path =
                targetDirPath.equals("") ? file.getName() : targetDirPath + "/" + file.getName();
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
            msg.prepare(name + MSG_MKDIR).addString(path).send();

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
        String dirName = isRoot ? "Root" : dir.getName();

        boolean open =
                ImGui.treeNodeEx(
                        dirName,
                        ImGuiTreeNodeFlags.SpanFullWidth
                                | (isRoot ? ImGuiTreeNodeFlags.DefaultOpen : 0));
        if (ImGui.beginDragDropSource()) {
            ImGui.text(dirName);
            ImGui.setDragDropPayload("TM_" + name + "_DRAG_DIR", dir);
            ImGui.endDragDropSource();
        }
        if (ImGui.beginDragDropTarget()) {
            RemoteNode node = ImGui.acceptDragDropPayload("TM_" + name + "_DRAG_FILE");
            if (node == null) node = ImGui.acceptDragDropPayload("TM_" + name + "_DRAG_DIR");

            if (node != null && !isChild(node, dir)) {
                String dstPath = dir.getFullPath();
                if (!dstPath.equals("")) dstPath += "/";

                msg.prepare(name + MSG_MOVE_FILE)
                        .addString(node.getFullPath())
                        .addString(dstPath + node.getName())
                        .send();
            }

            ImGui.endDragDropTarget();
        }

        ImGui.pushID(dir.getName());

        boolean openNewDirPopup = false;
        if (ImGui.beginPopupContextItem("context_menu")) {
            if (!isRoot && ImGui.selectable("Delete")) {
                msg.prepare(name + MSG_DELETE_FILE).addString(dir.getFullPath()).send();
                ImGui.closeCurrentPopup();
            }
            if (ImGui.selectable("New directory")) {
                ImGui.closeCurrentPopup();
                openNewDirPopup = true;
            }
            if (ImGui.selectable("Upload file(s)")) {
                ImGui.closeCurrentPopup();
                FileChooser.chooseFileOrFolder((file) -> uploadFile(file, dir.getFullPath()));
            }
            if (ImGui.selectable("Refresh")) {
                ImGui.closeCurrentPopup();
                dir.setNeedsRefreshContent(true);
            }
            ImGui.endPopup();
        }

        if (openNewDirPopup) {
            mkdirName.set("");
            ImGui.openPopup("New Directory");
        }

        if (ImGui.beginPopupModal("New Directory", ImGuiWindowFlags.NoMove)) {
            // Center the popup
            ImGuiViewport vp = ImGui.getWindowViewport();
            ImGui.setWindowPos(
                    vp.getCenterX() - ImGui.getWindowWidth() / 2,
                    vp.getCenterY() - ImGui.getWindowHeight() / 2);

            ImGui.text("New directory name:");
            ImGui.setNextItemWidth(300);
            boolean submit =
                    ImGui.inputText("##name", mkdirName, ImGuiInputTextFlags.EnterReturnsTrue);
            ImGui.setItemDefaultFocus();

            ImGui.setNextItemWidth(300);
            submit |= ImGui.button("Create");

            if (submit) {
                String path =
                        isRoot ? mkdirName.get() : (dir.getFullPath() + "/" + mkdirName.get());
                msg.prepare(name + MSG_MKDIR).addString(path).send();
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }

        ImGui.popID();

        if (open) {
            if (dir.needsRefreshContent()) {
                ImGui.indent(ImGui.getTreeNodeToLabelSpacing());
                ImGui.textDisabled("Fetching...");
                ImGui.unindent(ImGui.getTreeNodeToLabelSpacing());

                if (reqContentCooldown.request()) {
                    msg.prepare(name + MSG_LIST_FILES).addString(dir.getFullPath()).send();
                }
            } else {
                for (RemoteNode node : dir.getChildren()) {
                    showNode(node);
                }
            }
            ImGui.treePop();
        }
    }

    private void showFile(RemoteFile file) {
        ImGui.treeNodeEx(
                file.getName(), ImGuiTreeNodeFlags.NoTreePushOnOpen | ImGuiTreeNodeFlags.Leaf);
        ImGui.pushID(file.getName());
        if (ImGui.beginPopupContextItem()) {
            if (ImGui.selectable("Delete")) {
                msg.prepare(name + MSG_DELETE_FILE).addString(file.getFullPath()).send();
                ImGui.closeCurrentPopup();
            }
            if (ImGui.selectable("Edit")) {
                msg.prepare(name + MSG_READ_FILE).addString(file.getFullPath()).send();
            }
            ImGui.endPopup();
        }
        if (ImGui.beginDragDropSource()) {
            ImGui.text(file.getName());
            ImGui.setDragDropPayload("TM_" + this.name + "_DRAG_FILE", file);
            ImGui.endDragDropSource();
        }
        ImGui.popID();
    }

    private void showNode(RemoteNode node) {
        if (node instanceof RemoteDirectory) {
            showDirectory((RemoteDirectory) node, false);
        } else {
            showFile((RemoteFile) node);
        }
    }

    public void process() {
        showDirectory(remoteRoot, true);
    }
}

package com.swrobotics.shufflelog.tool.taskmanager.file;

import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.shufflelog.ShuffleLog;
import com.swrobotics.shufflelog.tool.Tool;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCond;
import imgui.type.ImBoolean;
import imgui.type.ImString;

import java.nio.charset.StandardCharsets;

public final class FileEditorTool implements Tool {
    // Don't try to edit a file that is over a megabyte (probably opened a binary file)
    private static final int MAX_EDIT_LENGTH = 1024 * 1024;

    private final String path;
    private final ImString content;
    private final MessengerClient msg;
    private final String msgWrite;
    private final ShuffleLog log;
    private boolean changed;
    private final ImBoolean open;

    public FileEditorTool(
            String path, String content, MessengerClient msg, String msgWrite, ShuffleLog log) {
        this.path = path;
        this.msg = msg;
        this.msgWrite = msgWrite;
        this.log = log;

        if (content.length() > MAX_EDIT_LENGTH) {
            this.content = null;
        } else {
            this.content = new ImString(MAX_EDIT_LENGTH);
            this.content.set(content);
        }

        changed = false;
        open = new ImBoolean(true);
    }

    @Override
    public void process() {
        ImGui.setNextWindowSize(350, 350, ImGuiCond.Appearing);
        if (ImGui.begin(path, open)) {
            ImGui.alignTextToFramePadding();
            ImGui.text(path);
            ImGui.sameLine();
            ImGui.beginDisabled(!changed);
            if (ImGui.button("Save")) {
                byte[] data = content.get().getBytes(StandardCharsets.UTF_8);
                msg.prepare(msgWrite).addString(path).addInt(data.length).addRaw(data).send();
                changed = false;
            }
            ImGui.endDisabled();
            ImGui.separator();

            ImVec2 size = ImGui.getContentRegionAvail();
            changed |= ImGui.inputTextMultiline("##input", content, size.x, size.y);
        }
        ImGui.end();

        if (!open.get()) log.removeTool(this);
    }
}

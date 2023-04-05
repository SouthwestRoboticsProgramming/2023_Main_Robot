package com.swrobotics.shufflelog.tool.taskmanager;

import com.swrobotics.shufflelog.ShuffleLog;
import com.swrobotics.shufflelog.tool.Tool;
import com.swrobotics.shufflelog.tool.taskmanager.file.RemoteFileView;

import imgui.ImGui;

public final class RoboRIOFilesTool implements Tool {
    private final RemoteFileView fileView;

    public RoboRIOFilesTool(ShuffleLog log) {
        fileView = new RemoteFileView(log, "RoboRIO");
    }

    @Override
    public void process() {
        if (ImGui.begin("RoboRIO Files")) {
            fileView.process();
        }
        ImGui.end();
    }
}

package com.swrobotics.shufflelog.tool.taskmanager;

import imgui.type.ImBoolean;
import imgui.type.ImString;

public final class Task {
    public static final int EDIT_NAME = 1;
    public static final int EDIT_WORK_DIR = 2;
    public static final int EDIT_CMD = 4;
    public static final int EDIT_ENABLE = 8;

    public ImString name;
    public ImString workingDirectory;
    public ImString command;
    public ImBoolean enabled;

    public boolean edited;

    public Task(String name, String workingDirectory, String command, boolean enabled) {
        this.name = new ImString(64);
        this.workingDirectory = new ImString(128);
        this.command = new ImString(256);
        this.enabled = new ImBoolean(enabled);

        this.name.set(name);
        this.workingDirectory.set(workingDirectory);
        this.command.set(command);
    }
}

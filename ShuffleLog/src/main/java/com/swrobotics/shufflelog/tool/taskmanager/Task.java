package com.swrobotics.shufflelog.tool.taskmanager;

import imgui.type.ImBoolean;
import imgui.type.ImString;

import java.util.UUID;

public final class Task {
    public final String uuid;

    public ImString name;
    public ImString workingDirectory;
    public ImString command;
    public ImBoolean enabled;

    public boolean edited;
    public boolean nameEdited;
    public String syncedName;

    public Task(String name, String workingDirectory, String command, boolean enabled) {
        this.uuid = UUID.randomUUID().toString();

        this.name = new ImString(64);
        this.workingDirectory = new ImString(128);
        this.command = new ImString(256);
        this.enabled = new ImBoolean(enabled);

        this.name.set(name);
        this.workingDirectory.set(workingDirectory);
        this.command.set(command);

        syncedName = name;
    }

    public void markSynced() {
        edited = false;
        nameEdited = false;
        syncedName = name.get();
    }
}

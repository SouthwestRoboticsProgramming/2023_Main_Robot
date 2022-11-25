package com.swrobotics.taskmanager;

import org.zeroturnaround.exec.stream.LogOutputStream;

public final class TaskOutputLogger extends LogOutputStream {
    private final Task task;
    private final LogOutputType type;
    private final TaskManagerAPI api;

    public TaskOutputLogger(Task task, LogOutputType type, TaskManagerAPI api) {
        this.task = task;
        this.type = type;
        this.api = api;
    }

    @Override
    protected void processLine(String line) {
        api.broadcastTaskOutput(task, type, line);
        if (type == LogOutputType.STDOUT) {
            System.out.println("[" + task.getName() + "/Out] " + line);
        } else {
            System.err.println("[" + task.getName() + "/Err] " + line);
        }
    }
}

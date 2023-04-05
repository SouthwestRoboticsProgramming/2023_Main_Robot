package com.swrobotics.taskmanager;

import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;

import java.io.File;
import java.io.IOException;

public final class Task {
    // Settings
    private File workingDirectory;
    private String[] command;
    private boolean enabled;

    // Status
    private final transient TaskManagerAPI api;
    private final transient int maxFailCount;
    private transient String name;
    private transient boolean running;
    private transient int failedStartCount;
    private transient Process process;

    public Task(
            File workingDirectory,
            String[] command,
            boolean enabled,
            TaskManagerAPI api,
            int maxFailCount) {
        this.api = api;
        this.maxFailCount = maxFailCount;
        this.workingDirectory = workingDirectory;
        this.command = command;
        this.enabled = enabled;

        running = false;
        failedStartCount = 0;
    }

    public Task(
            File workingDirectory,
            String[] command,
            boolean enabled,
            TaskManagerAPI api,
            int maxFailCount,
            String name) {
        this(workingDirectory, command, enabled, api, maxFailCount);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void start() {
        if (!enabled) return;

        startProcess();
    }

    private void startProcess() {
        try {
            System.out.println("Starting task '" + name + "'");
            StartedProcess p =
                    new ProcessExecutor()
                            .command(command)
                            .directory(workingDirectory)
                            .redirectOutput(new TaskOutputLogger(this, LogOutputType.STDOUT, api))
                            .redirectError(new TaskOutputLogger(this, LogOutputType.STDERR, api))
                            .start();
            process = p.getProcess();
        } catch (IOException e) {
            System.err.println("Exception whilst starting task '" + name + "'");
            e.printStackTrace();
        }
    }

    public void restartIfProcessEnded() {
        if (!enabled) return;
        if (failedStartCount >= maxFailCount) return;
        if (process != null && process.isAlive()) return;

        if (process != null)
            System.err.println(
                    "Process terminated unexpectedly for task '"
                            + name
                            + "' (exit code "
                            + process.exitValue()
                            + ")");
        else System.err.println("Process not present for task '" + name + "'");

        startProcess();
        failedStartCount++;
        if (failedStartCount == maxFailCount) {
            System.err.println(
                    "Task '"
                            + name
                            + "' has exceeded maximum fail count of "
                            + maxFailCount
                            + ", it will not be restarted");
        }
    }

    public void forceStop() {
        if (!enabled || process == null || !process.isAlive()) return;

        System.out.println("Stopping task '" + name + "'");

        // Kill the process and its children
        process.descendants()
                .forEach(
                        (child) -> {
                            child.destroyForcibly();
                        });
        process.destroyForcibly();
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public String[] getCommand() {
        return command;
    }

    public boolean isEnabled() {
        return enabled;
    }
}

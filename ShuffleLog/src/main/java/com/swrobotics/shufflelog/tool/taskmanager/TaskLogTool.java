package com.swrobotics.shufflelog.tool.taskmanager;

import com.swrobotics.shufflelog.ShuffleLog;
import com.swrobotics.shufflelog.tool.Tool;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.type.ImBoolean;

import java.util.ArrayList;
import java.util.List;

public final class TaskLogTool implements Tool {
    private static final int MAX_LOG_HISTORY = 100;

    private final String taskName;
    private final ShuffleLog shuffleLog;
    private final ImBoolean open;

    private static final class Entry {
        final boolean isErr;
        final String line;

        public Entry(boolean isErr, String line) {
            this.isErr = isErr;
            this.line = line;
        }
    }

    private final List<Entry> log;

    public TaskLogTool(String taskName, ShuffleLog shuffleLog) {
        this.taskName = taskName;
        this.shuffleLog = shuffleLog;
        open = new ImBoolean(false);

        log = new ArrayList<>();
    }

    public void setOpen() {
        open.set(true);
    }

    public boolean isOpen() {
        return open.get();
    }

    public void addEntry(boolean err, String message) {
        Entry entry = new Entry(err, message);
        log.add(entry);
        if (log.size() > MAX_LOG_HISTORY) {
            log.remove(0);
        }
    }

    @Override
    public void process() {
        ImGui.setNextWindowSize(350, 350, ImGuiCond.Appearing);
        if (ImGui.begin("Task Log [" + taskName + "]", open)) {
            for (Entry entry : log) {
                if (entry.isErr) ImGui.pushStyleColor(ImGuiCol.Text, 1, 0, 0, 1);
                ImGui.text(entry.line);
                if (entry.isErr) ImGui.popStyleColor();
            }

            // Autoscroll
            if (ImGui.getScrollY() >= ImGui.getScrollMaxY()) ImGui.setScrollHereY(1.0f);
        }
        ImGui.end();

        if (!open.get()) {
            shuffleLog.removeTool(this);
        }
    }
}

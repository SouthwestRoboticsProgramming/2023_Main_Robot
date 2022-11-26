package com.swrobotics.shufflelog;

import com.swrobotics.shufflelog.tool.MenuBarTool;
import com.swrobotics.shufflelog.tool.Tool;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import processing.core.PApplet;
import processing.core.PFont;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class ShuffleLog extends PApplet {
    private static final String LAYOUT_FILE = "layout.ini";

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    private final List<Tool> tools = new ArrayList<>();
    private final List<Tool> addedTools = new ArrayList<>();
    private final List<Tool> removedTools = new ArrayList<>();

    @Override
    public void settings() {
        size(1280, 720, P2D);
    }

    @Override
    public void setup() {
        surface.setResizable(true);
        long windowHandle = (long) surface.getNative();

        ImGui.createContext();

        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(LAYOUT_FILE);
        io.setConfigFlags(ImGuiConfigFlags.DockingEnable);
        Styles.applyDark();

        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init();

        // Set default font
        try {
            textFont(new PFont(getClass().getClassLoader().getResourceAsStream("fonts/PTSans-Regular-14.vlw")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        tools.add(new MenuBarTool());
    }

    @Override
    public void draw() {
        imGuiGlfw.flushEvents();
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        background(210);
        ImGui.dockSpaceOverViewport();

        for (Tool tool : tools) {
            tool.process();
        }
        tools.addAll(addedTools);
        tools.removeAll(removedTools);
        addedTools.clear();
        removedTools.clear();

        flush();
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    public static void main(String[] args) {
        PApplet.main(ShuffleLog.class);
    }
}

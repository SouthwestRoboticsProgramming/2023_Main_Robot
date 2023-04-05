package com.swrobotics.shufflelog;

import imgui.ImFontConfig;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;

import java.io.IOException;

public final class Styles {
    private static final ImFontConfig fontConfig = new ImFontConfig();

    static {
        fontConfig.setFontDataOwnedByAtlas(false);
    }

    private static void loadFont(String res, float size) {
        byte[] data;
        try {
            data = StreamUtil.readResourceToByteArray(res);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        ImGuiIO io = ImGui.getIO();
        io.getFonts().addFontFromMemoryTTF(data, size, fontConfig);
    }

    public static void applyLight() {
        loadFont("fonts/RobotoMono-Regular.ttf", 15);

        ImGui.styleColorsLight();
        ImGuiStyle style = ImGui.getStyle();
        style.setFrameRounding(4.0f);
        style.setGrabRounding(4.0f);
    }

    public static void applyDark() {
        loadFont("fonts/RobotoMono-Regular.ttf", 15);

        ImGuiStyle style = ImGui.getStyle();
        style.setFrameRounding(4.0f);
        style.setGrabRounding(4.0f);

        style.setColor(ImGuiCol.Text, 0.95f, 0.96f, 0.98f, 1.00f);
        style.setColor(ImGuiCol.TextDisabled, 0.36f, 0.42f, 0.47f, 1.00f);
        style.setColor(ImGuiCol.WindowBg, 0.11f, 0.15f, 0.17f, 1.00f);
        style.setColor(ImGuiCol.ChildBg, 0.15f, 0.18f, 0.22f, 1.00f);
        style.setColor(ImGuiCol.PopupBg, 0.08f, 0.08f, 0.08f, 0.94f);
        style.setColor(ImGuiCol.Border, 0.08f, 0.10f, 0.12f, 1.00f);
        style.setColor(ImGuiCol.BorderShadow, 0.00f, 0.00f, 0.00f, 0.00f);
        style.setColor(ImGuiCol.FrameBg, 0.20f, 0.25f, 0.29f, 1.00f);
        style.setColor(ImGuiCol.FrameBgHovered, 0.12f, 0.20f, 0.28f, 1.00f);
        style.setColor(ImGuiCol.FrameBgActive, 0.09f, 0.12f, 0.14f, 1.00f);
        style.setColor(ImGuiCol.TitleBg, 0.09f, 0.12f, 0.14f, 0.65f);
        style.setColor(ImGuiCol.TitleBgActive, 0.08f, 0.10f, 0.12f, 1.00f);
        style.setColor(ImGuiCol.TitleBgCollapsed, 0.00f, 0.00f, 0.00f, 0.51f);
        style.setColor(ImGuiCol.MenuBarBg, 0.15f, 0.18f, 0.22f, 1.00f);
        style.setColor(ImGuiCol.ScrollbarBg, 0.02f, 0.02f, 0.02f, 0.39f);
        style.setColor(ImGuiCol.ScrollbarGrab, 0.20f, 0.25f, 0.29f, 1.00f);
        style.setColor(ImGuiCol.ScrollbarGrabHovered, 0.18f, 0.22f, 0.25f, 1.00f);
        style.setColor(ImGuiCol.ScrollbarGrabActive, 0.09f, 0.21f, 0.31f, 1.00f);
        style.setColor(ImGuiCol.CheckMark, 0.28f, 0.56f, 1.00f, 1.00f);
        style.setColor(ImGuiCol.SliderGrab, 0.28f, 0.56f, 1.00f, 1.00f);
        style.setColor(ImGuiCol.SliderGrabActive, 0.37f, 0.61f, 1.00f, 1.00f);
        style.setColor(ImGuiCol.Button, 0.20f, 0.25f, 0.29f, 1.00f);
        style.setColor(ImGuiCol.ButtonHovered, 0.28f, 0.56f, 1.00f, 1.00f);
        style.setColor(ImGuiCol.ButtonActive, 0.06f, 0.53f, 0.98f, 1.00f);
        style.setColor(ImGuiCol.Header, 0.20f, 0.25f, 0.29f, 0.55f);
        style.setColor(ImGuiCol.HeaderHovered, 0.26f, 0.59f, 0.98f, 0.80f);
        style.setColor(ImGuiCol.HeaderActive, 0.26f, 0.59f, 0.98f, 1.00f);
        style.setColor(ImGuiCol.Separator, 0.20f, 0.25f, 0.29f, 1.00f);
        style.setColor(ImGuiCol.SeparatorHovered, 0.10f, 0.40f, 0.75f, 0.78f);
        style.setColor(ImGuiCol.SeparatorActive, 0.10f, 0.40f, 0.75f, 1.00f);
        style.setColor(ImGuiCol.ResizeGrip, 0.26f, 0.59f, 0.98f, 0.25f);
        style.setColor(ImGuiCol.ResizeGripHovered, 0.26f, 0.59f, 0.98f, 0.67f);
        style.setColor(ImGuiCol.ResizeGripActive, 0.26f, 0.59f, 0.98f, 0.95f);
        style.setColor(ImGuiCol.Tab, 0.11f, 0.15f, 0.17f, 1.00f);
        style.setColor(ImGuiCol.TabHovered, 0.26f, 0.59f, 0.98f, 0.80f);
        style.setColor(ImGuiCol.TabActive, 0.20f, 0.25f, 0.29f, 1.00f);
        style.setColor(ImGuiCol.TabUnfocused, 0.11f, 0.15f, 0.17f, 1.00f);
        style.setColor(ImGuiCol.TabUnfocusedActive, 0.11f, 0.15f, 0.17f, 1.00f);
        style.setColor(ImGuiCol.PlotLines, 0.61f, 0.61f, 0.61f, 1.00f);
        style.setColor(ImGuiCol.PlotLinesHovered, 1.00f, 0.43f, 0.35f, 1.00f);
        style.setColor(ImGuiCol.PlotHistogram, 0.90f, 0.70f, 0.00f, 1.00f);
        style.setColor(ImGuiCol.PlotHistogramHovered, 1.00f, 0.60f, 0.00f, 1.00f);
        style.setColor(ImGuiCol.TextSelectedBg, 0.26f, 0.59f, 0.98f, 0.35f);
        style.setColor(ImGuiCol.DragDropTarget, 1.00f, 1.00f, 0.00f, 0.90f);
        style.setColor(ImGuiCol.NavHighlight, 0.26f, 0.59f, 0.98f, 1.00f);
        style.setColor(ImGuiCol.NavWindowingHighlight, 1.00f, 1.00f, 1.00f, 0.70f);
        style.setColor(ImGuiCol.NavWindowingDimBg, 0.80f, 0.80f, 0.80f, 0.20f);
        style.setColor(ImGuiCol.ModalWindowDimBg, 0.80f, 0.80f, 0.80f, 0.35f);
    }

    private Styles() {
        throw new AssertionError();
    }
}

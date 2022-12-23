package com.swrobotics.shufflelog.tool.blockauto.part;

import imgui.ImGui;

public final class NewLinePart extends StaticPart {
    public static final NewLinePart INSTANCE = new NewLinePart();

    private NewLinePart() {}

    @Override
    public void draw() {
        // No visible elements, just formatting
        ImGui.dummy(0, 0);
    }
}

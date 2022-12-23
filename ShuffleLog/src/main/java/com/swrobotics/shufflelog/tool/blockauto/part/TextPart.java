package com.swrobotics.shufflelog.tool.blockauto.part;

import com.swrobotics.messenger.client.MessageReader;
import imgui.ImGui;

public final class TextPart extends StaticPart {
    public static TextPart read(MessageReader reader) {
        return new TextPart(reader.readString());
    }

    private final String text;

    public TextPart(String text) {
        this.text = text;
    }

    @Override
    public void draw() {
        ImGui.text(text);
    }
}

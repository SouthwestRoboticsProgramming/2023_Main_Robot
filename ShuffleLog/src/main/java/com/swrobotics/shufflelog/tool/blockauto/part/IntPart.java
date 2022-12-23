package com.swrobotics.shufflelog.tool.blockauto.part;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.shufflelog.tool.blockauto.BlockAutoTool;
import imgui.ImGui;
import imgui.type.ImInt;

public final class IntPart extends ParamPart {
    public static IntPart read(MessageReader reader) {
        return new IntPart(reader.readInt());
    }

    private final int def;

    public IntPart(int def) {
        this.def = def;
    }

    private static final ImInt temp = new ImInt();

    @Override
    public Object getDefault() {
        return def;
    }

    @Override
    public boolean edit(Object[] prev) {
        temp.set((int) prev[0]);
        ImGui.setNextItemWidth(75);
        boolean changed = ImGui.inputInt("", temp);
        if (changed)
            prev[0] = temp.get();
        return changed;
    }

    @Override
    public Object readInst(MessageReader reader, BlockAutoTool tool) {
        return reader.readInt();
    }

    @Override
    public void writeInst(MessageBuilder builder, Object value) {
        builder.addInt((int) value);
    }

    @Override
    public boolean isFrame() {
        return true;
    }
}

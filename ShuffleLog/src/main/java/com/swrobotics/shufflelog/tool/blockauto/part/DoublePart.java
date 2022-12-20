package com.swrobotics.shufflelog.tool.blockauto.part;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.shufflelog.tool.blockauto.BlockAutoTool;
import imgui.ImGui;
import imgui.type.ImDouble;

public final class DoublePart extends ParamPart {
    public static DoublePart read(MessageReader reader) {
        return new DoublePart(reader.readDouble());
    }

    private final double def;

    public DoublePart(double def) {
        this.def = def;
    }

    @Override
    public Object getDefault() {
        return def;
    }

    private static final ImDouble temp = new ImDouble();

    @Override
    public boolean edit(Object[] prev) {
        temp.set((double) prev[0]);
        ImGui.setNextItemWidth(50);
        boolean changed = ImGui.inputDouble("", temp);
        if (changed)
            prev[0] = temp.get();
        return changed;
    }

    @Override
    public Object readInst(MessageReader reader, BlockAutoTool tool) {
        return reader.readDouble();
    }

    @Override
    public void writeInst(MessageBuilder builder, Object value) {
        builder.addDouble((double) value);
    }

    @Override
    public boolean isFrame() {
        return true;
    }
}


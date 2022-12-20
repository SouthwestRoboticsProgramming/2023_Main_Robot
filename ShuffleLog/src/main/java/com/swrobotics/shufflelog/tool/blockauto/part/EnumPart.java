package com.swrobotics.shufflelog.tool.blockauto.part;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.shufflelog.tool.blockauto.BlockAutoTool;
import imgui.ImGui;
import imgui.type.ImInt;

public final class EnumPart extends ParamPart {
    public static EnumPart read(MessageReader reader) {
        int count = reader.readInt();
        String[] values = new String[count];
        for (int i = 0; i < count; i++) {
            values[i] = reader.readString();
        }
        int defIdx = reader.readInt();

        return new EnumPart(values, defIdx);
    }

    private final String[] values;
    private final int defIdx;

    public EnumPart(String[] values, int defIdx) {
        this.values = values;
        this.defIdx = defIdx;
    }

    @Override
    public Object getDefault() {
        return defIdx;
    }

    private static final ImInt temp = new ImInt();

    @Override
    public boolean edit(Object[] prev) {
        temp.set((int) prev[0]);
        ImGui.setNextItemWidth(50);
        boolean changed = ImGui.combo("", temp, values);
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

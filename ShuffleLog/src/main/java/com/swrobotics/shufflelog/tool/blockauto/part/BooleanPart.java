package com.swrobotics.shufflelog.tool.blockauto.part;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.shufflelog.tool.blockauto.BlockAutoTool;
import imgui.ImGui;
import imgui.type.ImBoolean;

public final class BooleanPart extends ParamPart {
    public static BooleanPart read(MessageReader reader) {
        return new BooleanPart(reader.readBoolean());
    }

    private final boolean def;

    public BooleanPart(boolean def) {
        this.def = def;
    }

    @Override
    public Object getDefault() {
        return def;
    }

    private static final ImBoolean temp = new ImBoolean();

    @Override
    public boolean edit(Object[] val) {
        temp.set((boolean) val[0]);
        boolean changed = ImGui.checkbox("##checkbox", temp);
        if (changed)
            val[0] = temp.get();
        return changed;
    }

    @Override
    public Object readInst(MessageReader reader, BlockAutoTool tool) {
        return reader.readBoolean();
    }

    @Override
    public void writeInst(MessageBuilder builder, Object value) {
        builder.addBoolean((boolean) value);
    }

    @Override
    public boolean isFrame() {
        return true;
    }
}

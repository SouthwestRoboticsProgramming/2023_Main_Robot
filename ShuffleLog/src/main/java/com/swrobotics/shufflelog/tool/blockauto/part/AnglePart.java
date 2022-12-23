package com.swrobotics.shufflelog.tool.blockauto.part;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.shufflelog.tool.blockauto.BlockAutoTool;
import imgui.ImGui;
import imgui.type.ImDouble;

import java.util.HashMap;
import java.util.Map;

public final class AnglePart extends ParamPart {
    public enum Mode {
        CW_DEG(0),
        CCW_DEG(1),
        CW_RAD(2),
        CCW_RAD(3),
        CW_ROT(4),
        CCW_ROT(5);

        private static final Map<Byte, Mode> BY_ID = new HashMap<>();
        static {
            for (Mode m : values()) {
                BY_ID.put(m.id, m);
            }
        }

        private final byte id;

        Mode(int id) {
            this.id = (byte) id;
        }
    }

    public static AnglePart read(MessageReader reader) {
        Mode mode = Mode.BY_ID.get(reader.readByte());
        double val = reader.readDouble();
        return new AnglePart(mode, val);
    }

    private final Mode mode;
    private final double def;

    public AnglePart(Mode mode, double def) {
        this.mode = mode;
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

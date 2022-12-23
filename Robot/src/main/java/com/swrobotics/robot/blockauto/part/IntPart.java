package com.swrobotics.robot.blockauto.part;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;

public final class IntPart implements ParamPart {
    private final int def;

    public IntPart(int def) {
        this.def = def;
    }

    @Override
    public Object readInst(MessageReader reader) {
        return reader.readInt();
    }

    @Override
    public void writeInst(MessageBuilder builder, Object val) {
        builder.addInt((int) val);
    }

    @Override
    public void writeToMessenger(MessageBuilder builder) {
        builder.addByte(PartTypes.INT.getId());
        builder.addInt(def);
    }
}

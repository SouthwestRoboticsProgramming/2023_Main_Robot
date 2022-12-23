package com.swrobotics.robot.blockauto.part;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;

public final class DoublePart implements ParamPart {
    private final double def;

    public DoublePart(double def) {
        this.def = def;
    }

    @Override
    public Object readInst(MessageReader reader) {
        return reader.readDouble();
    }

    @Override
    public void writeInst(MessageBuilder builder, Object val) {
        builder.addDouble((double) val);
    }

    @Override
    public void writeToMessenger(MessageBuilder builder) {
        builder.addByte(PartTypes.DOUBLE.getId());
        builder.addDouble(def);
    }
}

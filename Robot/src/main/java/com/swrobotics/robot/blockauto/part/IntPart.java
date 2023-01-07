package com.swrobotics.robot.blockauto.part;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;

public final class IntPart extends ParamPart {
    private final int def;

    public IntPart(String name, int def) {
        super(name);
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

    @Override
    public Object deserializeInst(JsonElement elem) {
        if (elem == null) return def;
        return elem.getAsInt();
    }

    @Override
    public JsonElement serializeInst(Object val) {
        return new JsonPrimitive((int) val);
    }
}

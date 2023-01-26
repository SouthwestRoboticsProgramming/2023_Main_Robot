package com.swrobotics.robot.blockauto.part;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
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
    public Object deserializeInst(JsonElement elem, JsonDeserializationContext ctx) {
        if (elem == null) return def;
        return elem.getAsInt();
    }

    @Override
    public JsonElement serializeInst(Object val, JsonSerializationContext ctx) {
        return new JsonPrimitive((int) val);
    }
}

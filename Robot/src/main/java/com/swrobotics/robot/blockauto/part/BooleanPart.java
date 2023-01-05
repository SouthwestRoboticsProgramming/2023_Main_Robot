package com.swrobotics.robot.blockauto.part;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;

public final class BooleanPart extends ParamPart {
    private final boolean def;

    public BooleanPart(String name, boolean def) {
        super(name);
        this.def = def;
    }

    @Override
    public Object readInst(MessageReader reader) {
        return reader.readBoolean();
    }

    @Override
    public void writeInst(MessageBuilder builder, Object val) {
        builder.addBoolean((boolean) val);
    }

    @Override
    public void writeToMessenger(MessageBuilder builder) {
        builder.addByte(PartTypes.INT.getId());
        builder.addBoolean(def);
    }

    @Override
    public Object deserializeInst(JsonElement elem) {
        if (elem == null) return def;
        return elem.getAsBoolean();
    }

    @Override
    public JsonElement serializeInst(Object val) {
        return new JsonPrimitive((boolean) val);
    }
}

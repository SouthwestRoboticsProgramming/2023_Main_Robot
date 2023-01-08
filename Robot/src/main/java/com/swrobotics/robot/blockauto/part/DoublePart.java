package com.swrobotics.robot.blockauto.part;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;

public final class DoublePart extends ParamPart {
    private final double def;

    public DoublePart(String name, double def) {
        super(name);
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

    @Override
    public Object deserializeInst(JsonElement elem) {
        if (elem == null)
            return def;
        return elem.getAsDouble();
    }

    @Override
    public JsonElement serializeInst(Object val) {
        return new JsonPrimitive((double) val);
    }
}

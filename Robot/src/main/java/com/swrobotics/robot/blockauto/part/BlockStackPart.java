package com.swrobotics.robot.blockauto.part;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.robot.blockauto.BlockStackInst;

public final class BlockStackPart extends ParamPart {
    public BlockStackPart(String name) {
        super(name);
    }

    @Override
    public Object readInst(MessageReader reader) {
        return BlockStackInst.readFromMessenger(reader);
    }

    @Override
    public void writeInst(MessageBuilder builder, Object val) {
        ((BlockStackInst) val).write(builder);
    }

    @Override
    public void writeToMessenger(MessageBuilder builder) {
        builder.addByte(PartTypes.BLOCK_STACK.getId());
    }

    @Override
    public Object deserializeInst(JsonElement elem, JsonDeserializationContext ctx) {
        if (elem == null) return new BlockStackInst();
        return ctx.deserialize(elem, BlockStackInst.class);
    }

    @Override
    public JsonElement serializeInst(Object val, JsonSerializationContext ctx) {
        return ctx.serialize(val);
    }
}

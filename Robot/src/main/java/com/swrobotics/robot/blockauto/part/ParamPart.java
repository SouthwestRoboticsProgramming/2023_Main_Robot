package com.swrobotics.robot.blockauto.part;

import com.google.gson.JsonElement;
import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;

public abstract class ParamPart implements BlockPart {
    private final String name;

    public ParamPart(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract Object readInst(MessageReader reader);
    public abstract void writeInst(MessageBuilder builder, Object val);

    public abstract Object deserializeInst(JsonElement elem);
    public abstract JsonElement serializeInst(Object val);
}

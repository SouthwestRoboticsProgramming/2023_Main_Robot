package com.swrobotics.robot.blockauto.part;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;

public class Vec2dPart extends ParamPart {
    private final double defX;
    private final double defY;

    public Vec2dPart(String name, double defX, double defY) {
        super(name);
        this.defX = defX;
        this.defY = defY;
    }

    @Override
    public Object readInst(MessageReader reader) {
        return new Vec2d(reader.readDouble(), reader.readDouble());
    }

    @Override
    public void writeInst(MessageBuilder builder, Object val) {
        Vec2d v = (Vec2d) val;
        builder.addDouble(v.x);
        builder.addDouble(v.y);
    }

    @Override
    public void writeToMessenger(MessageBuilder builder) {
        builder.addByte(PartTypes.VEC2D.getId());
        builder.addDouble(defX);
        builder.addDouble(defY);
    }

    @Override
    public Object deserializeInst(JsonElement elem, JsonDeserializationContext ctx) {
        if (elem == null)
            return new Vec2d(defX, defY);

        JsonArray arr = elem.getAsJsonArray();
        return new Vec2d(arr.get(0).getAsDouble(), arr.get(1).getAsDouble());
    }

    @Override
    public JsonElement serializeInst(Object val, JsonSerializationContext ctx) {
        Vec2d v = (Vec2d) val;
        JsonArray arr = new JsonArray();
        arr.add(v.x);
        arr.add(v.y);
        return arr;
    }
}

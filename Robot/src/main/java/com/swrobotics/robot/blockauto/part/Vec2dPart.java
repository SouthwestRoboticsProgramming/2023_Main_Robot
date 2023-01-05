package com.swrobotics.robot.blockauto.part;

import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;

public class Vec2dPart implements ParamPart {
    private final double defX;
    private final double defY;

    public Vec2dPart(double defX, double defY) {
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
}

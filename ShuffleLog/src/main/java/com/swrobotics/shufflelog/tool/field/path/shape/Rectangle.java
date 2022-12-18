package com.swrobotics.shufflelog.tool.field.path.shape;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import imgui.type.ImDouble;

import java.util.UUID;

public final class Rectangle extends Shape {
    public final ImDouble x;
    public final ImDouble y;
    public final ImDouble width;
    public final ImDouble height;
    public final ImDouble rotation;

    public Rectangle(UUID id, boolean inverted) {
        super(id, inverted);

        x = new ImDouble();
        y = new ImDouble();
        width = new ImDouble();
        height = new ImDouble();
        rotation = new ImDouble();
    }

    @Override
    protected void readContent(MessageReader reader) {
        x.set(reader.readDouble());
        y.set(reader.readDouble());
        width.set(reader.readDouble());
        height.set(reader.readDouble());
        rotation.set(Math.toDegrees(reader.readDouble()));
    }

    @Override
    public void write(MessageBuilder builder) {
        super.write(builder);
        builder.addByte(RECTANGLE);
        builder.addDouble(x.get());
        builder.addDouble(y.get());
        builder.addDouble(width.get());
        builder.addDouble(height.get());
        builder.addDouble(Math.toRadians(rotation.get()));
    }
}

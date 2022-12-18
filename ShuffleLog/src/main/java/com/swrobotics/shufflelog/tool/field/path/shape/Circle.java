package com.swrobotics.shufflelog.tool.field.path.shape;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import imgui.type.ImDouble;

import java.util.UUID;

public final class Circle extends Shape {
    public final ImDouble x;
    public final ImDouble y;
    public final ImDouble radius;

    public Circle(UUID id, boolean inverted) {
        super(id, inverted);

        x = new ImDouble();
        y = new ImDouble();
        radius = new ImDouble();
    }

    @Override
    protected void readContent(MessageReader reader) {
        x.set(reader.readDouble());
        y.set(reader.readDouble());
        radius.set(reader.readDouble());
    }

    @Override
    public void write(MessageBuilder builder) {
        super.write(builder);
        builder.addByte(CIRCLE);
        builder.addDouble(x.get());
        builder.addDouble(y.get());
        builder.addDouble(radius.get());
    }
}

package com.swrobotics.shufflelog.tool.field.path.grid;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.shufflelog.tool.field.path.PathfindingLayer;
import com.swrobotics.shufflelog.tool.field.path.shape.Shape;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ShapeGrid extends Grid {
    private final Set<Shape> shapes;

    public ShapeGrid(UUID id) {
        super(id);
        shapes = new HashSet<>();
    }

    public Set<Shape> getShapes() {
        return shapes;
    }

    @Override
    public void readContent(MessageReader reader) {
        int count = reader.readInt();
        for (int i = 0; i < count; i++) {
            shapes.add(Shape.read(reader));
        }
    }

    @Override
    public void register(PathfindingLayer layer) {
        super.register(layer);
        for (Shape shape : shapes) {
            shape.register(layer);
        }
    }

    @Override
    public void write(MessageBuilder builder) {
        super.write(builder);
        builder.addByte(SHAPE);
    }
}

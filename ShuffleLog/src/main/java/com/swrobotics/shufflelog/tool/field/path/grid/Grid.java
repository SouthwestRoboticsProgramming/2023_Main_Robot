package com.swrobotics.shufflelog.tool.field.path.grid;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.shufflelog.tool.field.path.FieldNode;
import com.swrobotics.shufflelog.tool.field.path.PathfindingLayer;

import java.util.UUID;

public abstract class Grid implements FieldNode {
    // Type IDs
    public static final byte UNION = 0;
    public static final byte BITFIELD = 1;
    public static final byte SHAPE = 2;

    private final UUID id;
    private GridUnion parent;

    public Grid(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public GridUnion getParent() {
        return parent;
    }

    public void setParent(GridUnion parent) {
        this.parent = parent;
    }

    public abstract void readContent(MessageReader reader);

    public void register(PathfindingLayer layer) {
        layer.registerGrid(this);
    }

    public static Grid read(MessageReader reader) {
        long idMsb = reader.readLong();
        long idLsb = reader.readLong();
        UUID id = new UUID(idMsb, idLsb);

        byte type = reader.readByte();
        Grid grid;
        switch (type) {
            case UNION:
                grid = new GridUnion(id);
                break;
            case BITFIELD:
                grid = new BitfieldGrid(id);
                break;
            case SHAPE:
                grid = new ShapeGrid(id);
                break;
            default:
                throw new RuntimeException("Unknown type id: " + type);
        }
        grid.readContent(reader);

        return grid;
    }

    public void write(MessageBuilder builder) {
        builder.addLong(id.getMostSignificantBits());
        builder.addLong(id.getLeastSignificantBits());
    }
}

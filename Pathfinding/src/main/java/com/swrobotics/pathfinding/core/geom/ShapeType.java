package com.swrobotics.pathfinding.core.geom;

public enum ShapeType {
    CIRCLE((byte) 0, Circle.class),
    RECTANGLE((byte) 1, Rectangle.class);

    private final byte typeId;
    private final Class<? extends Shape> type;

    ShapeType(byte typeId, Class<? extends Shape> type) {
        this.typeId = typeId;
        this.type = type;
    }

    public byte getTypeId() {
        return typeId;
    }

    public Class<? extends Shape> getType() {
        return type;
    }
}

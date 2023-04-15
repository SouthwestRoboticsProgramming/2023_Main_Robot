package com.swrobotics.pathfinding.core.grid;

public enum GridType {
    UNION((byte) 0, GridUnion.class),
    BITFIELD((byte) 1, BitfieldGrid.class),
    SHAPE((byte) 2, ShapeGrid.class);

    private final byte typeId;
    private final Class<? extends Grid> type;

    GridType(byte typeId, Class<? extends Grid> type) {
        this.typeId = typeId;
        this.type = type;
    }

    public byte getTypeId() {
        return typeId;
    }

    public Class<? extends Grid> getType() {
        return type;
    }
}

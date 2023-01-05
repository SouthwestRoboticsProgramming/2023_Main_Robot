package com.swrobotics.robot.blockauto.part;

public enum PartTypes {
    TEXT(0),
    INT(1),
    DOUBLE(2),
    VEC2D(3),
    FIELD_POINT(4),
    ANGLE(5),
    ENUM(6),
    BLOCK_STACK(7),
    NEW_LINE(8),
    BOOLEAN(9);

    // This will be fine until we have more than 256 part types
    // (probably won't ever happen)
    private final byte id;

    PartTypes(int id) {
        this.id = (byte) id;
    }

    public byte getId() {
        return id;
    }
}

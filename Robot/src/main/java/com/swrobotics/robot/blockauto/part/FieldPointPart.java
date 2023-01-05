package com.swrobotics.robot.blockauto.part;

public final class FieldPointPart extends Vec2dPart {
    public FieldPointPart(String name, double defX, double defY) {
        super(name, defX, defY);
    }

    @Override
    protected byte getTypeId() {
        return PartTypes.FIELD_POINT.getId();
    }
}

package com.swrobotics.robot.blockauto.part;

public final class FieldPointPart extends Vec2dPart {
    public FieldPointPart(double defX, double defY) {
        super(defX, defY);
    }

    @Override
    protected byte getTypeId() {
        return PartTypes.FIELD_POINT.getId();
    }
}

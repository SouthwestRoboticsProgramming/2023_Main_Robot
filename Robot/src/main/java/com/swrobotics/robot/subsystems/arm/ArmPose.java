package com.swrobotics.robot.subsystems.arm;

import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.Vec2d;

public final class ArmPose {
    // Angle of bottom segment relative to horizontal
    public final Angle bottomAngle;

    // Angle of top segment relative to horizontal
    public final Angle topAngle;

    // Angle of wrist angle relative to horizontal
    public final Angle wristAngle;

    public ArmPose(Angle bottomAngle, Angle topAngle, Angle wristAngle) {
        this.bottomAngle = bottomAngle;
        this.topAngle = topAngle;
        this.wristAngle = wristAngle;
    }

    /**
     * Gets the position of the joint between the bottom and top segments
     *
     * @return midpoint in meters
     */
    public Vec2d getMidpoint() {
        return new Vec2d(bottomAngle, ArmConstants.BOTTOM_LENGTH);
    }

    /**
     * Gets the position of the axis of rotation of the wrist
     *
     * @return wrist axis of rotation in meters
     */
    public Vec2d getAxisPos() {
        return getMidpoint().add(new Vec2d(topAngle, ArmConstants.TOP_LENGTH));
    }

    public ArmPosition toPosition() {
        return new ArmPosition(getAxisPos(), wristAngle);
    }

    @Override
    public String toString() {
        return "ArmPose{" +
                "bottomAngle=" + bottomAngle +
                ", topAngle=" + topAngle +
                ", wristAngle=" + wristAngle +
                '}';
    }
}

package com.swrobotics.lib.field;

import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.mathlib.Vec2d;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.DriverStation;

/** Represents the information related to the layout of the game field. */
public final class FieldInfo {
    /** Information for the 2023 Charged Up field */
    public static final FieldInfo CHARGED_UP_2023 =
            new FieldInfo(16.4846, 8.02, FieldSymmetry.LATERAL);

    private final Vec2d size;
    private final FieldSymmetry symmetry;

    public FieldInfo(double width, double height, FieldSymmetry symmetry) {
        size = new Vec2d(width, height);
        this.symmetry = symmetry;
    }

    /**
     * Gets the size of the field in meters.
     *
     * @return size in meters
     */
    public Vec2d getSize() {
        return new Vec2d(size);
    }

    /**
     * Gets the width of the field in meters. This is the horizontal distance relative to the
     * scoring table.
     *
     * @return width in meters
     */
    public double getWidth() {
        return size.x;
    }

    /**
     * Gets the height of the field in meters. This is the vertical distance relative to the scoring
     * table.
     *
     * @return height in meters
     */
    public double getHeight() {
        return size.y;
    }

    /**
     * Gets the center position of the field in meters.
     *
     * @return center position in meters
     */
    public Vec2d getCenter() {
        return new Vec2d(size).div(2);
    }

    /**
     * Gets the type of symmetry for the two halves of the field.
     *
     * @return alliance symmetry
     */
    public FieldSymmetry getSymmetry() {
        return symmetry;
    }

    /**
     * Flips a pose to be relative to the current alliance. This is equivalent to {@code
     * getSymmetry().flipForAlliance(bluePose, this)}.
     *
     * @param bluePose pose as if the robot was on blue alliance
     * @return pose relative to the current alliance
     */
    public Pose2d flipPoseForAlliance(Pose2d bluePose) {
        return symmetry.flipForAlliance(bluePose, this);
    }

    /**
     * Gets the forward vector relative to the current alliance driver station.
     *
     * @return alliance forward vector
     */
    public Vec2d getAllianceForward() {
        return DriverStation.getAlliance() == DriverStation.Alliance.Blue
                ? new Vec2d(1, 0)
                : new Vec2d(-1, 0);
    }

    /**
     * Gets the angle pointing forward relative to the current alliance driver station.
     *
     * @return alliance forward angle
     */
    public Angle getAllianceForwardAngle() {
        return DriverStation.getAlliance() == DriverStation.Alliance.Blue
                ? Angle.ZERO
                : CCWAngle.deg(180);
    }

    /**
     * Gets the reverse vector relative to the current alliance driver station.
     *
     * @return alliance reverse vector
     */
    public Vec2d getAllianceReverse() {
        return DriverStation.getAlliance() == DriverStation.Alliance.Blue
                ? new Vec2d(-1, 0)
                : new Vec2d(1, 0);
    }

    /**
     * Gets the angle pointing reverse relative to the current alliance driver station.
     *
     * @return alliance reverse angle
     */
    public Angle getAllianceReverseAngle() {
        return DriverStation.getAlliance() == DriverStation.Alliance.Blue
                ? CCWAngle.deg(180)
                : Angle.ZERO;
    }

    @Override
    public String toString() {
        return "FieldInfo{" + "size=" + size + ", symmetry=" + symmetry + '}';
    }
}

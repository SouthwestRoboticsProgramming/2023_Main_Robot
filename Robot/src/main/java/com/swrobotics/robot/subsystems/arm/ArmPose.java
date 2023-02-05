package com.swrobotics.robot.subsystems.arm;

import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.mathlib.MathUtil;
import com.swrobotics.mathlib.Vec2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;

public final class ArmPose {
    // FIXME: Set based on rules in game manual and dimensions of robot
    private static final double MAX_HORIZ_EXTENT = 2;
    private static final double MAX_VERT_EXTENT = 2;
    private static final double FLOOR_LEVEL = -0.5;

    public static boolean isEndPositionValid(Translation2d endPos) {
        double x = endPos.getX();
        double y = endPos.getY();

        // Make sure it's possible for the arm to reach the position
        double lengthSq = x * x + y * y;
        if (lengthSq > MathUtil.square(ArmSubsystem.BOTTOM_LENGTH + ArmSubsystem.TOP_LENGTH) + MathUtil.EPSILON
            || lengthSq + MathUtil.EPSILON < MathUtil.square(ArmSubsystem.BOTTOM_LENGTH - ArmSubsystem.TOP_LENGTH)) {
            return false;
        }

        // Make sure the position is legal according to rules and does not hit floor
        return x >= -0.2 && x < MAX_HORIZ_EXTENT && y > FLOOR_LEVEL && y < MAX_VERT_EXTENT;
    }

    public static ArmPose fromEndPosition(Translation2d position) {
        System.out.println("Set position to: " + position);

        if (!isEndPositionValid(position)) {
            String posFormat = String.format("(%.3f, %.3f)", position.getX(), position.getY());
            DriverStation.reportWarning("Trying to set arm to illegal position: " + posFormat, false);
            return null;
        }

        double lengthA = ArmSubsystem.BOTTOM_LENGTH;
        double lengthB = ArmSubsystem.TOP_LENGTH;
        double targetX = position.getX();
        double targetY = position.getY();

        double targetAngle = Math.atan2(-targetX, targetY) + Math.PI / 2;
        double len = Math.sqrt(targetX * targetX + targetY * targetY);

        double angleAL = Math.acos((lengthA * lengthA + len * len - lengthB * lengthB) / (2 * lengthA * len));
        double angleAB = Math.acos((lengthA * lengthA + lengthB * lengthB - len * len) / (2 * lengthA * lengthB));

        double angle1 = targetAngle + angleAL;
        double angle2 = angle1 + angleAB - Math.PI;

        return new ArmPose(angle1, angle2);
    }

    // CCW radians from horizontal
    public final double bottomAngle;
    public final double topAngle;

    public ArmPose(double bottomAngle, double topAngle) {
        this.bottomAngle = bottomAngle;
        this.topAngle = topAngle;
    }

    public Translation2d getCenterPosition() {
        return new Translation2d(ArmSubsystem.BOTTOM_LENGTH, new Rotation2d(bottomAngle));
    }

    public Translation2d getEndPosition() {
        Vec2d pos = new Vec2d(ArmSubsystem.BOTTOM_LENGTH, 0).rotateBy(CCWAngle.rad(bottomAngle))
                .add(new Vec2d(ArmSubsystem.TOP_LENGTH, 0).rotateBy(CCWAngle.rad(topAngle)));

        return new Translation2d(pos.x, pos.y);
    }

    public boolean isValid() {
        return isEndPositionValid(getEndPosition());
    }

    @Override
    public String toString() {
        return "ArmPose{" +
                "bottomAngle=" + bottomAngle +
                ", topAngle=" + topAngle +
                '}';
    }
}

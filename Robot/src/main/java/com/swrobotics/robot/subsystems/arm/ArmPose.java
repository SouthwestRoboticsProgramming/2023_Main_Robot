package com.swrobotics.robot.subsystems.arm;

import static com.swrobotics.robot.subsystems.arm.ArmConstants.*;

import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.mathlib.MathUtil;
import com.swrobotics.mathlib.Vec2d;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

public final class ArmPose {
//    // FIXME: Set based on rules in game manual and dimensions of robot
//    private static final double MAX_HORIZ_EXTENT = 2;
//    private static final double MAX_VERT_EXTENT = 2;
//    private static final double FLOOR_LEVEL = -0.5;
//
//    private static final double MIN_LOWER_ANGLE = Math.toRadians(45);
//    private static final double MAX_LOWER_ANGLE = Math.toRadians(135);

//    private static boolean isEndPositionValid(Translation2d endPos) {
//        double x = endPos.getX();
//        double y = endPos.getY();
//
//        // Make sure it's possible for the arm to reach the position
//        double lengthSq = x * x + y * y;
//        if (lengthSq > MathUtil.square(BOTTOM_LENGTH + TOP_LENGTH) + MathUtil.EPSILON
//                || lengthSq + MathUtil.EPSILON < MathUtil.square(BOTTOM_LENGTH - TOP_LENGTH)) {
//            return false;
//        }
//
//        // Make sure the position is legal according to rules and does not hit floor
//        return x >= -0.2 && x < MAX_HORIZ_EXTENT /*&& y > FLOOR_LEVEL*/ && y < MAX_VERT_EXTENT;
//    }

    public static ArmPose fromEndPosition(Translation2d position, double wristAngle) {
//        if (!isEndPositionValid(position)) {
//            String posFormat = String.format("(%.3f, %.3f)", position.getX(), position.getY());
//            System.err.println("Trying to set arm to illegal position: " + posFormat);
//            return null;
//        }

        double lengthA = BOTTOM_LENGTH;
        double lengthB = TOP_LENGTH;
        double targetX = position.getX();
        double targetY = position.getY();

        double targetAngle = Math.atan2(-targetX, targetY) + Math.PI / 2;
        double len = Math.sqrt(targetX * targetX + targetY * targetY);

        double angleAL =
                Math.acos(
                        (lengthA * lengthA + len * len - lengthB * lengthB) / (2 * lengthA * len));
        double angleAB =
                Math.acos(
                        (lengthA * lengthA + lengthB * lengthB - len * len)
                                / (2 * lengthA * lengthB));

        double angle1 = targetAngle + angleAL;
        double angle2 = angle1 + angleAB - Math.PI;

        return new ArmPose(angle1, angle2, wristAngle);
    }

    // CCW radians from horizontal
    public final double bottomAngle;
    public final double topAngle;
    public final double wristAngle;

    public ArmPose(double bottomAngle, double topAngle, double wristAngle) {
        this.bottomAngle = bottomAngle;
        this.topAngle = topAngle;
        this.wristAngle = wristAngle;
    }

    public Translation2d getCenterPosition() {
        return new Translation2d(BOTTOM_LENGTH, new Rotation2d(bottomAngle));
    }

    public Translation2d getEndPosition() {
        Vec2d pos =
                new Vec2d(BOTTOM_LENGTH, 0)
                        .rotateBy(CCWAngle.rad(bottomAngle))
                        .add(new Vec2d(TOP_LENGTH, 0).rotateBy(CCWAngle.rad(topAngle)));

        return new Translation2d(pos.x, pos.y);
    }

//    public boolean isValid() {
//        if (bottomAngle < MIN_LOWER_ANGLE) return false;
//        if (bottomAngle > MAX_LOWER_ANGLE) return false;
//
//        return isEndPositionValid(getEndPosition());
//    }

    @Override
    public String toString() {
        return "ArmPose{" + "bottomAngle=" + bottomAngle + ", topAngle=" + topAngle + ", wristAngle="
                + wristAngle + '}';
    }
}

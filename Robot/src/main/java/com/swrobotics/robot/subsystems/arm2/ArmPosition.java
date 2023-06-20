package com.swrobotics.robot.subsystems.arm2;

import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.robot.subsystems.arm.ArmConstants;

public final class ArmPosition {
    // Position in meters of the wrist's axis of rotation along the right side plane
    public final Vec2d axisPos;

    // Angle of the wrist relative to horizontal
    public final Angle wristAngle;

    public ArmPosition(Vec2d axisPos, Angle wristAngle) {
        this.axisPos = axisPos;
        this.wristAngle = wristAngle;
    }

    /**
     * Finds a pose that satisfies the constraints specified in this position.
     *
     * @return optimal pose that satisfies constraints, or null if no such pose
     *         exists
     */
    public ArmPose toPose() {
        double lengthA = ArmConstants.BOTTOM_LENGTH;
        double lengthB = ArmConstants.TOP_LENGTH;
        double targetX = axisPos.x;
        double targetY = axisPos.y;

        double flip = targetX < 0 ? -1 : 1;

        double targetAngle = Math.atan2(-targetX, targetY) + Math.PI / 2;
        double len = Math.sqrt(targetX * targetX + targetY * targetY);
        if (len >= lengthA + lengthB) {
            // Target is too far away, it's impossible for the arm to reach
            return null;
        }

        double angleAL =
                Math.acos(
                        (lengthA * lengthA + len * len - lengthB * lengthB) / (2 * lengthA * len));
        double angleAB =
                Math.acos(
                        (lengthA * lengthA + lengthB * lengthB - len * len)
                                / (2 * lengthA * lengthB));

        Angle angle1 = CCWAngle.rad(targetAngle + flip * angleAL);
        Angle angle2 = angle1.ccw().add(CCWAngle.rad(flip * angleAB - Math.PI)).wrapDeg(-270, 90);
        Angle wrist = wristAngle.sub(angle2);

        return new ArmPose(angle1, angle2, wrist);
    }

    @Override
    public String toString() {
        return "ArmPosition{" +
                "axisPos=" + axisPos +
                ", wristAngle=" + wristAngle +
                '}';
    }
}

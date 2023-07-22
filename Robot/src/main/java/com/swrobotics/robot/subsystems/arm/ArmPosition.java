package com.swrobotics.robot.subsystems.arm;

import com.swrobotics.lib.net.NTBoolean;
import com.swrobotics.lib.net.NTDoubleArray;
import com.swrobotics.lib.net.NTEntry;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.mathlib.MathUtil;
import com.swrobotics.mathlib.Vec2d;

public final class ArmPosition {
    public static final class NT extends NTEntry<ArmPosition> {
        private final NTEntry<double[]> value;
        private final NTEntry<Boolean> intermediate;
        private final ArmPosition defPos;

        public NT(String path, double defX, double defY, Angle defWrist, boolean defIntermediate) {
            value = new NTDoubleArray(path + "/Position", defX, defY, defWrist.ccw().deg());
            intermediate = new NTBoolean(path + "/Use Intermediate", defIntermediate);
            defPos = new ArmPosition(new Vec2d(defX, defY), defWrist);
        }

        public NT(String path, ArmPosition defPos, boolean defIntermediate) {
            this(path, defPos.axisPos.x, defPos.axisPos.y, defPos.wristAngle, defIntermediate);
        }

        @Override
        public ArmPosition get() {
            double[] coords = value.get();
            if (coords.length < 3) {
                set(defPos);
            }
            return new ArmPosition(new Vec2d(coords[0], coords[1]), CCWAngle.deg(coords[2]));
        }

        public void set(ArmPosition tx) {
            value.set(new double[] {tx.axisPos.x, tx.axisPos.y, tx.wristAngle.ccw().deg()});
        }

        public boolean useIntermediate() {
            return intermediate.get();
        }

        @Override
        public NTEntry<ArmPosition> setPersistent() {
            value.setPersistent();
            intermediate.setPersistent();
            return this;
        }

        @Override
        public void registerChangeListeners(Runnable fireFn) {
            value.registerChangeListeners(fireFn);
            intermediate.registerChangeListeners(fireFn);
        }
    }

    // Position in meters of the wrist's axis of rotation along the right side plane
    public final Vec2d axisPos;

    // Angle of the wrist relative to horizontal
    public final Angle wristAngle;

    public ArmPosition(Vec2d axisPos, Angle wristAngle) {
        this.axisPos = axisPos;
        this.wristAngle = wristAngle;
    }

    private static final double MIN_BOTTOM_ANGLE = Math.toRadians(25);
    private static final double MAX_BOTTOM_ANGLE = Math.toRadians(180 - 55);

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
        if (len >= lengthA + lengthB - MathUtil.EPSILON) {
            // Target is too far away, it's impossible for the arm to reach
            return null;
        }
        if (len < Math.abs(lengthA - lengthB)) {
            // Target is too close, it's impossible to reach
            return null;
        }

        double angleAL =
                Math.acos(
                        (lengthA * lengthA + len * len - lengthB * lengthB) / (2 * lengthA * len));
        double angleAB =
                Math.acos(
                        (lengthA * lengthA + lengthB * lengthB - len * len)
                                / (2 * lengthA * lengthB));

        double angle1Rad = targetAngle + flip * angleAL;
        if (angle1Rad < MIN_BOTTOM_ANGLE || angle1Rad > MAX_BOTTOM_ANGLE)
            return null;

        Angle angle1 = CCWAngle.rad(angle1Rad);
        Angle angle2 = angle1.ccw().add(CCWAngle.rad(flip * angleAB - Math.PI)).wrapDeg(-270, 90);

        return new ArmPose(angle1, angle2, wristAngle);
    }

    @Override
    public String toString() {
        return "ArmPosition{" +
                "axisPos=" + axisPos +
                ", wristAngle=" + wristAngle +
                '}';
    }
}

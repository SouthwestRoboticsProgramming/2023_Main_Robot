package com.swrobotics.lib.gyro;

import com.ctre.phoenix.sensors.Pigeon2;
import com.ctre.phoenix.sensors.PigeonIMU;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.mathlib.Vec3d;

/**
 * Gyroscope implementation for the Pigeon IMU gyroscope from CTRE.
 */
public final class PigeonGyroscope extends Gyroscope {
    private final PigeonIMU pigeon;

    /**
     * Creates a new instance of the Pigeon2 gyroscope with a specific CAN
     * ID on the RoboRIO CAN bus.
     *
     * @param canId CAN ID of the Pigeon2
     */
    public PigeonGyroscope(int canId) {
        pigeon = new PigeonIMU(canId);
    }

    @Override
    public void calibrate() {
        // Pigeon automatically calibrates when not moving
    }

    @Override
    protected Angle getRawAngle() {
        return CCWAngle.deg(pigeon.getYaw());
    }

    // Rotation around Pigeon X axis
    public Angle getPitch() {
        return CCWAngle.deg(pigeon.getPitch());
    }

    // Rotation around Pigeon Y axis
    public Angle getRoll() {
        return CCWAngle.deg(pigeon.getRoll());
    }

    // TODO: make it work
//    /**
//     * Gets the vector aligned with gravity (i.e. pointing straight down).
//     */
//    public Vec3d getGravityVector() {
//        double[] xyz = new double[3];
//        pigeon.get(xyz);
//        return new Vec3d(xyz[0], xyz[1], xyz[2]).normalize();
//    }
}

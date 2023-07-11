package com.swrobotics.lib.gyro;

import com.ctre.phoenix.sensors.Pigeon2;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.mathlib.Vec3d;

/**
 * Gyroscope implementation for the Pigeon2 gyroscope from CTRE.
 */
public final class PigeonGyroscope extends Gyroscope {
    private final Pigeon2 pigeon;

    /**
     * Creates a new instance of the Pigeon2 gyroscope with a specific CAN
     * ID on the RoboRIO CAN bus.
     *
     * @param canId CAN ID of the Pigeon2
     */
    public PigeonGyroscope(int canId) {
        this(canId, "");
    }

    /**
     * Creates a new instance of the Pigeon2 gyroscope with a specified CAN ID
     * on a specified CAN bus.
     *
     * @param canId CAN ID of the Pigeon2 on the bus
     * @param canBus name of the CAN bus the Pigeon2 is connected to
     */
    public PigeonGyroscope(int canId, String canBus) {
        pigeon = new Pigeon2(canId, canBus);
    }

    @Override
    public void calibrate() {
        // Pigeon automatically calibrates when not moving
    }

    @Override
    protected Angle getRawAngle() {
        return CCWAngle.deg(pigeon.getYaw());
    }

    /**
     * Gets the vector aligned with gravity (i.e. pointing straight down).
     */
    public Vec3d getGravityVector() {
        double[] xyz = new double[3];
        pigeon.getGravityVector(xyz);
        return new Vec3d(xyz[0], xyz[1], xyz[2]).normalize();
    }
}

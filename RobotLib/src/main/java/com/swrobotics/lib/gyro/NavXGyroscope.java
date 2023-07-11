package com.swrobotics.lib.gyro;

import com.kauailabs.navx.frc.AHRS;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;

import edu.wpi.first.wpilibj.SPI;

/** Gyroscope implementation for the NavX2 IMU from Kauai Labs. */
public class NavXGyroscope extends Gyroscope {
    private final AHRS navx;

    /**
     * Creates a new instance of the NavX gyroscope on a specified SPI port. It is not recommended
     * to have multiple instances of this class for the same NavX device.
     *
     * @param port SPI port the NaxX is connected to
     */
    public NavXGyroscope(SPI.Port port) {
        navx = new AHRS(port);
    }

    @Override
    public void calibrate() {
        navx.calibrate();
    }

    @Override
    protected Angle getRawAngle() {
        return CCWAngle.rad(navx.getRotation2d().getRadians());
    }

    // TODO: Angle-ify
    // TODO: There could also be an interface for 3-axis gyroscopes
    public double getPitch() {
        return navx.getPitch();
    }

    public double getRoll() {
        return navx.getRoll();
    }
}

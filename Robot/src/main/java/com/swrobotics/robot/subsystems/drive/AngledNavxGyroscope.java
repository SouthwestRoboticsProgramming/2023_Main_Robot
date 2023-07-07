package com.swrobotics.robot.subsystems.drive;

import org.littletonrobotics.junction.Logger;

import com.swrobotics.lib.gyro.NavXGyroscope;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CWAngle;

import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.wpilibj.SPI.Port;

public class AngledNavxGyroscope extends NavXGyroscope {

    private static final double GYRO_ROLL_DEGREES = 40.25;//57.12660124; // FIXME: Measure
    private static final Rotation3d ACTUAL_ROTATION = new Rotation3d(Math.toRadians(-GYRO_ROLL_DEGREES), 0, 0);
    
    public AngledNavxGyroscope(Port port) {
        super(port);
    }

    @Override
    public double getPitch() {
        return getRotation3d().getZ();
    }

    @Override
    public double getRoll() {
        double angle = -super.getRoll() - GYRO_ROLL_DEGREES;
        // Logger.getInstance().recordOutput("GyroTest/OutputRoll", angle);
        return angle;
    }

    @Override
    protected Angle getRawAngle() {
        return CWAngle.deg(getRotation3d().getY());
    }

    private Rotation3d getRotation3d() {
        Logger.getInstance().recordOutput("GyroTest/RawPitch", super.getPitch());
        Logger.getInstance().recordOutput("GyroTest/RawYaw", super.getRawAngle().cw().deg());
        Logger.getInstance().recordOutput("GyroTest/RawRoll", super.getRoll());
        Rotation3d rotation = new Rotation3d(getRoll(), Math.toRadians(super.getPitch()), super.getRawAngle().ccw().rad()).rotateBy(ACTUAL_ROTATION);
        Logger.getInstance().recordOutput("GyroTest/OutputRoll", Math.toDegrees(rotation.getX()));
        Logger.getInstance().recordOutput("GyroTest/OutputPitch", Math.toDegrees(rotation.getY()));
        Logger.getInstance().recordOutput("GyroTest/OutputYaw", Math.toDegrees(rotation.getZ()));
        return rotation;
    }
}

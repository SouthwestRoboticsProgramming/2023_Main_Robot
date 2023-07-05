package com.swrobotics.robot.subsystems.drive;

import org.littletonrobotics.junction.Logger;

import com.swrobotics.lib.gyro.NavXGyroscope;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CWAngle;

import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.wpilibj.SPI.Port;

public class AngledNavxGyroscope extends NavXGyroscope {

    private static final double GYRO_ROLL_DEGREES = 50.0; // FIXME: Measure
    
    public AngledNavxGyroscope(Port port) {
        super(port);
    }

    @Override
    protected Angle getRawAngle() {
        Logger.getInstance().recordOutput("GyroTest/RawPitch", getPitch());
        Logger.getInstance().recordOutput("GyroTest/RawYaw", super.getRawAngle().cw().deg());
        Logger.getInstance().recordOutput("GyroTest/RawRoll", getRoll());
        Rotation3d rotation = new Rotation3d(getRoll(), getPitch(), super.getRawAngle().cw().deg());
        rotation.rotateBy(new Rotation3d(-GYRO_ROLL_DEGREES, 0, 0));
        Logger.getInstance().recordOutput("GyroTest/OutputX", rotation.getX());
        Logger.getInstance().recordOutput("GyroTest/OutputY", rotation.getY());
        Logger.getInstance().recordOutput("GyroTest/OutputZ", rotation.getZ());
        
        return CWAngle.deg(rotation.getY());
    }
}

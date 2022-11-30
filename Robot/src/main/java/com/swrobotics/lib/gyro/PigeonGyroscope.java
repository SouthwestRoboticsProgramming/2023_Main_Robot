package com.swrobotics.lib.gyro;

import com.ctre.phoenix.sensors.PigeonIMU;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;

public class PigeonGyroscope extends Gyroscope {

    private final PigeonIMU gyro;

    public PigeonGyroscope(int id) {
        gyro = new PigeonIMU(id);
    }

    @Override
    protected Angle getRawAngleImpl() {
        return CCWAngle.deg(gyro.getFusedHeading());
    }
    
}

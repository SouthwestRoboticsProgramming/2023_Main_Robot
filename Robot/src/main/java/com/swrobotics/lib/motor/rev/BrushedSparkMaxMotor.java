package com.swrobotics.lib.motor.rev;

import com.revrobotics.CANSparkMaxLowLevel;

public final class BrushedSparkMaxMotor extends SparkMaxMotor {
    public BrushedSparkMaxMotor(int canID) {
        super(canID, CANSparkMaxLowLevel.MotorType.kBrushed);
        // No encoder by default
    }
}

package com.swrobotics.lib.motor.rev;

import com.revrobotics.CANSparkMaxLowLevel;

/** Motor implementation for a Spark MAX controlling a brushed motor. */
public final class BrushedSparkMaxMotor extends SparkMaxMotor {
    public BrushedSparkMaxMotor(int canID) {
        super(canID, CANSparkMaxLowLevel.MotorType.kBrushed);
        // No encoder by default
    }
}

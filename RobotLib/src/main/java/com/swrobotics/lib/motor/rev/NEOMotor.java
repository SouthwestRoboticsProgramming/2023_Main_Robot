package com.swrobotics.lib.motor.rev;

import com.revrobotics.*;

/** Motor implementation for a Spark MAX controlling a brushless NEO motor. */
public final class NEOMotor extends SparkMaxMotor {
    public NEOMotor(int canID) {
        super(canID, CANSparkMaxLowLevel.MotorType.kBrushless);

        // Configure for integrated brushless encoder
        withPrimaryEncoder(SparkMaxRelativeEncoder.Type.kHallSensor, 42);
    }
}

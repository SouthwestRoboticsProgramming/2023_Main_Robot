package com.swrobotics.lib.motor.rev;

import com.swrobotics.lib.motor.PWMMotor;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;

public final class PWMSparkMaxMotor extends PWMMotor {
    public PWMSparkMaxMotor(int pwmPort) {
        super(new PWMSparkMax(pwmPort));
    }
}

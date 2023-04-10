package com.swrobotics.lib.motor.rev;

import com.swrobotics.lib.motor.PWMMotor;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;

/**
 * Motor implementation for a Spark MAX motor connected via PWM. The motor
 * type must be configured using the REV Hardware Client over USB.
 */
public final class PWMSparkMaxMotor extends PWMMotor {
    public PWMSparkMaxMotor(int pwmPort) {
        super(new PWMSparkMax(pwmPort));
    }
}

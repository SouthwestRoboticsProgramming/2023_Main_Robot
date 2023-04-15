package com.swrobotics.lib.motor.ctre;

import com.swrobotics.lib.motor.PWMMotor;

import edu.wpi.first.wpilibj.motorcontrol.PWMTalonFX;

/** Motor implementation for a Talon FX connected via PWM. */
public final class PWMTalonFXMotor extends PWMMotor {
    public PWMTalonFXMotor(int pwmPort) {
        super(new PWMTalonFX(pwmPort));
    }
}

package com.swrobotics.lib.motor.ctre;

import com.swrobotics.lib.motor.PWMMotor;

import edu.wpi.first.wpilibj.motorcontrol.PWMTalonSRX;

/** Motor implementation for a Talon SRX connected via PWM. */
public final class PWMTalonSRXMotor extends PWMMotor {
    public PWMTalonSRXMotor(int pwmPort) {
        super(new PWMTalonSRX(pwmPort));
    }
}

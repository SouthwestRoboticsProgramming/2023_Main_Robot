package com.swrobotics.lib.motor.ctre;

import com.swrobotics.lib.motor.PWMMotor;
import edu.wpi.first.wpilibj.motorcontrol.PWMTalonFX;

public final class PWMTalonFXMotor extends PWMMotor {
    public PWMTalonFXMotor(int pwmPort) {
        super(new PWMTalonFX(pwmPort));
    }
}

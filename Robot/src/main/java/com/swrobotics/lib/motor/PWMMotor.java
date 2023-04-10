package com.swrobotics.lib.motor;

import edu.wpi.first.wpilibj.motorcontrol.PWMMotorController;

/**
 * Motor implementation for a PWM motor controller.
 */
public class PWMMotor implements Motor {
    private final PWMMotorController ctrl;

    public PWMMotor(PWMMotorController ctrl) {
        this.ctrl = ctrl;
    }

    @Override
    public void setPercentOut(double percent) {
        ctrl.set(percent);
    }

    @Override
    public void setInverted(boolean inverted) {
        ctrl.setInverted(inverted);
    }
}

package com.swrobotics.lib.motor.ctre;

import com.swrobotics.lib.motor.PWMMotor;
import edu.wpi.first.wpilibj.motorcontrol.PWMVictorSPX;

/** Motor implementation for a Victor SPX connected via PWM. */
public final class PWMVictorSPXMotor extends PWMMotor {
  public PWMVictorSPXMotor(int pwmPort) {
    super(new PWMVictorSPX(pwmPort));
  }
}

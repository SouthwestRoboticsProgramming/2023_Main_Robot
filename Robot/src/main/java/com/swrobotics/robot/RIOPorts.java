package com.swrobotics.robot;

/**
 * Class to define all IO ports used on the RoboRIO in one place, so it is easy to verify all the
 * wires are plugged in correctly
 */
public final class RIOPorts {
  public static final int LIGHTS_PWM = 0;
  public static final int INTAKE_PWM = 1; // Blue tape

  public static final int ARNOLD_LEFT_PWM = 2;
  public static final int ARNOLD_RIGHT_PWM = 3;

  public static final int LIGHT_SERVO_PWM = 4;

  public static final int ARM_DETECT_DIO = 2;

  public static final int BLUE_LIGHT_DIO = 3;
  public static final int YELLOW_LIGHT_DIO = 4;

  private RIOPorts() {
    throw new AssertionError();
  }
}

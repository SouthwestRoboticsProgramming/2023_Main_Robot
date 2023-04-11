package com.swrobotics.lib.motor.ctre;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.can.TalonFXConfiguration;

/** Motor implementation for a Talon FX connected via CAN. */
public final class TalonFXMotor extends TalonMotor {
  private static TalonFX createFX(int canID, String canBus) {
    TalonFXConfiguration config = new TalonFXConfiguration();

    // Set initial PIDF to zero so the motor won't move by default
    config.slot0.kP = 0;
    config.slot0.kI = 0;
    config.slot0.kD = 0;
    config.slot0.kF = 0;

    // Select the motor's integrated encoder
    config.primaryPID.selectedFeedbackSensor = FeedbackDevice.IntegratedSensor;

    TalonFX fx = new TalonFX(canID, canBus);
    fx.configAllSettings(config);
    return fx;
  }

  /**
   * Creates a new instance for a Talon FX on the RoboRIO CAN bus.
   *
   * @param canID can ID of the talon
   */
  public TalonFXMotor(int canID) {
    this(canID, "");
  }

  /**
   * Creates a new instance for a Talon FX on a specified CAN bus.
   *
   * @param canID can ID of the talon
   * @param canBus can bus the talon is connected to
   */
  public TalonFXMotor(int canID, String canBus) {
    super(createFX(canID, canBus));
    enableIntegratedEncoder(2048);
  }

  @Override
  protected boolean canSetSensorPhase() {
    return false;
  }
}

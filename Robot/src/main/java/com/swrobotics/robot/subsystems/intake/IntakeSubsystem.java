package com.swrobotics.robot.subsystems.intake;

import com.swrobotics.lib.motor.Motor;
import com.swrobotics.lib.motor.rev.PWMSparkMaxMotor;
import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.lib.schedule.SwitchableSubsystemBase;
import com.swrobotics.robot.RIOPorts;

public final class IntakeSubsystem extends SwitchableSubsystemBase {
  private static final NTDouble CONE_HOLD = new NTDouble("Intake/Cone Hold", 0.1);
  private static final NTDouble CUBE_HOLD = new NTDouble("Intake/Cube Hold", -0.1);

  private final Motor motor;

  private GamePiece expectedPiece;
  private boolean running;

  public IntakeSubsystem() {
    motor = new PWMSparkMaxMotor(RIOPorts.INTAKE_PWM);

    expectedPiece = GamePiece.CUBE;
    running = false;
  }

  public void setExpectedPiece(GamePiece expectedPiece) {
    this.expectedPiece = expectedPiece;

    // Become more stopped
    if (!running) stop();
  }

  public void run() {
    if (isEnabled()) {
      motor.setPercentOut(expectedPiece.getIntakeOutput());
      running = true;
    }
  }

  public void eject() {
    if (isEnabled()) {
      motor.setPercentOut(-expectedPiece.getOuttakeOutput());
      running = true;
    }
  }

  public void stop() {
    motor.setPercentOut(expectedPiece == GamePiece.CONE ? (CONE_HOLD.get()) : CUBE_HOLD.get());
    running = false;
  }

  @Override
  public void onDisable() {
    motor.stop();
  }
}

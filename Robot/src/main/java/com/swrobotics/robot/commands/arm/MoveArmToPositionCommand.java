package com.swrobotics.robot.commands.arm;

import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.subsystems.arm.ArmSubsystem;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.CommandBase;
import java.util.function.Supplier;

public final class MoveArmToPositionCommand extends CommandBase {
  private final ArmSubsystem arm;
  private final Supplier<Translation2d> target;

  public MoveArmToPositionCommand(RobotContainer robot, Translation2d target) {
    this(robot, () -> target);
  }

  public MoveArmToPositionCommand(RobotContainer robot, Supplier<Translation2d> target) {
    arm = robot.arm;
    this.target = target;

    addRequirements(arm);
  }

  @Override
  public void execute() {
    arm.setTargetPosition(target.get());
  }

  @Override
  public boolean isFinished() {
    return arm.isInTolerance();
  }
}

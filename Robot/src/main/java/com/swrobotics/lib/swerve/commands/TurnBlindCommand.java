package com.swrobotics.lib.swerve.commands;

import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.subsystems.drive.DrivetrainSubsystem;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class TurnBlindCommand extends CommandBase {
  private final DrivetrainSubsystem drive;

  private final Rotation2d rotation;

  public TurnBlindCommand(RobotContainer robot, double omegaRadiansPerSecond) {
    drive = robot.drivetrainSubsystem;

    rotation = new Rotation2d(omegaRadiansPerSecond);
  }

  @Override
  public void execute() {
    drive.setTargetRotation(rotation);
  }
}

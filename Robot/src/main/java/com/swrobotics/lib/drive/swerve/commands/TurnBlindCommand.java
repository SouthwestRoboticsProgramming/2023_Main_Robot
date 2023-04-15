package com.swrobotics.lib.drive.swerve.commands;

import com.swrobotics.lib.drive.swerve.SwerveDrive;
import com.swrobotics.robot.RobotContainer;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class TurnBlindCommand extends CommandBase {
    private final SwerveDrive drive;

    private final Rotation2d rotation;

    public TurnBlindCommand(RobotContainer robot, double omegaRadiansPerSecond) {
        drive = robot.swerveDrive;

        rotation = new Rotation2d(omegaRadiansPerSecond);
    }

    @Override
    public void execute() {
        drive.addRotation(rotation);
    }
}

package com.swrobotics.robot.commands;

import com.swrobotics.lib.drive.swerve.commands.DriveBlindCommand;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.lib.drive.swerve.SwerveDrive;
import com.swrobotics.robot.subsystems.drive.DrivetrainSubsystem;

import java.util.function.Supplier;

public class StartBalanceCommand extends DriveBlindCommand {

    private final DrivetrainSubsystem drive;

    public StartBalanceCommand(
            RobotContainer robot,
            Supplier<Angle> direction,
            double velocityMetersPerSecond,
            boolean robotRelative) {
        super(robot, direction, velocityMetersPerSecond, robotRelative);
        drive = robot.swerveDrive;
    }

    @Override
    public boolean isFinished() {
        // Stop the command when the robot is at an angle
        var tilt = drive.getTiltAsTranslation();
        double magnitude = tilt.getNorm();
        return Math.abs(magnitude) > 4.0;
    }
}

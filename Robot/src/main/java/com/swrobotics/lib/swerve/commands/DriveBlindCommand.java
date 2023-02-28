package com.swrobotics.lib.swerve.commands;

import com.swrobotics.mathlib.Angle;

import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.subsystems.drive.DrivetrainSubsystem;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class DriveBlindCommand extends CommandBase {
    private final DrivetrainSubsystem drive;

    private final Translation2d translation;
    private Translation2d currentTranslation;
    private final boolean robotRelative;

    public DriveBlindCommand(RobotContainer robot, Angle direction, double velocityMetersPerSecond, boolean robotRelative) {
        drive = robot.drivetrainSubsystem;

        this.robotRelative = robotRelative;

        Rotation2d directionWPI = direction.ccw().rotation2d();
        Translation2d justVelocity = new Translation2d(velocityMetersPerSecond, 0);
        Translation2d withDirection = justVelocity.rotateBy(directionWPI);
        translation = withDirection;
    }

    @Override
    public void initialize() {
        // Make it relative to the current angle
        if (robotRelative) {
            currentTranslation = translation.rotateBy(drive.getPose().getRotation());
        } else {
            currentTranslation = translation;
        }
    }

    @Override
    public void execute() {
        drive.setTargetTranslation(currentTranslation, true);
        System.out.println("Drive blind: translation " + translation);
    }
}

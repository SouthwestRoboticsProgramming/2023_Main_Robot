package com.swrobotics.lib.swerve.commands;

import com.swrobotics.mathlib.Angle;

import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.subsystems.DrivetrainSubsystem;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class DriveBlindCommand extends CommandBase {
    private final DrivetrainSubsystem drive;

    private Translation2d translation;
    private final boolean robotRelative;

    public DriveBlindCommand(RobotContainer robot, Angle direction, double velocityMetersPerSecond, boolean robotRelative) {
        drive = robot.m_drivetrainSubsystem;

        this.robotRelative = robotRelative;

        Rotation2d directionWPI = direction.ccw().rotation2d();
        Translation2d justVelocity = new Translation2d(velocityMetersPerSecond, 0);
        Translation2d withDirection = justVelocity.rotateBy(directionWPI);
        translation = withDirection;

        addRequirements(drive.DRIVE_SUBSYSTEM);

    }

    @Override
    public void initialize() {
        // Make it relative to the current angle
        if (robotRelative) {
            translation = translation.rotateBy(drive.getGyroscopeRotation());
        }
    }

    @Override
    public void execute() {
        // Add a ChassisSpeeds with just translation to the total
        ChassisSpeeds output = ChassisSpeeds.fromFieldRelativeSpeeds(translation.getX(), translation.getY(), 0, drive.getGyroscopeRotation());

        drive.combineChassisSpeeds(output);
    }
}

package com.swrobotics.lib.swerve.commands;

import com.swrobotics.mathlib.Angle;

import com.swrobotics.lib.commands.TimedCommand;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.subsystems.DrivetrainSubsystem;
import com.swrobotics.robot.subsystems.Lights;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;

import com.swrobotics.robot.subsystems.Lights.IndicatorMode;

public class DriveBlindCommand extends TimedCommand {
    private final DrivetrainSubsystem drive;
    private final Lights lights;

    private Translation2d translation;
    private final boolean robotRelative;

    public DriveBlindCommand(RobotContainer robot, Angle direction, double velocityMetersPerSecond, double runtimeSeconds, boolean robotRelative) {
        super(runtimeSeconds);
        drive = robot.m_drivetrainSubsystem;
        lights = robot.m_lights;

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

        super.initialize();
        lights.set(IndicatorMode.IN_PROGRESS);
    }

    @Override
    public void execute() {
        // Add a ChassisSpeeds with just translation to the total
        ChassisSpeeds output = ChassisSpeeds.fromFieldRelativeSpeeds(translation.getX(), translation.getY(), 0, drive.getGyroscopeRotation());

        drive.combineChassisSpeeds(output);
    }

    @Override
    public boolean isFinished() {
        return super.isFinished();
    }

    @Override
    public void end(boolean interrupted) {
        lights.set(IndicatorMode.SUCCESS);
        super.end(interrupted);
    }
}

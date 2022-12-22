package com.swrobotics.lib.swerve.commands;

import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CoordinateConversions;

import com.swrobotics.lib.commands.TimedCommand;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.subsystems.DrivetrainSubsystem;
import com.swrobotics.robot.subsystems.Lights;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.CommandBase;

import com.swrobotics.robot.subsystems.Lights.IndicatorMode;

public class DriveBlindCommand extends TimedCommand {
    private final DrivetrainSubsystem drive;
    private final Lights lights;

    private final ChassisSpeeds output;

    public DriveBlindCommand(RobotContainer robot, Angle direction, double velocityMetersPerSecond, double runtimeSeconds, boolean robotRelative) {
        super(runtimeSeconds);
        drive = robot.m_drivetrainSubsystem;
        lights = robot.m_lights;

        Rotation2d directionWPI = CoordinateConversions.toWPIAngle(direction);
        Translation2d translation = new Translation2d().times(velocityMetersPerSecond);
        translation.rotateBy(directionWPI);

        if (robotRelative) {
            output = new ChassisSpeeds(translation.getX(), translation.getY(), 0); // FIXME: Two-part chassis speeds to allow for spinning at the same time
        } else {
            output = ChassisSpeeds.fromFieldRelativeSpeeds(translation.getX(), translation.getY(), 0, drive.getGyroscopeRotation()); // FIXME: Update with new gyro rotation
        }
    }

    @Override
    public void initialize() {
        lights.set(IndicatorMode.IN_PROGRESS);
    }

    @Override
    public void execute() {
        drive.setChassisSpeeds(output);
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

package com.swrobotics.lib.drive.swerve.commands;

import com.swrobotics.mathlib.Angle;

import com.swrobotics.robot.RobotContainer;
import com.swrobotics.lib.drive.swerve.SwerveDrive;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.CommandBase;

import java.util.function.Supplier;

public class DriveBlindCommand extends CommandBase {
    private final SwerveDrive drive;

    private final Supplier<Angle> direction;
    private final double velocityMetersPerSecond;
    private Translation2d currentTranslation;
    private final boolean robotRelative;

    public DriveBlindCommand(RobotContainer robot, Supplier<Angle> direction, double velocityMetersPerSecond, boolean robotRelative) {
        drive = robot.swerveDrive;

        this.direction = direction;
        this.velocityMetersPerSecond = velocityMetersPerSecond;
        this.robotRelative = robotRelative;
    }

    @Override
    public void initialize() {
        Rotation2d directionWPI = direction.get().ccw().rotation2d();
        Translation2d justVelocity = new Translation2d(velocityMetersPerSecond, 0);
        Translation2d translation = justVelocity.rotateBy(directionWPI);

        // Make it relative to the current angle
        if (robotRelative) {
            currentTranslation = translation.rotateBy(drive.getPose().getRotation());
        } else {
            currentTranslation = translation;
        }
    }

    @Override
    public void execute() {
        drive.addTranslation(currentTranslation, true);
    }
}

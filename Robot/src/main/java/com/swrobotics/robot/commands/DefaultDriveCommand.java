package com.swrobotics.robot.commands;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj2.command.CommandBase;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

import com.swrobotics.robot.subsystems.drive.DrivetrainSubsystem;

public class DefaultDriveCommand extends CommandBase {
    private static final double SLOW_MODE_MULTIPLIER = 0.5;
    private static final double FAST_MODE_MULTIPLIER = 2.0;

    private final DrivetrainSubsystem drivetrainSubsystem;

    private final DoubleSupplier translationXSupplier;
    private final DoubleSupplier translationYSupplier;
    private final DoubleSupplier rotationSupplier;

    private final BooleanSupplier slowModeSupplier;
    private final BooleanSupplier fastModeSupplier;

    public DefaultDriveCommand(DrivetrainSubsystem drive,
                               DoubleSupplier translationXSupplier,
                               DoubleSupplier translationYSupplier,
                               DoubleSupplier rotationSupplier,
                               BooleanSupplier slowModeSupplier,
                               BooleanSupplier fastModeSupplier) {
        this.drivetrainSubsystem = drive;
        this.translationXSupplier = translationXSupplier;
        this.translationYSupplier = translationYSupplier;
        this.rotationSupplier = rotationSupplier;
        this.slowModeSupplier = slowModeSupplier;
        this.fastModeSupplier = fastModeSupplier;

        addRequirements(drive);
    }

    @Override
    public void execute() {
        // You can use `new ChassisSpeeds(...)` for robot-oriented movement instead of field-oriented movement

        double multiplier = 1.0;

        if (slowModeSupplier.getAsBoolean()) {
            multiplier *= SLOW_MODE_MULTIPLIER;
        }

        if (fastModeSupplier.getAsBoolean()) {
            multiplier *= FAST_MODE_MULTIPLIER;
        }

        double x = translationXSupplier.getAsDouble() * multiplier;
        double y = translationYSupplier.getAsDouble() * multiplier;

        if (RobotBase.isSimulation()) {
            drivetrainSubsystem.setChassisSpeeds(
                ChassisSpeeds.fromFieldRelativeSpeeds(
                        x,
                        y,
                        rotationSupplier.getAsDouble(),
                        drivetrainSubsystem.getPose().getRotation()
                )
            );
            return;
        }

        drivetrainSubsystem.setChassisSpeeds(
                ChassisSpeeds.fromFieldRelativeSpeeds(
                        x,
                        y,
                        rotationSupplier.getAsDouble(),
                        drivetrainSubsystem.getGyroscopeRotation()
                )
        );
    }

    @Override
    public void end(boolean interrupted) {
        drivetrainSubsystem.setChassisSpeeds(new ChassisSpeeds(0.0, 0.0, 0.0));
    }
}
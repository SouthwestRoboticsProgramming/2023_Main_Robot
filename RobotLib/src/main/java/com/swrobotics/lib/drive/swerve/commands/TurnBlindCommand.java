package com.swrobotics.lib.drive.swerve.commands;

import com.swrobotics.lib.drive.swerve.SwerveDrive;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class TurnBlindCommand extends CommandBase {
    private final SwerveDrive drive;

    private final Rotation2d rotation;

    public TurnBlindCommand(SwerveDrive drive, double omegaRadiansPerSecond) {
        this.drive = drive;

        rotation = new Rotation2d(omegaRadiansPerSecond);
    }

    @Override
    public void execute() {
        drive.addRotation(rotation);
    }
}

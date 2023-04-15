package com.swrobotics.robot.commands;

import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.robot.input.Input;
import com.swrobotics.robot.subsystems.drive.DrivetrainSubsystem;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class DefaultDriveCommand extends CommandBase {
    private final DrivetrainSubsystem drive;
    private final Input input;

    public DefaultDriveCommand(DrivetrainSubsystem drive, Input input) {
        this.drive = drive;
        this.input = input;
        addRequirements(drive);
    }

    @Override
    public void execute() {
        Vec2d translation = input.getDriveTranslation();
        Angle rotation = input.getDriveRotation();

        double x = translation.x;
        double y = translation.y;
        double rotationCCW = rotation.ccw().rad();

        Rotation2d gyro = drive.getPose().getRotation();
        ChassisSpeeds speeds;
        if (input.isRobotRelative()) {
            speeds = new ChassisSpeeds(x, y, rotationCCW);
        } else if (DriverStation.getAlliance() == DriverStation.Alliance.Blue) {
            speeds = ChassisSpeeds.fromFieldRelativeSpeeds(x, y, rotationCCW, gyro);
        } else {
            speeds = ChassisSpeeds.fromFieldRelativeSpeeds(-x, -y, rotationCCW, gyro);
        }

        drive.addChassisSpeeds(speeds);
    }

    @Override
    public void end(boolean interrupted) {
        drive.addChassisSpeeds(new ChassisSpeeds(0.0, 0.0, 0.0));
    }
}

package com.swrobotics.robot.commands;

import com.swrobotics.lib.drive.swerve.StopPosition;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.config.NTData;
import com.swrobotics.robot.subsystems.drive.DrivetrainSubsystem;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class AutoBalanceCommand extends CommandBase {
    private final DrivetrainSubsystem drive;

    private final StopPosition firstStopPosition;

    public AutoBalanceCommand(RobotContainer robot) {
        drive = robot.swerveDrive;

        firstStopPosition = drive.getStopPosition();
    }

    @Override
    public void initialize() {
        drive.setStopPosition(StopPosition.CROSS); // Allow it to hold position
    }

    @Override
    public void execute() {
        // FIXME: Why * -1?
        var tilt = drive.getTiltAsTranslation().times(-1);
        double magnitude = tilt.getNorm();
        if (Math.abs(magnitude) < NTData.BALANCE_STOP_TOL.get()) {
            return;
        }

        Rotation2d rotation = new Rotation2d(tilt.getX(), tilt.getY());

        double adjustmentAmount = NTData.BALANCE_ADJUST_AMT.get();
        Translation2d output = new Translation2d(adjustmentAmount, rotation);
        drive.addChassisSpeeds(new ChassisSpeeds(output.getX(), output.getY(), 0.0));
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public void end(boolean interrupted) {
        drive.setStopPosition(firstStopPosition); // Set it back to how it was
    }
}

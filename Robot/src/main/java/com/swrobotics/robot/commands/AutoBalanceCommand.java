package com.swrobotics.robot.commands;

import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.subsystems.drive.DrivetrainSubsystem;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.CommandBase;
import com.swrobotics.robot.subsystems.drive.DrivetrainSubsystem.StopPosition;

public class AutoBalanceCommand extends CommandBase {
    private static final NTDouble ADJUST_AMOUNT = new NTDouble("Auto Balance/Adjust Amount", -0.3);

    private final DrivetrainSubsystem drive;

    private final StopPosition firstStopPosition;

    public AutoBalanceCommand(RobotContainer robot) {
        drive = robot.drivetrainSubsystem;
        // pid = new PIDController(KP.get(), 1000.0, 0.0);

        // KP.onChange(() -> pid.setP(KP.get()));

        firstStopPosition = drive.getStopPosition();
    }

    @Override
    public void initialize() {
        drive.setStopPosition(StopPosition.CROSS); // Allow it to hold position
    }

    @Override
    public void execute() {
        var tilt = drive.getTiltAsTranslation().times(-1);
        double magnitude = tilt.getNorm();
        System.out.println("M: " + magnitude);
        if (Math.abs(magnitude) < 1.5) {
            drive.setChassisSpeeds(new ChassisSpeeds());
            return;
        }

        Rotation2d rotation = new Rotation2d(tilt.getX(), tilt.getY());

        // double adjustmentAmount = pid.calculate(magnitude, 0.0);
        double adjustmentAmount = -0.375;//ADJUST_AMOUNT.get();
        Translation2d output = new Translation2d(adjustmentAmount, rotation);
        drive.setChassisSpeeds(new ChassisSpeeds(output.getX(), output.getY(), 0.0));
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

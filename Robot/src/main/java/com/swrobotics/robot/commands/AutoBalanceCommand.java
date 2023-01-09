package com.swrobotics.robot.commands;

import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.subsystems.DrivetrainSubsystem;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.CommandBase;
import com.swrobotics.robot.subsystems.DrivetrainSubsystem.StopPosition;

public class AutoBalanceCommand extends CommandBase {

    private static final NTDouble KP = new NTDouble("Drive/Balance/kP", 0.1);

    private final DrivetrainSubsystem drive;
    private final PIDController pid;

    private final StopPosition firstStopPosition;

    public AutoBalanceCommand(RobotContainer robot) {
        drive = robot.drivetrainSubsystem;
        pid = new PIDController(KP.get(), 0.0, 0.0);

        KP.onChange(() -> pid.setP(KP.get()));

        firstStopPosition = drive.getStopPosition();

        addRequirements(drive.DRIVE_SUBSYSTEM);
    }

    @Override
    public void initialize() {
        drive.setStopPosition(StopPosition.CROSS); // Allow it to hold position
    }

    @Override
    public void execute() {
        System.out.println(drive.getTiltAsTranslation());
        var tilt = drive.getTiltAsTranslation().times(-1);
        double magnitude = tilt.getNorm();
        Rotation2d rotation = new Rotation2d(tilt.getX(), tilt.getY());

        double adjustmentAmount = pid.calculate(magnitude, 0.0);
        Translation2d output = new Translation2d(adjustmentAmount, rotation);
        drive.setChassisSpeeds(new ChassisSpeeds(output.getX(), output.getY(), 0.0));
    }

    @Override
    public void end(boolean interrupted) {
        drive.setStopPosition(firstStopPosition); // Set it back to how it was
    }

}

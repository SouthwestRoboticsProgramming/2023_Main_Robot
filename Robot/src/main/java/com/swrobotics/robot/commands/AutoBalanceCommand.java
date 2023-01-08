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
    private static final NTDouble TOLERANCE = new NTDouble("Drive/Balance/Tolerance Degrees", 2.5);

    private final DrivetrainSubsystem drive;
    private final PIDController pid;

    public AutoBalanceCommand(RobotContainer robot) {
        drive = robot.m_drivetrainSubsystem;
        pid = new PIDController(KP.get(), 0.0, 0.0);
        pid.setTolerance(TOLERANCE.get());

        KP.onChange(() -> pid.setP(KP.get()));
        TOLERANCE.onChange(() -> pid.setTolerance(TOLERANCE.get()));
        
    }

    @Override
    public void initialize() {
        
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
    public boolean isFinished() {
        return pid.atSetpoint();
    }

    @Override
    public void end(boolean interrupted) {
        drive.setStopPosition(StopPosition.CROSS); // Allow it to hold position
        drive.setBrakeMode(true); // Hold position even more
    }

}

package com.swrobotics.lib.swerve.commands;

import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.subsystems.DrivetrainSubsystem;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class TurnBlindCommand extends CommandBase {
    private final DrivetrainSubsystem drive;

    private final ChassisSpeeds output;

    public TurnBlindCommand(RobotContainer robot, double omegaRadiansPerSecond) {
        drive = robot.drivetrainSubsystem;

        output = new ChassisSpeeds(0, 0, omegaRadiansPerSecond);


        addRequirements(drive.TURN_SUBSYSTEM);
    }

    @Override
    public void execute() {
        drive.combineChassisSpeeds(output);
    }
}

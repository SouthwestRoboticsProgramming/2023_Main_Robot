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

public class TurnBlindCommand extends TimedCommand {
    private final DrivetrainSubsystem drive;
    private final Lights lights;

    private final ChassisSpeeds output;

    // FIXME: Not tested with rotation
    public TurnBlindCommand(RobotContainer robot, double omegaRadiansPerSecond, double runtimeSeconds) {
        super(runtimeSeconds);
        drive = robot.m_drivetrainSubsystem;
        lights = robot.m_lights;


        output = new ChassisSpeeds(0, 0, omegaRadiansPerSecond);
    }

    @Override
    public void initialize() {
        super.initialize();
        lights.set(IndicatorMode.IN_PROGRESS);
    }

    @Override
    public void execute() {
        drive.combineChassisSpeeds(output);
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

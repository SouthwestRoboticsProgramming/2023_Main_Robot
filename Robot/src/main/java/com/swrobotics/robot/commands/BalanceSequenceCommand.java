package com.swrobotics.robot.commands;

import com.swrobotics.lib.swerve.commands.DriveBlindCommand;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CWAngle;
import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.robot.RobotContainer;

import com.swrobotics.robot.subsystems.drive.DrivetrainSubsystem;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import java.util.function.Supplier;

public class BalanceSequenceCommand extends SequentialCommandGroup {
    public BalanceSequenceCommand(RobotContainer robot, boolean reversed) {
        Supplier<Angle> angle = DrivetrainSubsystem::getAllianceForward;

        if (reversed) {
            angle = DrivetrainSubsystem::getAllianceReverse;
        }

        addCommands(
            new StartBalanceCommand(robot, angle, -1.5, false).withTimeout(3),
            new DriveBlindCommand(robot, angle, -1.5, false).withTimeout(1), // Keep driving for 1 second
            new AutoBalanceCommand(robot)
        );
    }
}

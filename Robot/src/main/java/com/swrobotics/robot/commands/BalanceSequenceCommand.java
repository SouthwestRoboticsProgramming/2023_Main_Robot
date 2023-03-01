package com.swrobotics.robot.commands;

import com.swrobotics.lib.swerve.commands.DriveBlindCommand;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CWAngle;
import com.swrobotics.robot.RobotContainer;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

public class BalanceSequenceCommand extends SequentialCommandGroup {
    public BalanceSequenceCommand(RobotContainer robot, boolean reversed) {
        Angle angle = Angle.ZERO;

        if (reversed) {
            angle = CWAngle.deg(180);
        }

        addCommands(
            new StartBalanceCommand(robot, angle, -1.5, false).withTimeout(3),

            new DriveBlindCommand(robot, angle, -1.5, false).withTimeout(1), // Keep driving for 1 second
            new AutoBalanceCommand(robot)
        );
    }
}

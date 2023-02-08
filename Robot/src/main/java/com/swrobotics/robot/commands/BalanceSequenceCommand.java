package com.swrobotics.robot.commands;

import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.robot.RobotContainer;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

public class BalanceSequenceCommand extends SequentialCommandGroup {
    public BalanceSequenceCommand(RobotContainer robot, boolean fromOutsideCommunity) {
        Angle angle = Angle.ZERO;
        if (!fromOutsideCommunity) {
            angle = CCWAngle.deg(180);
        }

        addCommands(
            new StartBalanceCommand(robot, angle, 0.75, false),
            new AutoBalanceCommand(robot)
        );
    }
}

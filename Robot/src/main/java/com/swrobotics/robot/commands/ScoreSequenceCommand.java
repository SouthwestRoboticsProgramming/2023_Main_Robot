package com.swrobotics.robot.commands;

import com.swrobotics.lib.time.Duration;
import com.swrobotics.lib.time.TimeUnit;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.commands.arm.MoveArmToPositionCommand;
import com.swrobotics.robot.subsystems.arm.ArmPosition;
import com.swrobotics.robot.subsystems.intake.GamePiece;
import com.swrobotics.robot.subsystems.intake.IntakeSubsystem;
import edu.wpi.first.wpilibj2.command.PrintCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;

public final class ScoreSequenceCommand extends SequentialCommandGroup {
    public ScoreSequenceCommand(RobotContainer robot, GamePiece piece, ArmPosition.NT scorePos, double iX, double iY, Angle iWrist) {
        addCommands(
                new MoveArmToPositionCommand(robot, new ArmPosition(new Vec2d(iX, iY), iWrist)),
                new MoveArmToPositionCommand(robot, scorePos),
                new PrintCommand("Finished moving the arm to position"),
                new WaitCommand(0.5),
                new IntakeRunCommand(robot.intake, IntakeSubsystem.Mode.EJECT, piece, new Duration(2, TimeUnit.SECONDS))
        );
    }
}

package com.swrobotics.robot.commands.arm;

import com.swrobotics.robot.RobotContainer;

import com.swrobotics.robot.subsystems.arm.ArmPosition;
import com.swrobotics.robot.subsystems.arm.ArmSubsystem;
import edu.wpi.first.wpilibj2.command.CommandBase;

import java.util.function.Supplier;

public final class MoveArmToPositionCommand extends CommandBase {
    private final ArmSubsystem arm;
    private final Supplier<ArmPosition> target;

    public MoveArmToPositionCommand(RobotContainer robot, ArmPosition target) {
        this(robot, () -> target);
    }

    public MoveArmToPositionCommand(RobotContainer robot, Supplier<ArmPosition> target) {
        arm = robot.arm;
        this.target = target;

        addRequirements(arm);
    }

    @Override
    public void execute() {
        arm.setTargetPosition(target.get());
    }

    @Override
    public boolean isFinished() {
        return arm.isInTolerance();
    }
}

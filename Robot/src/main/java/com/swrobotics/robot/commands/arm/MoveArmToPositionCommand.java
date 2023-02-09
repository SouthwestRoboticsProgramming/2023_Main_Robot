package com.swrobotics.robot.commands.arm;

import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.subsystems.arm.ArmSubsystem;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.CommandBase;

public final class MoveArmToPositionCommand extends CommandBase {
    private final ArmSubsystem arm;
    private final Translation2d target;

    public MoveArmToPositionCommand(RobotContainer robot, Translation2d target) {
        arm = robot.arm;
        this.target = target;

        addRequirements(arm);
    }

    @Override
    public void execute() {
        arm.setTargetPosition(target);
    }

    @Override
    public boolean isFinished() {
        return arm.isInTolerance();
    }
}

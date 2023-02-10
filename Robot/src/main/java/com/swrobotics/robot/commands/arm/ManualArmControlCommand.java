package com.swrobotics.robot.commands.arm;

import com.swrobotics.mathlib.MathUtil;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.subsystems.arm.ArmSubsystem;
import com.swrobotics.shared.arm.ArmPose;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj2.command.CommandBase;

public final class ManualArmControlCommand extends CommandBase {
    private static final double MAX_RATE = 1; // Meters/s

    private final ArmSubsystem arm;
    private final Joystick joystick;
    private Translation2d targetPos;

    public ManualArmControlCommand(RobotContainer robot, Joystick joystick) {
        this.arm = robot.arm;
        this.joystick = joystick;

        addRequirements(arm);

        targetPos = new Translation2d(1, 1);
    }

    @Override
    public InterruptionBehavior getInterruptionBehavior() {
        // Prioritize continuing this command
        return InterruptionBehavior.kCancelIncoming;
    }

    @Override
    public void execute() {
        double dt = 0.02;
        double dx = MathUtil.deadband(joystick.getX(), 0.1) * MAX_RATE * dt;
        double dy = MathUtil.deadband(joystick.getY(), 0.1) * MAX_RATE * dt;

        Translation2d newTarget = targetPos.plus(new Translation2d(dx, dy));
        System.out.println("Out of new target");
        if (ArmPose.isEndPositionValid(newTarget)) {
            System.out.println("New target: " + newTarget);
            targetPos = newTarget;
            arm.setTargetPosition(targetPos);
        }
    }
}

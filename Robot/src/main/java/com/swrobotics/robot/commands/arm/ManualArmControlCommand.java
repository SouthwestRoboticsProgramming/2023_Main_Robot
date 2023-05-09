package com.swrobotics.robot.commands.arm;

import org.littletonrobotics.junction.Logger;

import com.swrobotics.lib.net.NTDouble;
import com.swrobotics.lib.net.NTEntry;
import com.swrobotics.mathlib.MathUtil;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.subsystems.arm.ArmSubsystem;
import com.swrobotics.shared.arm.ArmPose;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.CommandBase;

public final class ManualArmControlCommand extends CommandBase {
    private static final double MAX_RATE = 1; // Meters/s

    private final ArmSubsystem arm;
    private final XboxController joystick;
    private Translation2d targetPos;

    /**
     * Command meant for debugging,
     * plug a controller into port 3 to use
     * 
     * @param robot
     */
    public ManualArmControlCommand(RobotContainer robot) {
        this.arm = robot.arm;
        this.joystick = new XboxController(3);

        addRequirements(arm);

        targetPos = new Translation2d(1, 1);
    }

    @Override
    public void execute() {
        double dt = 0.02;
        double dx = MathUtil.deadband(joystick.getLeftX(), 0.1) * MAX_RATE * dt;
        double dy = MathUtil.deadband(-joystick.getLeftY(), 0.1) * MAX_RATE * dt;

        Translation2d newTarget = targetPos.plus(new Translation2d(dx, dy));
        ArmPose pose = ArmPose.fromEndPosition(newTarget);
        if (pose != null && pose.isValid()) {
            targetPos = newTarget;

            Logger.getInstance().recordOutput("arm/target/manualX", targetPos.getX());
            Logger.getInstance().recordOutput("arm/target/manualY", targetPos.getY());

            arm.setTargetPosition(targetPos);
        }
    }
}

package com.swrobotics.robot.commands;

import com.swrobotics.lib.swerve.commands.TurnToAngleCommand;
import com.swrobotics.mathlib.Angle;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.subsystems.vision.Limelight;
import edu.wpi.first.math.geometry.Rotation2d;

import java.util.function.Supplier;

public class AimAtLimelight2 extends TurnToAngleCommand {
    private final Limelight limelight;


    public AimAtLimelight2(RobotContainer robot, Supplier<Angle> angleSupplier) {
        super(robot, angleSupplier, true);

        this.limelight = robot.limelight;
    
    }
	

    @Override
    public void execute() {

        if (limelight.targetFound()) {
        Rotation2d target = limelight.getXAngle();
        
        setTargetRot(target);
        }

        
    }
}

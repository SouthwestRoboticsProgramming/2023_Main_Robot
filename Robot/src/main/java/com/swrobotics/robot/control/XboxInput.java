package com.swrobotics.robot.control;

import java.util.function.Supplier;

import com.swrobotics.mathlib.Vec2d;

import edu.wpi.first.wpilibj.XboxController;

public class XboxInput implements Input {

    private final XboxController controller = new XboxController(0);

    @Override
    public Supplier<Vec2d> getDriveTranslation() {
        // double x = controller.getLeftX();
        // double y = -controller.getLeftY();

        // Vec2d vector = new Vec2d(x, y).normalize();
        
        return () -> new Vec2d(
            controller.getLeftX(),
            controller.getLeftY())
            .normalize();
    }

    @Override
    public Supplier<Double> getDriveRotation() {
        return () -> 0.0; //controller.getRightX();
    }
    
}

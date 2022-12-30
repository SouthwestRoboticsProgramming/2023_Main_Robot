package com.swrobotics.robot.control;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.button.Button;

import static com.swrobotics.robot.control.InputUtils.*;

public class XboxInput implements Input {

    private static final double DEADBAND = 0.2;
    private final XboxController controller = new XboxController(0);

    @Override
    public double getDriveX() {
        return -modifyAxis(controller.getLeftY(), DEADBAND);
    }

    @Override
    public double getDriveY() {
        return -modifyAxis(controller.getLeftX(), DEADBAND);
    }

    @Override
    public double getDriveRotation() {
        return -modifyAxis(controller.getRightX(), DEADBAND);
    }

    @Override
    public Button getResetGyroButton() {
        return new Button(controller::getBackButton);
    }
    
}

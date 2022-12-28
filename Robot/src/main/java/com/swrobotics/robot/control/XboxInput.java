package com.swrobotics.robot.control;

import edu.wpi.first.wpilibj.XboxController;
import static com.swrobotics.robot.control.InputUtils.*;

public class XboxInput implements Input {

    private static final double DEADBAND = 0.2;
    private final XboxController controller = new XboxController(0);

    @Override
    public double getDriveX() {
        return deadband(controller.getLeftX(), DEADBAND);
    }

    @Override
    public double getDriveY() {
        return deadband(controller.getLeftX(), DEADBAND);
    }

    @Override
    public double getDriveRotation() {
        return deadband(controller.getLeftX(), DEADBAND);
    }
    
}

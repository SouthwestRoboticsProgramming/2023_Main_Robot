package com.swrobotics.robot.control;

import edu.wpi.first.wpilibj2.command.button.Button;

public interface Input {
    double getDriveX();
    double getDriveY();
    double getDriveRotation();

    Button getResetGyroButton();
}

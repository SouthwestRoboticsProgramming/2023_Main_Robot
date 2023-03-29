package com.swrobotics.robot.io;

import com.swrobotics.lib.encoder.Encoder;
import com.swrobotics.lib.gyro.Gyroscope;
import com.swrobotics.lib.motor.FeedbackMotor;
import com.swrobotics.lib.motor.Motor;

public interface RobotIO {
    Gyroscope getGyroscope();

    FeedbackMotor getSwerveDriveMotor(int module);
    FeedbackMotor getSwerveTurnMotor(int module);
    Encoder getSwerveEncoder(int module);

    FeedbackMotor getArmBottomMotor();
    FeedbackMotor getArmTopMotor();
    Encoder getArmBottomEncoder();
    Encoder getArmTopEncoder();

    Motor getIntakeMotor();
}

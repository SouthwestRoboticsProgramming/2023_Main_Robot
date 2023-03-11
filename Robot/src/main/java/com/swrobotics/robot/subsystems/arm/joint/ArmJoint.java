package com.swrobotics.robot.subsystems.arm.joint;

public interface ArmJoint {
    // Counterclockwise from horizontal
    double getCurrentAngle();
    void setCurrentAngle(double angle);

    // Positive motor output should correspond to increase in angle
    void setMotorOutput(double motor);
}

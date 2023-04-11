package com.swrobotics.robot.subsystems.arm.joint;

public interface ArmJoint {
  // Counterclockwise from horizontal
  double getCurrentAngle();

  // Assumes arm is in home position physically
  void calibrateCanCoder();

  // Calibrates encoder using CANCoder measurement
  void calibrateHome(double homeAngle);

  // Positive motor output should correspond to increase in angle
  void setMotorOutput(double motor);
}

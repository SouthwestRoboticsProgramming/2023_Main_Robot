package com.swrobotics.robot.subsystems;


// This Could Not Exist, but I made it this way to make it easier to re-factor
public class RobotError {
    public int severity;
    public int id;

    public RobotError(int severity, int id) {
        this.id = id;
        this.severity = severity;
    }
}

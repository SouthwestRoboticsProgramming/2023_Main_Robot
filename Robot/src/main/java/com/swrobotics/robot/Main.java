package com.swrobotics.robot;

import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.RobotBase;

import java.io.File;

/**
 * Do NOT add any static variables to this class, or any initialization at all. Unless you know what
 * you are doing, do not modify this file except to change the parameter class to the startRobot
 * call.
 */
public final class Main {
    private Main() {}

    /**
     * Main initialization function. Do not perform any initialization here.
     *
     * <p>If you change your main robot class, change the parameter type.
     */
    public static void main(String... args) {
        // Delete networktables persistent values
//        File homeDir = Filesystem.getOperatingDirectory();
//        new File(homeDir, "networktables.json").delete();
//        new File(homeDir, "networktables.json.bck").delete();
//        System.out.println("NT data deleted!");

        RobotBase.startRobot(Robot::new);
    }
}

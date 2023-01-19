package com.swrobotics.shufflelog.tool;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTablesJNI;

public class Test {
    public static void main(String[] args) {
        NetworkTableInstance instance = NetworkTableInstance.getDefault();
        instance.setServer("localhost");
        instance.startClient3("Test");
    }
}

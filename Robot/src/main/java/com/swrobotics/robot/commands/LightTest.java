package com.swrobotics.robot.commands;

import com.swrobotics.robot.subsystems.Lights;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class LightTest extends CommandBase {

    private final Lights lights;
    
    public LightTest(Lights lights) {
        this.lights = lights;
        SmartDashboard.putNumber("Light value", 0.0);
    }

    @Override
    public void execute() {
        lights.setDebug(SmartDashboard.getNumber("Light value", 0));
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}

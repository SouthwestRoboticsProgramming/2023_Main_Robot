package com.swrobotics.robot.commands;

import com.swrobotics.robot.subsystems.Lights;
import com.swrobotics.robot.subsystems.Lights.Color;

public class LightCommand extends TimedCommand {

    private final Color color;
    private final Lights lights;

    public LightCommand(Lights lights, Color color, double runtimeSeconds) {
        super(runtimeSeconds);
        this.color = color;
        this.lights = lights;
    }

    @Override
    public void initialize() {
        lights.setColor(color);
        System.out.println("Lights at color: " + color);
    }

    @Override
    public void end(boolean interrupted) {
        lights.setColor(Color.OFF);
    }
    
}

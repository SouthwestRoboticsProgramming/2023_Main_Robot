package com.swrobotics.lib.commands;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.CommandBase;

public class TimedCommand extends CommandBase {

    protected final Timer timer = new Timer();
    protected final double runtimeSeconds;

    protected TimedCommand(double runtimeSeconds) {
        this.runtimeSeconds = runtimeSeconds;
    }

    @Override
    public void initialize() {
        timer.reset();
        timer.start();
    }

    @Override
    public void end(boolean interrupted) {
        timer.stop();
    }

    @Override
    public boolean isFinished() {
        return timer.hasElapsed(runtimeSeconds);
    }
}

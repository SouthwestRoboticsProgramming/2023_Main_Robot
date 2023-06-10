package com.swrobotics.robot.autotest;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.CommandBase;

import java.util.function.Supplier;

public abstract class Test extends CommandBase {
    public static Test check(String name, Supplier<Boolean> checker) {
        return new Test(name) {
            @Override
            public void execute() {
                if (checker.get())
                    pass();
                else
                    fail();
            }
        };
    }

    private final String name;
    private boolean finished;

    public Test(String name) {
        this.name = name;
    }

    @Override
    public void initialize() {
        finished = false;
    }

    protected void pass() {
        System.out.println("Passed: " + name);
        finished = true;
    }

    protected void fail() {
        DriverStation.reportError("Failure: " + name, false);
        finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}

package com.swrobotics.robot;

import com.swrobotics.lib.schedule.Scheduler;
import com.swrobotics.lib.swerve.SwerveDrive;
import com.swrobotics.lib.schedule.Subsystem;
import com.swrobotics.lib.wpilib.AbstractRobot;
import com.swrobotics.messenger.client.MessengerClient;


public final class Robot extends AbstractRobot {
    private static final double PERIODIC_PER_SECOND = 50;

    private final SwerveDrive drive = new SwerveDrive();

    public Robot() {
	    super(PERIODIC_PER_SECOND);
    }
    
    @Override
    protected final void addSubsystems() {
        Scheduler sch = Scheduler.get();

        sch.addSubsystem(drive);
        // drive.setChassisSpeeds(new ChassisSpeeds(1, 1, Math.PI / 4));
    }
}

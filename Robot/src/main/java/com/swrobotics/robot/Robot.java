package com.swrobotics.robot;

import com.swrobotics.lib.schedule.Scheduler;
import com.swrobotics.lib.schedule.Subsystem;
import com.swrobotics.lib.wpilib.AbstractRobot;
import com.swrobotics.messenger.client.MessengerClient;

public final class Robot extends AbstractRobot {
    private static final double PERIODIC_PER_SECOND = 50;

    public Robot() {
	    super(PERIODIC_PER_SECOND);
    }
    
    @Override
    protected final void addSubsystems() {
        MessengerClient msg = new MessengerClient("10.21.29.3", 5805, "RIO");

        Scheduler.get().addSubsystem(new Subsystem() {
            @Override
            public void periodic() {
                msg.prepare("Something")
                        .addDouble(Math.random())
                        .addInt(83)
                        .send();

                msg.readMessages();

                System.out.println("Messenger: " + msg.isConnected());
            }
        });
    }
}

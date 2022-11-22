package com.swrobotics.lib.schedule.debug;

import com.swrobotics.lib.schedule.*;
import com.swrobotics.lib.time.TimeUnit;

public class SchedulerTest implements Subsystem {
    private static final class InstantCommand implements Command {
        @Override
        public void init() {
            System.out.println("Instant command init");
        }

        @Override
        public boolean run() {
            System.out.println("Instant command run");
            return true;
        }

        @Override
        public void end(boolean wasCancelled) {
            System.out.println("Instant command end: " + (wasCancelled ? "cancelled" : "not cancelled"));
        }

        @Override
        public void suspend() {

        }

        @Override
        public void resume() {

        }
    }

    private static final class TestSequence extends CommandSequence {
        public TestSequence(SchedulerTest ss) {
            append(() -> {
                System.out.println("Instantly completed command test");
                System.out.println("Waiting 1 second");
                return true;
            });
            append(new WaitCommand(1, TimeUnit.SECONDS));
            append(() -> {
                System.out.println("Suspending the subsystem");
                Scheduler.get().setSubsystemSuspended(ss, true);
                System.out.println("Change the robot state here to test state init");
                return true;
            });
            append(new WaitCommand(2, TimeUnit.SECONDS));
            append(() -> {
                System.out.println("Resuming the subsystem");
                Scheduler.get().setSubsystemSuspended(ss, false);
                System.out.println("Repeatedly scheduling instant command");
                return true;
            });
            int[] i = new int[1];
            append(() -> {
                Scheduler.get().addCommand(ss, new InstantCommand());
                i[0]++;
                return i[0] >= 5;
            });
            append(() -> {
                System.out.println("Scheduler test complete");
                return true;
            });

            System.out.println("Scheduler test sequence initialized");
        }
    }

    public static void fail(String message) {
        while (true) {
            System.err.println(message);
            try {
                Thread.sleep(1000);
            } catch (Throwable e) {}
        }
    }

    @Override
    public void onAdd() {
        System.out.println("Subsystem added");
    }

    @Override
    public void onRemove() {
        System.out.println("Subsystem removed");
    }

    private boolean shouldFailIfPeriodic = false;

    @Override
    public void suspend() {
        System.out.println("Subsystem suspended");
        shouldFailIfPeriodic = true;
    }

    @Override
    public void resume() {
        System.out.println("Subsystem resumed");
        shouldFailIfPeriodic = false;
    }

    @Override
    public void periodic() {
        if (shouldFailIfPeriodic) {
            fail("Periodic called while suspended");
        }
    }

    @Override
    public void disabledInit() {
        System.out.println("Disabled init");
    }

    @Override
    public void disabledPeriodic() {

    }

    @Override
    public void autonomousInit() {
        System.out.println("Init auto");
    }

    @Override
    public void autonomousPeriodic() {

    }

    @Override
    public void teleopInit() {
        System.out.println("Init teleop");
    }

    @Override
    public void teleopPeriodic() {

    }

    @Override
    public void testInit() {
        System.out.println("Init test");
        System.out.println("Starting the test sequence");

        // Can't be the parent here otherwise it gets suspended when testing that
        Scheduler.get().addCommand(new TestSequence(this));
    }

    @Override
    public void testPeriodic() {

    }
}

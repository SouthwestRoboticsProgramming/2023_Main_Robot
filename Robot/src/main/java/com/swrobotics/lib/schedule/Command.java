package com.swrobotics.lib.schedule;

import com.swrobotics.lib.time.Duration;
import com.swrobotics.lib.time.TimeUnit;
import com.swrobotics.lib.wpilib.AbstractRobot;

/**
 * An action that runs until it is complete.
 * A command must be scheduled through the scheduler and will be ran IMMEDIATELY.
 * A command will continue to run if the robot state changes, so if a command should
 * only run in a specific state, make sure to cancel it if the state changes.
 */
public interface Command {
    Duration DEFAULT_INTERVAL = new Duration(1 / AbstractRobot.get().getPeriodicPerSecond(), TimeUnit.SECONDS);

    /**
     * Called before the first time {@code run()} is called,
     * to perform initialization.
     */
    default void init() {}

    /**
     * This function runs every periodic until it returns true.
     * 
     * <p>
     * NOTE: This is called by the scheduler and not by another class!
     * @return Returns true when command is compete.
     */
    boolean run();

    /**
     * Called by the scheduler when the command ends.
     * 
     * @param wasCancelled Whether the end is caused by cancelling this command
     */
    default void end(boolean wasCancelled) {}

    /**
     * Called by the scheduler when the command or its parent is suspended.
     * The default behavior is to call {@link #end(boolean)} as if the command
     * was cancelled.
     */
    default void suspend() {
        end(true);
    }

    /**
     * Called by the scheduler when the command is resumed after being suspended.
     * The default behavior is to call {@link #init()} as if the command was just
     * started.
     */
    default void resume() {
        init();
    }

    /**
     * Gets the interval between consecutive executions of the command.
     * 
     * @return Interval, by default time per robot periodic
     */
    default Duration getInterval() {
        return DEFAULT_INTERVAL;
    }
}

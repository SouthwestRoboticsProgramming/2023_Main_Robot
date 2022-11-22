package com.swrobotics.lib.schedule;

public interface Subsystem {
    /**
     * Called by the scheduler when this Subsystem is added using
     * {@link Scheduler#addCommand}.
     */
    default void onAdd() {}

    /**
     * Called by the scheduler when this Subsystem is removed
     * using {@link Scheduler#removeCommand}. A subsystem should
     * shut down any components and/or resources it is using here.
     *
     * Example: Stop motor when system removed
     */
    default void onRemove() {}

    /**
     * Called by the scheduler when this subsystem or its parent
     * is suspended. The default behavior is to call the
     * {@link #onRemove) method.
     */
    default void suspend() { onRemove(); }

    /**
     * Called by the scheduler when this subsystem or its parent
     * is resumed after being suspended. The default behavior is to
     * call the {@link #onAdd()} method.
     */
    default void resume() { onAdd(); }

    // Global periodic, called in all states
    default void periodic() {}

    // Robot state handlers
    default void disabledInit() {}
    default void disabledPeriodic() {}
    default void autonomousInit() {}
    default void autonomousPeriodic() {}
    default void teleopInit() {}
    default void teleopPeriodic() {}
    default void testInit() {}
    default void testPeriodic() {}
}

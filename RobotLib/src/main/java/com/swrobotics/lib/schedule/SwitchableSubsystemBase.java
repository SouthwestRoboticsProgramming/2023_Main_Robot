package com.swrobotics.lib.schedule;

import com.swrobotics.lib.net.NTBoolean;

import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Subsystem;

/**
 * Abstract class to allow subsystems to be toggled in NetworkTables. This should be used in place
 * of SubsystemBase. The toggle will be a NTBoolean in the "Subsystems/" table.
 */
public abstract class SwitchableSubsystemBase implements Subsystem {
    private static final String TABLE_NAME = "Subsystems/";

    private static String nameFromClass(Class<?> c) {
        String name = c.getSimpleName();
        name = name.substring(name.lastIndexOf('.') + 1);
        return name;
    }

    private final NTBoolean enable;
    private boolean isScheduled;

    public SwitchableSubsystemBase() {
        String name = nameFromClass(getClass());
        enable = new NTBoolean(TABLE_NAME + name, true);

        isScheduled = false;
        enable.nowAndOnChange(() -> updateEnabled(enable.get()));
    }

    /** Called when the subsystem is disabled. */
    protected void onDisable() {}

    /**
     * Gets whether this subsystem is currently enabled.
     *
     * @return whether the subsystem is enabled
     */
    public boolean isEnabled() {
        return enable.get();
    }

    private void updateEnabled(boolean enabled) {
        if (enabled && !isScheduled) CommandScheduler.getInstance().registerSubsystem(this);
        else if (!enabled && isScheduled) {
            onDisable();
            CommandScheduler.getInstance().unregisterSubsystem(this);
        }
        isScheduled = enabled;
    }
}

package com.swrobotics.robot.subsystems;

import com.swrobotics.lib.net.NTBoolean;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Subsystem;

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
    enable.onChange(() -> updateEnabled(enable.get()));
    updateEnabled(enable.get());
  }

  protected void onDisable() {}

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

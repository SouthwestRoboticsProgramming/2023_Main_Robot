package com.swrobotics.lib.net;

import com.swrobotics.lib.ThreadUtils;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.networktables.NetworkTableInstance;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.function.Supplier;

/**
 * Represents one data entry in NetworkTables.
 *
 * @param <T> data type
 */
public abstract class NTEntry<T> implements Supplier<T> {
  private final ArrayList<Runnable> changeListeners;
  protected final NetworkTableEntry entry;
  private boolean hasSetChangeListener;

  /**
   * Creates a new entry with a specified path. The path can be split using the '/' character to
   * organize entries into groups.
   *
   * <p>NOTE: This entry is persistent by default.
   *
   * @param path path
   */
  public NTEntry(String path, T defaultVal) {
    changeListeners = new ArrayList<>();

    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    NetworkTable table = inst.getTable("");
    String[] parts = path.split("/");
    for (int i = 0; i < parts.length - 1; i++) {
      table = table.getSubTable(parts[i]);
    }
    entry = table.getEntry(parts[parts.length - 1]);

    // Ensure entry actually exists so it is editable
    if (!entry.exists()) set(defaultVal);

    entry.setPersistent();

    hasSetChangeListener = false;
  }

  public abstract void set(T value);

  public NTEntry<T> setTemporary() {
    entry.clearPersistent();
    return this;
  }

  public void onChange(Runnable listener) {
    if (!hasSetChangeListener) {
      NetworkTableInstance.getDefault()
          .addListener(
              entry,
              EnumSet.of(NetworkTableEvent.Kind.kValueAll),
              (event) -> {
                fireOnChanged();
              });
      hasSetChangeListener = true;
    }

    changeListeners.add(listener);
  }

  private void fireOnChanged() {
    for (Runnable listener : changeListeners) {
      ThreadUtils.runOnMainThread(listener);
    }
  }
}

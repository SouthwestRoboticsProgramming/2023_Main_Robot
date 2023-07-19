package com.swrobotics.lib.net;

import com.swrobotics.lib.ThreadUtils;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class NTEntry<T> implements Supplier<T> {
    private final ArrayList<Consumer<T>> changeListeners;
    private boolean hasSetChangeListener;

    public NTEntry() {
        changeListeners = new ArrayList<>();
        hasSetChangeListener = false;
    }

    public abstract void set(T value);

    public abstract NTEntry<T> setPersistent();

    public abstract void registerChangeListeners(Runnable fireFn);

    public void onChange(Consumer<T> listener) {
        if (!hasSetChangeListener) {
            registerChangeListeners(this::fireOnChanged);
            hasSetChangeListener = true;
        }

        changeListeners.add(listener);
    }

    public void nowAndOnChange(Consumer<T> listener) {
        listener.accept(get());
        onChange(listener);
    }

    private void fireOnChanged() {
        for (Consumer<T> listener : changeListeners) {
            ThreadUtils.runOnMainThread(() -> listener.accept(get()));
        }
    }
}

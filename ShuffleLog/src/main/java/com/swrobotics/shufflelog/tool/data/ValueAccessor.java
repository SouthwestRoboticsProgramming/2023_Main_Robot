package com.swrobotics.shufflelog.tool.data;

import edu.wpi.first.networktables.NetworkTableType;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ValueAccessor<T> {
    private final NetworkTableType type;
    private final Supplier<T> getter;
    private final Consumer<T> setter;

    public ValueAccessor(NetworkTableType type, Supplier<T> getter, Consumer<T> setter) {
        this.type = type;
        this.getter = getter;
        this.setter = setter;
    }

    public NetworkTableType getType() {
        return type;
    }

    public T get() {
        return getter.get();
    }

    public void set(T t) {
        setter.accept(t);
    }
}

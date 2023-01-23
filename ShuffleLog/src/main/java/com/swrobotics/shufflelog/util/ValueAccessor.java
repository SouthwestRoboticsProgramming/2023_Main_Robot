package com.swrobotics.shufflelog.util;

public interface ValueAccessor<T> {
    T get();
    void set(T t);
}

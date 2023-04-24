package com.swrobotics.shufflelog.tool;

import java.util.Properties;

public interface Tool {
    void process();

    default void loadPersistence(Properties props) {}
    default void savePersistence(Properties props) {}
}

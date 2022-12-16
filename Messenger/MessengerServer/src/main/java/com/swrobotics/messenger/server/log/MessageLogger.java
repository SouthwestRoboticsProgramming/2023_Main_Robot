package com.swrobotics.messenger.server.log;

import com.swrobotics.messenger.server.Message;

public interface MessageLogger {
    void logEvent(String type, String name, String descriptor);
    void logMessage(Message msg);
}

package com.swrobotics.messenger.server.log;

import com.swrobotics.messenger.server.Message;

/**
 * A logger that does nothing.
 *
 * @author rmheuer
 */
public final class NoOpLogger implements MessageLogger {
    @Override
    public void logEvent(String type, String name, String descriptor) {}

    @Override
    public void logMessage(Message msg) {}
}

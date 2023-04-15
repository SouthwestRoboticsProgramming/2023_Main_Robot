package com.swrobotics.messenger.client;

/**
 * Represents a function called when a message is received.
 *
 * @author rmheuer
 */
@FunctionalInterface
public interface MessageHandler {
    /**
     * Called when a matching message is received.
     *
     * @param type message type
     * @param reader message data reader
     */
    void handle(String type, MessageReader reader);
}

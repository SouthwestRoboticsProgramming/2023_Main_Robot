package com.swrobotics.messenger.client.impl.handler;

public interface Handler {
    void handle(String type, byte[] data);
}

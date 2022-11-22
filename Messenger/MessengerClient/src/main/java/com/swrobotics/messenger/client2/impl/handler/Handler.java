package com.swrobotics.messenger.client2.impl.handler;

public interface Handler {
    void handle(String type, byte[] data);
}

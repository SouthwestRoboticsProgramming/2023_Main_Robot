package com.swrobotics.messenger.client.impl.handler;

import com.swrobotics.messenger.client.MessageHandler;
import com.swrobotics.messenger.client.MessageReader;

public final class DirectHandler implements Handler {
    private final String targetType;
    private final MessageHandler handler;

    public DirectHandler(String targetType, MessageHandler handler) {
        this.targetType = targetType;
        this.handler = handler;
    }

    @Override
    public void handle(String type, byte[] data) {
        if (type.equals(targetType)) {
            handler.handle(type, new MessageReader(data));
        }
    }
}

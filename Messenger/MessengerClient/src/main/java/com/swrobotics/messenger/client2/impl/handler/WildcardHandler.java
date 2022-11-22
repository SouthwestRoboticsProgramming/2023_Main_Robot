package com.swrobotics.messenger.client2.impl.handler;

import com.swrobotics.messenger.client2.MessageHandler;
import com.swrobotics.messenger.client2.MessageReader;

public final class WildcardHandler implements Handler {
    private final String targetPrefix;
    private final MessageHandler handler;

    public WildcardHandler(String targetPrefix, MessageHandler handler) {
        this.targetPrefix = targetPrefix;
        this.handler = handler;
    }

    @Override
    public void handle(String type, byte[] data) {
        if (type.startsWith(targetPrefix)) {
            handler.handle(type, new MessageReader(data));
        }
    }
}

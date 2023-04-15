package com.swrobotics.messenger.server;

public final class Message {
    private final String type;
    private final byte[] data;

    public Message(String type, byte[] data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public byte[] getData() {
        return data;
    }
}

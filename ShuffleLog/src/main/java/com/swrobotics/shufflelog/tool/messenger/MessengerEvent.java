package com.swrobotics.shufflelog.tool.messenger;

public final class MessengerEvent {
    private final double timestamp;
    private final String type;
    private final String name;
    private final String descriptor;

    public MessengerEvent(double timestamp, String type, String name, String descriptor) {
        this.timestamp = timestamp;
        this.type = type;
        this.name = name;
        this.descriptor = descriptor;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    @Override
    public String toString() {
        return "MessengerEvent{"
                + "timestamp="
                + timestamp
                + ", type='"
                + type
                + '\''
                + ", name='"
                + name
                + '\''
                + ", descriptor='"
                + descriptor
                + '\''
                + '}';
    }
}

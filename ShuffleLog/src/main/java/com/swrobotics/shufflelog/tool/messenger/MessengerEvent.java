package com.swrobotics.shufflelog.tool.messenger;

public final class MessengerEvent {
    private final String type;
    private final String name;
    private final String descriptor;

    public MessengerEvent(String type, String name, String descriptor) {
        this.type = type;
        this.name = name;
        this.descriptor = descriptor;
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
        return "MessengerEvent{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", descriptor='" + descriptor + '\'' +
                '}';
    }
}

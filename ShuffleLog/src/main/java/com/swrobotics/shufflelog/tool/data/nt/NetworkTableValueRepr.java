package com.swrobotics.shufflelog.tool.data.nt;

import edu.wpi.first.networktables.GenericPublisher;
import edu.wpi.first.networktables.GenericSubscriber;
import edu.wpi.first.networktables.NetworkTableType;
import edu.wpi.first.networktables.Topic;

public final class NetworkTableValueRepr implements AutoCloseable {
    private final Topic topic;
    public final GenericSubscriber sub;
    public final GenericPublisher pub;

    public NetworkTableValueRepr(Topic topic) {
        this.topic = topic;
        sub = topic.genericSubscribe();
        pub = topic.genericPublish(topic.getTypeString());
    }

    public String getName() {
        return topic.getName();
    }

    public boolean isArray() {
        switch (topic.getType()) {
            case kBooleanArray:
            case kFloatArray:
            case kDoubleArray:
            case kIntegerArray:
            case kStringArray:
                return true;
        }
        return false;
    }

    public NetworkTableType getType() {
        return topic.getType();
    }

    @Override
    public void close() {
        sub.close();
        pub.close();
    }
}

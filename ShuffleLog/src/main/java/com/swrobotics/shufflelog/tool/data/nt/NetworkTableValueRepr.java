package com.swrobotics.shufflelog.tool.data.nt;

import edu.wpi.first.networktables.*;

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
        return NetworkTable.basenameKey(topic.getName());
    }

    public String getPath() {
        return topic.getName();
    }

    public NetworkTableType getType() {
        return sub.get().getType();
    }

    @Override
    public void close() {
        sub.close();
        pub.close();
    }
}

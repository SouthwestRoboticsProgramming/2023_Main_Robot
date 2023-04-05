package com.swrobotics.shufflelog.tool.data.nt;

import edu.wpi.first.networktables.*;

public final class NetworkTableValueRepr implements AutoCloseable {
    private final Topic topic;
    public final GenericSubscriber sub;
    private GenericPublisher pub;

    public NetworkTableValueRepr(Topic topic) {
        this.topic = topic;
        sub = topic.genericSubscribe();
        pub = null;
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

    public GenericPublisher getPub() {
        if (pub == null) pub = topic.genericPublish(topic.getTypeString());
        return pub;
    }

    @Override
    public void close() {
        sub.close();
        if (pub != null) pub.close();
    }
}

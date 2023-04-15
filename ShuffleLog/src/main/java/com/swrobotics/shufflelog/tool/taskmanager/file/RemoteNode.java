package com.swrobotics.shufflelog.tool.taskmanager.file;

import java.util.ArrayList;
import java.util.List;

public abstract class RemoteNode {
    private final String name;
    private RemoteDirectory parent;

    public RemoteNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setParent(RemoteDirectory parent) {
        this.parent = parent;
    }

    public String getFullPath() {
        List<RemoteNode> nodes = new ArrayList<>();
        RemoteNode node = this;
        while (node.parent != null) {
            nodes.add(0, node);
            node = node.parent;
        }

        StringBuilder builder = new StringBuilder();
        boolean separator = false;
        for (RemoteNode n : nodes) {
            if (separator) builder.append("/");
            else separator = true;

            builder.append(n.name);
        }

        return builder.toString();
    }

    public void remove() {
        parent.removeChild(name);
    }
}

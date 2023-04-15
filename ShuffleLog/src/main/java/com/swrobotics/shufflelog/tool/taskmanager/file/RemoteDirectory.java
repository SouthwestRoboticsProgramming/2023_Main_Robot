package com.swrobotics.shufflelog.tool.taskmanager.file;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RemoteDirectory extends RemoteNode {
    private final Map<String, RemoteNode> children;
    private boolean needsRefreshContent;

    public RemoteDirectory(String name) {
        super(name);
        children = new LinkedHashMap<>();
        needsRefreshContent = true;
    }

    public RemoteNode getChild(String name) {
        return children.get(name);
    }

    public Collection<RemoteNode> getChildren() {
        return children.values();
    }

    public void clearChildren() {
        children.clear();
    }

    public void addChild(RemoteNode node) {
        node.setParent(this);
        children.put(node.getName(), node);
    }

    public void removeChild(String name) {
        children.remove(name);
    }

    public void setNeedsRefreshContent(boolean needsRefreshContent) {
        this.needsRefreshContent = needsRefreshContent;
    }

    public boolean needsRefreshContent() {
        return needsRefreshContent;
    }
}

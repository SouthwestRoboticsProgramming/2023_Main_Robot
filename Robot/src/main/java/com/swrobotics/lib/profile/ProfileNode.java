package com.swrobotics.lib.profile;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one section of code that was profiled.
 */
public final class ProfileNode {
    private final String name;
    private final ProfileNode parent;
    private final List<ProfileNode> children;
    private long total;
    private long accumulator;
    private long startTime, pauseTime;

    public ProfileNode(String name, ProfileNode parent) {
        this.name = name;
        this.parent = parent;
        children = new ArrayList<>();
        accumulator = 0;
        total = 0;
    }

    public ProfileNode(String name, ProfileNode parent, long accumulator, long total) {
        this.name = name;
        this.parent = parent;
        children = new ArrayList<>();
        this.accumulator = accumulator;
        this.total = total;
    }

    public void begin() {
        startTime = System.nanoTime();
        unpause();
    }

    public void pause() {
        accumulator += System.nanoTime() - pauseTime;
    }

    public void unpause() {
        pauseTime = System.nanoTime();
    }

    public void end() {
        pause();
        total = System.nanoTime() - startTime;
    }

    public void addChild(ProfileNode child) {
        children.add(child);
    }

    /**
     * Gets the name of this node.
     * 
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the parent of this node.
     * 
     * @return parent
     */
    public ProfileNode getParent() {
        return parent;
    }

    /**
     * Gets the children of this node.
     * Important: Do not modify the returned list from this method.
     * 
     * @return children
     */
    public List<ProfileNode> getChildren() {
        // Intentionally not defensively copied
        return children;
    }

    /**
     * Gets the elapsed execution time of this node, not including
     * the time spent running child nodes.
     * 
     * @return self time in nanoseconds
     */
    public long getSelfTimeNanoseconds() {
        return accumulator;
    }

    /**
     * Gets the total execution of this node, including the time spent
     * running child nodes.
     * 
     * @return total time in nanoseconds
     */
    public long getTotalTimeNanoseconds() { return total; }
}

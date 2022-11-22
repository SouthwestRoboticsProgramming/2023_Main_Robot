package com.swrobotics.lib.profile;

/**
 * A tool to measure how long each part of the code takes to run.
 */
public final class Profiler {
    private static ProfileNode root, current, last;

    private Profiler() {
        throw new AssertionError();
    }

    /**
     * Begins a new round of profiling. This is done automatically
     * every periodic by {@code AbstractRobot}.
     * 
     * @param rootName name of the root node
     */
    public static void beginMeasurements(String rootName) {
        root = new ProfileNode(rootName, null);
        current = root;
        root.begin();
    }

    /**
     * Begins a new child node with a specified name.
     * 
     * @param name name of the child node
     */
    public static void push(String name) {
        ProfileNode next = new ProfileNode(name, current);
        current.pause();
        current.addChild(next);
        current = next;
        current.begin();
    }

    /**
     * Ends the current node and returns to profiling the
     * parent node.
     */
    public static void pop() {
        current.end();
        current = current.getParent();
        current.unpause();
    }

    /**
     * Ends the round of profiling. This is done automatically after every
     * periodic by {@code AbstractRobot}.
     */
    public static void endMeasurements() {
        current.end();
        last = root;
    }

    /**
     * Gets the root node from the last round of profiling.
     * 
     * @return last round data
     */
    public static ProfileNode getLastData() {
        return last;
    }
}

package com.swrobotics.pathfinding.task;

public final class PathfinderMain {
    public static void main(String[] args) {
        PathfinderTask task = new PathfinderTask();
        task.run();
    }

    private PathfinderMain() {
        throw new AssertionError();
    }
}

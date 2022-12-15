package com.swrobotics.pathfinding.finder;

import com.swrobotics.pathfinding.Point;

import java.util.List;

public interface Pathfinder {
    void setStart(Point start);
    void setGoal(Point goal);

    List<Point> findPath();
}

package com.swrobotics.pathfinding.core.finder;

import java.util.List;

public interface Pathfinder<P> {
    void setStart(P start);

    void setGoal(P goal);

    List<P> findPath();
}

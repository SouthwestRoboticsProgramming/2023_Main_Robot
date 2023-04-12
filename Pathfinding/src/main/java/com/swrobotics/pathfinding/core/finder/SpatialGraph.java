package com.swrobotics.pathfinding.core.finder;

public interface SpatialGraph<P> extends Graph<P> {
    boolean lineOfSight(P a, P b);
}

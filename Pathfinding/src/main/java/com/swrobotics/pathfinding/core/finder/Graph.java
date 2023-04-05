package com.swrobotics.pathfinding.core.finder;

public interface Graph<P> {
    double heuristic(P point, P goal);

    double cost(P current, P next);

    // It is valid to return the same array again
    // countOut[0] should be set to the number of neighbors
    P[] getNeighbors(P current, int[] countOut);
}

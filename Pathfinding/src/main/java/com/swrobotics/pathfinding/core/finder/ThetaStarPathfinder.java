package com.swrobotics.pathfinding.core.finder;

public final class ThetaStarPathfinder<P> extends AStarPathfinder<P> {
    private final SpatialGraph<P> graph;

    public ThetaStarPathfinder(SpatialGraph<P> graph) {
        super(graph);
        this.graph = graph;
    }

    @Override
    protected void computeCost(Node<P> current, Node<P> next) {
        if (current.parent != null && graph.lineOfSight(current.parent.position, next.position)) {
            double newCost =
                    current.parent.cost + graph.cost(current.parent.position, next.position);
            if (newCost < next.cost) {
                next.parent = current.parent;
                next.cost = newCost;
            }
        } else {
            super.computeCost(current, next);
        }
    }
}

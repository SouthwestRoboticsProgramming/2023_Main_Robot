package com.swrobotics.pathfinding.finder;

import com.swrobotics.pathfinding.grid.Grid;

public final class ThetaStarPathfinder extends AStarPathfinder {
    public ThetaStarPathfinder(Grid grid) {
        super(grid);
    }

    @Override
    protected void computeCost(Node current, Node next) {
        if (current.parent != null && grid.lineOfSight(current.parent.position, next.position)) {
            double newCost = current.parent.cost + getCost(current.parent, next);
            if (newCost < next.cost) {
                next.parent = current.parent;
                next.cost = newCost;
            }
        } else {
            super.computeCost(current, next);
        }
    }
}

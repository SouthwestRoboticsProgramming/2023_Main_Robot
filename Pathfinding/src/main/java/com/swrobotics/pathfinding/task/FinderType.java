package com.swrobotics.pathfinding.task;

import com.swrobotics.pathfinding.core.finder.AStarPathfinder;
import com.swrobotics.pathfinding.core.finder.Pathfinder;
import com.swrobotics.pathfinding.core.finder.ThetaStarPathfinder;
import com.swrobotics.pathfinding.core.grid.Grid;
import com.swrobotics.pathfinding.core.grid.Point;

public enum FinderType {
    A_STAR {
        @Override
        public Pathfinder<Point> create(Grid grid) {
            return new AStarPathfinder<>(grid.asGraph());
        }
    },
    THETA_STAR {
        @Override
        public Pathfinder<Point> create(Grid grid) {
            return new ThetaStarPathfinder<>(grid.asGraph());
        }
    };

    public abstract Pathfinder<Point> create(Grid grid);
}

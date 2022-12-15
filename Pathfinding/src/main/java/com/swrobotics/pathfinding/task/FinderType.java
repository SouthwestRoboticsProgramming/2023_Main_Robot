package com.swrobotics.pathfinding.task;

import com.swrobotics.pathfinding.finder.AStarPathfinder;
import com.swrobotics.pathfinding.finder.Pathfinder;
import com.swrobotics.pathfinding.finder.ThetaStarPathfinder;
import com.swrobotics.pathfinding.grid.Grid;

public enum FinderType {
    A_STAR {
        @Override
        public Pathfinder create(Grid grid) {
            return new AStarPathfinder(grid);
        }
    },
    THETA_STAR {
        @Override
        public Pathfinder create(Grid grid) {
            return new ThetaStarPathfinder(grid);
        }
    };

    public abstract Pathfinder create(Grid grid);
}

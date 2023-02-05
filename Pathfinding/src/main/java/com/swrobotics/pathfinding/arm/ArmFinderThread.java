package com.swrobotics.pathfinding.arm;

import com.swrobotics.pathfinding.core.FinderThread;
import com.swrobotics.pathfinding.core.finder.ThetaStarPathfinder;
import com.swrobotics.pathfinding.core.grid.Point;
import com.swrobotics.shared.arm.ArmConstants;
import com.swrobotics.shared.arm.ArmPose;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class ArmFinderThread extends FinderThread<Point> {
    private final ArmStateSpaceGrid grid;
    private final AtomicReference<List<ArmPose>> pathRef;

    private static ArmStateSpaceGrid tempGrid;
    public ArmFinderThread() {
        // Java can be quite annoying sometimes
        super("Arm Finder", new ThetaStarPathfinder<>((tempGrid = new ArmStateSpaceGrid()).asGraph(ArmConstants.BOTTOM_GEAR_RATIO, ArmConstants.TOP_GEAR_RATIO)));

        this.pathRef = new AtomicReference<>();
        grid = tempGrid;
    }

    public ArmStateSpaceGrid getGrid() {
        return grid;
    }

    public void setEndpoints(ArmPose start, ArmPose goal) {
        setEndpoints(ArmStateSpaceGrid.closestPoint(start), ArmStateSpaceGrid.closestPoint(goal));
    }

    public List<ArmPose> getLatestPath() {
        return pathRef.get();
    }

    @Override
    protected void reportResult(List<Point> path) {
        if (path == null) {
            pathRef.set(null);
            return;
        }

        List<ArmPose> poses = new ArrayList<>();
        for (Point point : path) {
            poses.add(ArmStateSpaceGrid.pointToPose(point));
        }
        pathRef.set(poses);
    }
}

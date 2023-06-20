package com.swrobotics.robot.subsystems.arm2;

import com.swrobotics.mathlib.Angle;
import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.messenger.client.MessengerClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ArmPathfinder {
    // This is different from ArmPose since the pathfinder does not care
    // about the wrist angle. It just approximates the intake as a circle
    public static final class PathPoint {
        public final Angle bottomAngle;
        public final Angle topAngle;

        public PathPoint(Angle bottomAngle, Angle topAngle) {
            this.bottomAngle = bottomAngle;
            this.topAngle = topAngle;
        }

        public static PathPoint fromPose(ArmPose pose) {
            return new PathPoint(pose.bottomAngle, pose.topAngle);
        }

        @Override
        public String toString() {
            return "PathPoint{" +
                    "bottomAngle=" + bottomAngle +
                    ", topAngle=" + topAngle +
                    '}';
        }
    }

    private static final String MSG_CALC = "Pathfinding:Calc";
    private static final String MSG_PATH = "Pathfinding:Path";

    private static final double CORRECT_TARGET_TOL = 0.1;

    private final MessengerClient msg;
    private List<PathPoint> path;
    private PathPoint target;

    public ArmPathfinder(MessengerClient msg) {
        this.msg = msg;
        path = null;

        // Something non-null it'll never be set to
        target = new PathPoint(Angle.ZERO, Angle.ZERO);

        msg.addHandler(MSG_PATH, this::onPath);
    }

    public void setEndpoints(PathPoint start, PathPoint goal) {
        target = goal;
        msg.prepare(MSG_CALC)
                .addDouble(start.bottomAngle.ccw().rad())
                .addDouble(start.topAngle.ccw().rad())
                .addDouble(goal.bottomAngle.ccw().rad())
                .addDouble(goal.topAngle.ccw().rad())
                .send();
    }

    // If null, no path was found (shouldn't happen, but still possible)
    // If non-null, the path should be checked with isPathValid() to check if it's up to date
    public List<PathPoint> getPath() {
        return path;
    }

    public boolean isPathValid() {
        if (path == null || path.isEmpty())
            return false;

        PathPoint lastPoint = path.get(path.size() - 1);
        double diffBot = lastPoint.bottomAngle.ccw().deg() - target.bottomAngle.ccw().deg();
        double diffTop = lastPoint.topAngle.ccw().deg() - target.topAngle.ccw().deg();
        double magSq = diffBot * diffBot + diffTop * diffTop;

        return magSq < CORRECT_TARGET_TOL * CORRECT_TARGET_TOL;
    }

    private void onPath(String type, MessageReader reader) {
        boolean good = reader.readBoolean();
        if (!good) {
            path = null;
            return;
        }

        path = new ArrayList<>();
        int length = reader.readInt();
        for (int i = 0; i < length; i++) {
            double bot = reader.readDouble();
            double top = reader.readDouble();
            path.add(new PathPoint(CCWAngle.rad(bot), CCWAngle.rad(top)));
        }
    }
}

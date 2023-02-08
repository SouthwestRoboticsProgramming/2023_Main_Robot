package com.swrobotics.robot.positions;

import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.robot.blockauto.WaypointStorage;
import edu.wpi.first.wpilibj.DriverStation;

public final class ScoringPositions {
    // Warning: Changing this will break any references to the waypoints in BlockAuto sequences
    private static final String WAYPOINT_NAME_PREFIX = "Score Pos ";

    private static final class Position {
        private final Vec2d blueAlliancePos;
        private final Vec2d redAlliancePos;

        public Position(Vec2d blueAlliancePos, Vec2d redAlliancePos) {
            this.blueAlliancePos = blueAlliancePos;
            this.redAlliancePos = redAlliancePos;
        }

        public Vec2d get(DriverStation.Alliance alliance) {
            if (alliance == DriverStation.Alliance.Red)
                return redAlliancePos;
            return blueAlliancePos;
        }
    }

    private static final Position[] POSITIONS = {
            // TODO
    };

    public static Vec2d getPosition(int column) {
        return POSITIONS[column].get(DriverStation.getAlliance());
    }

    public static void update() {
        DriverStation.Alliance alliance = DriverStation.getAlliance();
        for (int i = 0; i < POSITIONS.length; i++) {
            WaypointStorage.registerStaticWaypoint(
                    WAYPOINT_NAME_PREFIX + i,
                    POSITIONS[i].get(alliance)
            );
        }
    }
}

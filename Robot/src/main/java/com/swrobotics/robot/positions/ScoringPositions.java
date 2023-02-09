package com.swrobotics.robot.positions;

import com.swrobotics.lib.swerve.commands.PathfindToPointCommand;
import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.blockauto.WaypointStorage;
import com.swrobotics.robot.commands.arm.MoveArmToPositionCommand;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;

public final class ScoringPositions {
    private static final class Position {
        private final String name;
        private final Vec2d blueAlliancePos;
        private final Vec2d redAlliancePos;

        public Position(String name, double y) {
            this.name = name;

            double yMeters = Units.inchesToMeters(y);
            this.blueAlliancePos = new Vec2d(BLUE_X, yMeters);
            this.redAlliancePos = new Vec2d(RED_X, yMeters);
        }

        public Vec2d get(DriverStation.Alliance alliance) {
            if (alliance == DriverStation.Alliance.Red)
                return redAlliancePos;
            return blueAlliancePos;
        }
    }

    // In inches
    private static final double BOTTOM_TO_LOWEST_CONE = 20.19;
    private static final double CONE_SPAN_ACROSS_CUBE = 44;
    private static final double CONE_SPAN_ADJACENT = 22;
    private static final double BOTTOM_TO_LOWEST_CUBE = 42.19;
    private static final double CUBE_SPAN = 66;
    private static final double BLUE_RIGHT_X = 4*12 + 6.25;
    private static final double RED_LEFT_X = BLUE_RIGHT_X + 542.7225;

    private static final double ROBOT_SIZE_FW = 32; // FIXME
    private static final double DIST_FROM_GRIDS = 6 + ROBOT_SIZE_FW / 2;
    private static final double BLUE_X = Units.inchesToMeters(BLUE_RIGHT_X + DIST_FROM_GRIDS);
    private static final double RED_X = Units.inchesToMeters(RED_LEFT_X - DIST_FROM_GRIDS);

    private static final Position[] POSITIONS = {
            new Position("Grid 0 (CONE)", BOTTOM_TO_LOWEST_CONE),
            new Position("Grid 1 (CUBE)", BOTTOM_TO_LOWEST_CUBE),
            new Position("Grid 2 (CONE)", BOTTOM_TO_LOWEST_CONE + CONE_SPAN_ACROSS_CUBE),
            new Position("Grid 3 (CONE)", BOTTOM_TO_LOWEST_CONE + CONE_SPAN_ACROSS_CUBE + CONE_SPAN_ADJACENT),
            new Position("Grid 4 (CUBE)", BOTTOM_TO_LOWEST_CUBE + CUBE_SPAN),
            new Position("Grid 5 (CONE)", BOTTOM_TO_LOWEST_CONE + 2 * CONE_SPAN_ACROSS_CUBE + CONE_SPAN_ADJACENT),
            new Position("Grid 6 (CONE)", BOTTOM_TO_LOWEST_CONE + 2 * CONE_SPAN_ACROSS_CUBE + 2 * CONE_SPAN_ADJACENT),
            new Position("Grid 7 (CUBE)", BOTTOM_TO_LOWEST_CUBE + 2 * CUBE_SPAN),
            new Position("Grid 8 (CONE)", BOTTOM_TO_LOWEST_CONE + 3 * CONE_SPAN_ACROSS_CUBE + 2 * CONE_SPAN_ADJACENT)
    };

    // FIXME: None of these are correct
    private static final Translation2d[] ARM_POSITIONS = {
            new Translation2d(1.5, 1.5),
            new Translation2d(0.9, 0.75),
            new Translation2d(0.3, 0)
    };

    public static Command moveToPosition(RobotContainer robot, int column, int row) {
        Vec2d fieldPos = getPosition(column);
        Translation2d armPos = getArmPosition(row);

        return new ParallelCommandGroup(
                new PathfindToPointCommand(robot, fieldPos),
                new MoveArmToPositionCommand(robot, armPos)
        );
    }

    public static Vec2d getPosition(int column) {
        return POSITIONS[column].get(DriverStation.getAlliance());
    }

    public static Translation2d getArmPosition(int row) {
        return ARM_POSITIONS[row];
    }

    public static void update() {
        DriverStation.Alliance alliance = DriverStation.getAlliance();
        for (Position position : POSITIONS) {
            WaypointStorage.registerStaticWaypoint(
                    position.name,
                    position.get(alliance)
            );
        }
    }
}

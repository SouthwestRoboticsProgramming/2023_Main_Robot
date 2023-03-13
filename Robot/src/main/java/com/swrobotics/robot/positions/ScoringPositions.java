package com.swrobotics.robot.positions;

import com.swrobotics.lib.swerve.commands.PathfindToPointCommand;
import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.robot.RobotContainer;
import com.swrobotics.robot.blockauto.WaypointStorage;
import com.swrobotics.robot.commands.arm.MoveArmToPositionCommand;
import com.swrobotics.robot.subsystems.intake.GamePiece;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.*;

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

    private static final double ROBOT_SIZE_FW = 33;
    private static final double DIST_FROM_GRIDS = 3 + ROBOT_SIZE_FW / 2;
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

    private static final Translation2d CUBE_UPPER = new Translation2d(1.501206, 1.393604 - 0.15 - Units.inchesToMeters(6));
    private static final Translation2d CUBE_CENTER = new Translation2d(1.436464 - 0.15, 0.575931);
    private static final Translation2d CUBE_LOWER = new Translation2d(1.007295, 0.362655);
    private static final Translation2d CONE_UPPER = new Translation2d(1.42892 + Units.inchesToMeters(3), 1.533184 + Units.inchesToMeters(2) - Units.inchesToMeters(18));
    private static final Translation2d CONE_CENTER = new Translation2d(1.076355, 0.872756);
    private static final Translation2d CONE_LOWER = new Translation2d(0.929547, 0.093047);
    private static final Translation2d[] CUBE_POSITIONS = {CUBE_UPPER, CUBE_CENTER, CUBE_LOWER};
    private static final Translation2d[] CONE_POSITIONS = {CONE_UPPER, CONE_CENTER, CONE_LOWER};

    private static final Translation2d CONE_PICKUP = new Translation2d(0.671102, 0.758571);
    private static final Translation2d CUBE_PICKUP = new Translation2d(0.766450, 0.666299 - 0.1);
    private static final double PRE_HEIGHT = 0.15;
    private static final Translation2d CONE_PICKUP_PRE = new Translation2d(CONE_PICKUP.getX(), CONE_PICKUP.getY() + PRE_HEIGHT);
    private static final Translation2d CUBE_PICKUP_PRE = new Translation2d(CUBE_PICKUP.getX(), CUBE_PICKUP.getY() + PRE_HEIGHT + 0.1);

    public static final Translation2d HOLD_TARGET = new Translation2d(0.689397, Units.inchesToMeters(11.5 - 13 + 2.25));

    private static Command moveArm(RobotContainer robot, int column, int row) {
        Translation2d armPos = getArmPosition(row, column);
        Translation2d up = new Translation2d(
            0.6,
            armPos.getY()
        );

        Translation2d currentPos = robot.arm.getCurrentPose().getEndPosition();
        double distToTarget = currentPos.getDistance(armPos);
        double distToUp = currentPos.getDistance(up);

        MoveArmToPositionCommand toTarget = new MoveArmToPositionCommand(robot, armPos);
        if ((row == 1 || row == 0) && distToUp < distToTarget) {
            MoveArmToPositionCommand upFirst = new MoveArmToPositionCommand(robot, up);
            return upFirst.andThen(toTarget);
        }

        return toTarget;
    }

    public static Command moveToPosition(RobotContainer robot, int column, int row) {
        Vec2d fieldPos = getPosition(column);

        double angle = DriverStation.getAlliance() == DriverStation.Alliance.Blue ? 180 : 0;

        ParallelCommandGroup driveAndArm = new ParallelCommandGroup(
                new PathfindToPointCommand(robot, fieldPos),
                moveArm(robot, column, row)
        );

        TurnWithArmSafetyCommand turn = new TurnWithArmSafetyCommand(robot, () -> CCWAngle.deg(angle), fieldPos);
        RepeatCommand turnRepeat = turn.repeatedly();
        return new CommandBase() {
            boolean driveDone = false;
            {
                CommandScheduler.getInstance().registerComposedCommands(driveAndArm, turnRepeat);
                m_requirements.addAll(driveAndArm.getRequirements());
                m_requirements.addAll(turnRepeat.getRequirements());
            }

            @Override
            public void initialize() {
                driveAndArm.initialize();
                turnRepeat.initialize();
            }

            @Override
            public void execute() {
                if (!driveDone) {
                    driveAndArm.execute();
                    if (driveAndArm.isFinished()) {
                        driveAndArm.end(false);
                        driveDone = true;
                    }
                }

                turnRepeat.execute();
            }

            @Override
            public void end(boolean cancelled) {
                if (!driveDone)
                    driveAndArm.end(true);
                turnRepeat.end(cancelled);
            }

            @Override
            public boolean isFinished() {
                return driveDone && turn.isFinished();
            }
        };
    }

    public static Vec2d getPosition(int column) {
        return POSITIONS[column].get(DriverStation.getAlliance());
    }

    private static boolean isCube(int column) {
        return column == 1 || column == 4 || column == 7;
    }

    public static Translation2d getArmPosition(int row, int column) {
        if (isCube(column))
            return CUBE_POSITIONS[row];
        else
            return CONE_POSITIONS[row];
    }

    public static Translation2d getPickupArmTarget(GamePiece piece) {
        if (piece == GamePiece.CONE)
            return CONE_PICKUP;
        else
            return CUBE_PICKUP;
    }

    public static Translation2d getPickupArmTargetPre(GamePiece piece) {
        if (piece == GamePiece.CONE)
            return CONE_PICKUP_PRE;
        else
            return CUBE_PICKUP_PRE;
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

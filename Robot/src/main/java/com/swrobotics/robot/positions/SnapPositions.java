package com.swrobotics.robot.positions;

import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.robot.subsystems.drive.DrivetrainSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.FieldObject2d;

public final class SnapPositions {
    public static final class SnapPosition {
        private final Pose2d bluePose;

        public SnapPosition(double x, double y, double rot) {
            bluePose = new Pose2d(Units.inchesToMeters(x), Units.inchesToMeters(y), new Rotation2d(rot));
        }

        public Pose2d getPose() {
            return DrivetrainSubsystem.flipForAlliance(bluePose);
        }
    }

    public static final class SnapStatus {
        public final Translation2d snapPosition;
        public final Rotation2d snapRotation;

        public SnapStatus(Translation2d snapPosition, Rotation2d snapRotation) {
            this.snapPosition = snapPosition;
            this.snapRotation = snapRotation;
        }

        @Override
        public String toString() {
            return "SnapStatus{" +
                    "snapPosition=" + snapPosition +
                    ", snapRotation=" + snapRotation +
                    '}';
        }
    }

    private static final double SNAP_RADIUS = 1.5; // Meters
    private static final double ANGLE_SNAP_TOL = 22.5; // Degrees

    // In inches
    private static final double BOTTOM_TO_LOWEST_CONE = 20.19;
    private static final double CONE_SPAN_ACROSS_CUBE = 44;
    private static final double CONE_SPAN_ADJACENT = 22;
    private static final double BOTTOM_TO_LOWEST_CUBE = 42.19;
    private static final double CUBE_SPAN = 66;
    private static final double BLUE_RIGHT_X = 4*12 + 6.25;

    private static final double ROBOT_SIZE_FW = 33;
    private static final double DIST_FROM_GRIDS = 3 + ROBOT_SIZE_FW / 2;
    private static final double BLUE_X = BLUE_RIGHT_X + DIST_FROM_GRIDS;

    private static final SnapPosition[] POSITIONS = {
            // Grids
            new SnapPosition(BLUE_X, BOTTOM_TO_LOWEST_CONE, Math.PI),
            new SnapPosition(BLUE_X, BOTTOM_TO_LOWEST_CUBE, Math.PI),
            new SnapPosition(BLUE_X, BOTTOM_TO_LOWEST_CONE + CONE_SPAN_ACROSS_CUBE, Math.PI),
            new SnapPosition(BLUE_X, BOTTOM_TO_LOWEST_CONE + CONE_SPAN_ACROSS_CUBE + CONE_SPAN_ADJACENT, Math.PI),
            new SnapPosition(BLUE_X, BOTTOM_TO_LOWEST_CUBE + CUBE_SPAN, Math.PI),
            new SnapPosition(BLUE_X, BOTTOM_TO_LOWEST_CONE + 2 * CONE_SPAN_ACROSS_CUBE + CONE_SPAN_ADJACENT, Math.PI),
            new SnapPosition(BLUE_X, BOTTOM_TO_LOWEST_CONE + 2 * CONE_SPAN_ACROSS_CUBE + 2 * CONE_SPAN_ADJACENT, Math.PI),
            new SnapPosition(BLUE_X, BOTTOM_TO_LOWEST_CUBE + 2 * CUBE_SPAN, Math.PI),
            new SnapPosition(BLUE_X, BOTTOM_TO_LOWEST_CONE + 3 * CONE_SPAN_ACROSS_CUBE + 2 * CONE_SPAN_ADJACENT, Math.PI),

            // Substation  FIXME
            new SnapPosition(Units.metersToInches(15), Units.metersToInches(7), 0)
    };

    public static final Translation2d CUBE_UPPER = new Translation2d(1.501206, 1.393604 - 0.15 - Units.inchesToMeters(6));
    public static final Translation2d CUBE_CENTER = new Translation2d(1.436464 - 0.15, 0.575931);
    public static final Translation2d CONE_UPPER = new Translation2d(1.42892 + Units.inchesToMeters(3), 1.533184 + Units.inchesToMeters(2) - Units.inchesToMeters(18));
    public static final Translation2d CONE_CENTER = new Translation2d(1.076355, 0.872756);

    public static final Translation2d CONE_PICKUP = new Translation2d(1.3157, 0.7451 + 0.1);
    public static final Translation2d CUBE_PICKUP = new Translation2d(1.3157, 0.7451 + 0.075);
    public static final Translation2d PICKUP_PRE = new Translation2d(
            (CONE_PICKUP.getX() + CUBE_PICKUP.getX()) / 2,
            Math.max(CONE_PICKUP.getY(), CUBE_PICKUP.getY()) + 0.15
    );

    public static final Translation2d DEFAULT = new Translation2d(0.689397, Units.inchesToMeters(11.5 - 13 + 2.25));

    public static SnapStatus getSnap(Pose2d currentPose) {
        double minDist = Double.POSITIVE_INFINITY;
        SnapPosition closest = null;
        for (SnapPosition pos : POSITIONS) {
            Pose2d pose = pos.getPose();
            double dist = pose.getTranslation().getDistance(currentPose.getTranslation());

            if (dist < minDist) {
                closest = pos;
                minDist = dist;
            }
        }

        System.out.println(minDist);
        if (closest == null || minDist > SNAP_RADIUS)
            return new SnapStatus(null, null);

        Pose2d pose = closest.getPose();
        double absDiff = CCWAngle.rad(currentPose.getRotation().getRadians())
                .getAbsDiff(CCWAngle.rad(pose.getRotation().getRadians()))
                .deg();

        Rotation2d snapAngle = absDiff < ANGLE_SNAP_TOL ? pose.getRotation() : null;

        return new SnapStatus(pose.getTranslation(), snapAngle);
    }

    public static void showPositions(Field2d field) {
        FieldObject2d object = field.getObject("Snap positions");

        Pose2d[] poses = new Pose2d[POSITIONS.length];
        for (int i = 0; i < poses.length; i++) {
            poses[i] = POSITIONS[i].getPose();
        }

        object.setPoses(poses);
    }

    private SnapPositions() {
        throw new AssertionError();
    }
}

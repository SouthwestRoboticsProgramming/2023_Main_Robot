package com.swrobotics.robot.positions;

import com.swrobotics.mathlib.CCWAngle;
import com.swrobotics.robot.subsystems.drive.DrivetrainSubsystem;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.FieldObject2d;

public final class SnapPositions {
  // Activation tolerances
  private static final double SNAP_RADIUS = 1.5; // Meters; how close to target to enable snap
  private static final double ANGLE_SNAP_TOL =
      22.5; // Degrees; how close to correct to enable snap turn

  // All field measurements are in inches to be consistent with field drawings
  // Position parameters
  private static final double ROBOT_SIZE_FW = 33;
  private static final double SAFETY_SPACING = 1;
  private static final double CENTER_DIST_FROM_WALL = SAFETY_SPACING + ROBOT_SIZE_FW / 2;

  // Grid measurements
  private static final double BOTTOM_TO_LOWEST_CONE = 20.19;
  private static final double CONE_SPAN_ACROSS_CUBE = 44;
  private static final double CONE_SPAN_ADJACENT = 22;
  private static final double BOTTOM_TO_LOWEST_CUBE = 42.19;
  private static final double CUBE_SPAN = 66;
  private static final double BLUE_RIGHT_X = 4 * 12 + 6.25;
  private static final double GRID_X = BLUE_RIGHT_X + CENTER_DIST_FROM_WALL;

  // Substation measurements
  private static final double FIELD_WIDTH = 54 * 12 + 1;
  private static final double SUBSTATION_LOWER_Y = 216.03;
  private static final double SUBSTATION_DEPTH = 14;
  private static final double SUBSTATION_X = FIELD_WIDTH - SUBSTATION_DEPTH - CENTER_DIST_FROM_WALL;
  private static final double SUBSTATION_AVAIL_AREA = 34.21;
  private static final double SUBSTATION_LENGTH = 99.07;
  private static final double LOW_SUBSTATION_Y = SUBSTATION_LOWER_Y + SUBSTATION_AVAIL_AREA / 2;
  private static final double HIGH_SUBSTATION_Y =
      SUBSTATION_LOWER_Y + SUBSTATION_LENGTH - SUBSTATION_AVAIL_AREA / 2;

  public enum TurnMode {
    DIRECT_TURN, // Turn directly to pose angle
    CONE_NODE_AIM, // Limelight aim to cone node pole
    GAME_PIECE_AIM // Limelight aim to game piece
  }

  public static final class SnapPosition {
    private final Pose2d bluePose;
    private final boolean driveSnapEnabled;
    private final TurnMode turnMode;

    public SnapPosition(
        double x, double y, double rot, boolean driveSnapEnabled, TurnMode turnMode) {
      bluePose = new Pose2d(Units.inchesToMeters(x), Units.inchesToMeters(y), new Rotation2d(rot));
      this.driveSnapEnabled = driveSnapEnabled;
      this.turnMode = turnMode;
    }

    public Pose2d getPose() {
      return DrivetrainSubsystem.FIELD.flipPoseForAlliance(bluePose);
    }
  }

  private static SnapPosition cubeNode(double y) {
    return new SnapPosition(GRID_X, y, Math.PI, true, TurnMode.DIRECT_TURN);
  }

  private static SnapPosition coneNode(double y) {
    return new SnapPosition(GRID_X, y, Math.PI, true, TurnMode.CONE_NODE_AIM);
  }

  private static SnapPosition substation(double y) {
    return new SnapPosition(SUBSTATION_X, y, 0, false, TurnMode.GAME_PIECE_AIM);
  }

  public static final SnapPosition[] POSITIONS = {
    // Grid positions
    coneNode(BOTTOM_TO_LOWEST_CONE),
    cubeNode(BOTTOM_TO_LOWEST_CUBE),
    coneNode(BOTTOM_TO_LOWEST_CONE + CONE_SPAN_ACROSS_CUBE),
    coneNode(BOTTOM_TO_LOWEST_CONE + CONE_SPAN_ACROSS_CUBE + CONE_SPAN_ADJACENT),
    cubeNode(BOTTOM_TO_LOWEST_CUBE + CUBE_SPAN),
    coneNode(BOTTOM_TO_LOWEST_CONE + 2 * CONE_SPAN_ACROSS_CUBE + CONE_SPAN_ADJACENT),
    coneNode(BOTTOM_TO_LOWEST_CONE + 2 * CONE_SPAN_ACROSS_CUBE + 2 * CONE_SPAN_ADJACENT),
    cubeNode(BOTTOM_TO_LOWEST_CUBE + 2 * CUBE_SPAN),
    coneNode(BOTTOM_TO_LOWEST_CONE + 3 * CONE_SPAN_ACROSS_CUBE + 2 * CONE_SPAN_ADJACENT),

    // Double substation
    substation(LOW_SUBSTATION_Y),
    substation(HIGH_SUBSTATION_Y)
  };

  public static final class SnapStatus {
    public final Pose2d pose;
    public final boolean snapDrive, snapTurn;
    public final TurnMode turnMode;

    public SnapStatus(Pose2d pose, TurnMode turnMode, boolean snapDrive, boolean snapTurn) {
      this.pose = pose;
      this.turnMode = turnMode;
      this.snapDrive = snapDrive;
      this.snapTurn = snapTurn;
    }

    @Override
    public String toString() {
      return "SnapStatus{"
          + "pose="
          + pose
          + ","
          + "turnMode="
          + turnMode
          + ","
          + "snapDrive="
          + snapDrive
          + ","
          + "snapTurn="
          + snapTurn
          + "}";
    }
  }

  /**
   * Get current snap behavior based on robot pose
   *
   * @param currentPose current robot pose
   * @return current snap behavior, or null if not in a snap zone
   */
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

    if (closest == null || minDist > SNAP_RADIUS) return null;

    Pose2d pose = closest.getPose();
    double absDiff =
        CCWAngle.rad(currentPose.getRotation().getRadians())
            .getAbsDiff(CCWAngle.rad(pose.getRotation().getRadians()))
            .deg();

    return new SnapStatus(pose, closest.turnMode, false, absDiff < ANGLE_SNAP_TOL);
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

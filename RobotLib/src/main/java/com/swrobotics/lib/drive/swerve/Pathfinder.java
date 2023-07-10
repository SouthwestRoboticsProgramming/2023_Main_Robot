package com.swrobotics.lib.drive.swerve;

import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.messenger.client.MessengerClient;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import java.util.ArrayList;
import java.util.List;

/** Subsystem to request paths from the Pathfinder module. */
// TODO: Remove
public final class Pathfinder extends SubsystemBase {
    private static final String MSG_SET_POS = "Pathfinder:SetPos";
    private static final String MSG_SET_GOAL = "Pathfinder:SetGoal";
    private static final String MSG_PATH = "Pathfinder:Path";

    private static final double CORRECT_TARGET_TOL = 0.1524 + 0.1;

    private final MessengerClient msg;
    private final SwerveDrive drive;

    private final List<Vec2d> path;

    private double goalX, goalY;

    /**
     * Creates a new instance of the pathfinder subsystem.
     *
     * @param msg messenger client Pathfinder is connected to
     * @param drive drive base to get current pose from
     */
    public Pathfinder(MessengerClient msg, SwerveDrive drive) {
        this.msg = msg;
        this.drive = drive;
        path = new ArrayList<>();

        msg.addHandler(MSG_PATH, this::onPath);
    }

    /**
     * Sets the goal position for the path.
     *
     * @param x x position of goal in meters, field relative
     * @param y y position of goal in meters, field relative
     */
    public void setGoal(double x, double y) {
        goalX = x;
        goalY = y;
        msg.prepare(MSG_SET_GOAL).addDouble(x).addDouble(y).send();
    }

    // Gets whether the latest path has the correct target
    private boolean pathTargetCorrect() {
        if (path.isEmpty()) return false;

        Vec2d last = path.get(path.size() - 1);
        double dx = last.x - goalX;
        double dy = last.y - goalY;
        return dx * dx + dy * dy < CORRECT_TARGET_TOL * CORRECT_TARGET_TOL;
    }

    /**
     * Gets whether the latest path has the correct target. This will be false for a short amount of
     * time after setting a new goal, or continuously if the pathfinder is not running.
     *
     * @return whether the target is valid
     */
    public boolean isPathTargetValid() {
        return pathTargetCorrect();
    }

    /**
     * Gets the latest path as a list of field points. This path contains both the current pose and
     * the goal.
     *
     * @return path as list of points in meters
     */
    public List<Vec2d> getPath() {
        if (path == null || path.isEmpty()) return path;

        // Replace last point with actual target for more accuracy
        List<Vec2d> pathCopy = new ArrayList<>(path);
        if (!pathCopy.isEmpty()) {
            pathCopy.set(pathCopy.size() - 1, new Vec2d(goalX, goalY));
        }

        return pathCopy;
    }

    // Sends set position message
    private void setPosition(double x, double y) {
        msg.prepare(MSG_SET_POS).addDouble(x).addDouble(y).send();
    }

    // Handler for received path
    private void onPath(String type, MessageReader reader) {
        boolean pathValid = reader.readBoolean();
        if (!pathValid) {
            return;
        }

        int count = reader.readInt();
        path.clear();
        for (int i = 0; i < count; i++) {
            double x = reader.readDouble();
            double y = reader.readDouble();

            path.add(new Vec2d(x, y));
        }
    }

    @Override
    public void periodic() {
        // Send the current pose to the pathfinder
        Translation2d pos = drive.getPose().getTranslation();
        setPosition(pos.getX(), pos.getY());
    }
}

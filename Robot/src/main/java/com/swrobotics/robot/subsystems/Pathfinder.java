package com.swrobotics.robot.subsystems;

import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.messenger.client.MessengerClient;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import java.util.ArrayList;
import java.util.List;

public final class Pathfinder extends SubsystemBase {
    private static final String MSG_SET_POS = "Pathfinder:SetPos";
    private static final String MSG_SET_GOAL = "Pathfinder:SetGoal";
    private static final String MSG_PATH = "Pathfinder:Path";

    private final MessengerClient msg;
    private final DrivetrainSubsystem drive;

    private final List<Vec2d> path;
    private boolean pathValid;

    public Pathfinder(MessengerClient msg, DrivetrainSubsystem drive) {
        this.msg = msg;
        this.drive = drive;
        path = new ArrayList<>();

        msg.addHandler(MSG_PATH, this::onPath);
    }

    public void setGoal(double x, double y) {
        msg.prepare(MSG_SET_GOAL)
                .addDouble(x)
                .addDouble(y)
                .send();
    }

    public boolean isPathValid() {
        return pathValid;
    }

    public List<Vec2d> getPath() {
        return path;
    }

    private void setPosition(double x, double y) {
        msg.prepare(MSG_SET_POS)
                .addDouble(x)
                .addDouble(y)
                .send();
    }

    private void onPath(String type, MessageReader reader) {
        pathValid = reader.readBoolean();
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
        Translation2d pos = drive.getPose().getTranslation();
        setPosition(pos.getX(), pos.getY());
    }
}

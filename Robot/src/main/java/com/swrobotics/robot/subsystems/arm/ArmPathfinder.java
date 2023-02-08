package com.swrobotics.robot.subsystems.arm;

import com.swrobotics.mathlib.MathUtil;
import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.shared.arm.ArmPose;

import java.util.ArrayList;
import java.util.List;

public final class ArmPathfinder {
    private static final String MSG_ARM_SET_INFO = "Pathfinder:Arm:SetInfo";
    private static final String MSG_ARM_PATH = "Pathfinder:Arm:Path";

    private static final double CORRECT_TARGET_TOL = 0.2;

    public static Vec2d toStateSpaceVec(ArmPose pose) {
        return new Vec2d(
                pose.bottomAngle,
                MathUtil.wrap(pose.topAngle + Math.PI, 0, Math.PI * 2)
        );
    }

    private final MessengerClient msg;
    private final List<ArmPose> path;
    private ArmPose goal;

    public ArmPathfinder(MessengerClient msg) {
        this.msg = msg;
        path = new ArrayList<>();
        msg.addHandler(MSG_ARM_PATH, this::onPath);
        goal = new ArmPose(0, 0);
    }

    public void setInfo(ArmPose current, ArmPose target) {
        goal = target;
        msg.prepare(MSG_ARM_SET_INFO)
                .addDouble(current.bottomAngle)
                .addDouble(current.topAngle)
                .addDouble(target.bottomAngle)
                .addDouble(target.topAngle)
                .send();
    }

    public boolean isPathValid() {
        if (path.isEmpty())
            return false;

        ArmPose last = path.get(path.size() - 1);

        Vec2d lastVec = toStateSpaceVec(last);
        Vec2d goalVec = toStateSpaceVec(goal);
        double dist = lastVec.distanceToSq(goalVec);

        return dist < CORRECT_TARGET_TOL * CORRECT_TARGET_TOL;
    }

    public List<ArmPose> getPath() {
        return path;
    }

    private void onPath(String type, MessageReader reader) {
        boolean valid = reader.readBoolean();
        if (!valid) {
            return;
        }

        int count = reader.readInt();
        path.clear();
        for (int i = 0; i < count; i++) {
            double bottom = reader.readDouble();
            double top = reader.readDouble();

            path.add(new ArmPose(bottom, top));
        }
    }
}

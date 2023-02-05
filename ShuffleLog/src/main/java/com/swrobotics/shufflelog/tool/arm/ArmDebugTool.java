package com.swrobotics.shufflelog.tool.arm;

import com.swrobotics.mathlib.MathUtil;
import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.shufflelog.tool.ToolConstants;
import com.swrobotics.shufflelog.tool.ViewportTool;
import com.swrobotics.shufflelog.util.Cooldown;
import imgui.ImGui;
import imgui.flag.ImGuiDataType;
import imgui.type.ImDouble;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.List;

public final class ArmDebugTool extends ViewportTool {
    private static final String MSG_GET_STATESPACE = "Arm:GetStatespace";
    private static final String MSG_STATESPACE = "Arm:Statespace";
    private static final String MSG_ARM_PATH_UPDATE = "Arm:PathUpdate";

    private static final double MIN_BOTTOM_ANGLE = 0;
    private static final double MAX_BOTTOM_ANGLE = Math.PI;
    private static final double MIN_TOP_ANGLE = 0;
    private static final double MAX_TOP_ANGLE = 2 * Math.PI;

    private final MessengerClient msg;

    private boolean[][] stateSpace;
    private boolean hasStateSpace;
    private final Cooldown stateSpaceCooldown;

    private Vec2d current, target;
    private List<Vec2d> path;
    private boolean hasPath;

    private final ImDouble targetX, targetY;

    public ArmDebugTool(PApplet app, MessengerClient msg) {
        super(app, "Arm");
        this.msg = msg;

        current = new Vec2d(0, 0);
        target = new Vec2d(0, 0);
        path = new ArrayList<>();

        hasStateSpace = false;
        hasPath = false;
        stateSpaceCooldown = new Cooldown(ToolConstants.MSG_QUERY_COOLDOWN_TIME);

        msg.addHandler(MSG_STATESPACE, this::onStateSpace);
        msg.addHandler(MSG_ARM_PATH_UPDATE, this::onPathUpdate);

        targetX = new ImDouble(1);
        targetY = new ImDouble(1);
    }

    private void onStateSpace(String type, MessageReader reader) {
        int resolution = reader.readInt();
        stateSpace = new boolean[resolution][resolution];
        for (int bot = 0; bot < resolution; bot++) {
            for (int top = 0; top < resolution; top++) {
                boolean valid = reader.readBoolean();
                stateSpace[bot][top] = valid;
            }
        }
        hasStateSpace = true;
    }

    private void onPathUpdate(String type, MessageReader reader) {
        double currentBot = reader.readDouble();
        double currentTop = reader.readDouble();
        double targetBot = reader.readDouble();
        double targetTop = reader.readDouble();
        current = new Vec2d(currentBot, currentTop);
        target = new Vec2d(targetBot, targetTop);

        hasPath = reader.readBoolean();
        if (hasPath) {
            path.clear();
            int count = reader.readInt();
            for (int i = 0; i < count; i++) {
                double x = reader.readDouble();
                double y = reader.readDouble();
                path.add(new Vec2d(x, y));
            }
        }
    }

    private Vec2d toSSPos(Vec2d pose, int res) {
        double bot = MathUtil.map(pose.x, MIN_BOTTOM_ANGLE, MAX_BOTTOM_ANGLE, 0, res - 1);
        double top = MathUtil.map(MathUtil.wrap(pose.y + Math.PI, 0, Math.PI * 2), MIN_TOP_ANGLE, MAX_TOP_ANGLE, 0, res - 1);
        return new Vec2d(bot, top);
    }

    @Override
    protected void drawViewportContent(PGraphics g) {
        g.background(0);

        if (!hasStateSpace && stateSpaceCooldown.request()) {
            msg.send(MSG_GET_STATESPACE);
            return;
        }

        if (!hasStateSpace)
            return;

        int res = stateSpace.length;
        g.scale(4);
        for (int bot = 0; bot < res; bot++) {
            for (int top = 0; top < res; top++) {
                g.noStroke();
                g.fill(stateSpace[bot][top] ? 255 : 128);
                g.rect(bot, top, 1, 1);
            }
        }

        Vec2d ssCurrent = toSSPos(current, res);
        Vec2d ssTarget = toSSPos(target, res);
        ImGui.text("current " + ssCurrent);
        ImGui.text("target " + ssTarget);

        g.strokeWeight(2);
        g.stroke(255, 255, 0);
        if (hasPath) {
            g.beginShape(PConstants.LINE_STRIP);
            for (Vec2d pos : path) {
                Vec2d ssPos = toSSPos(pos, res);
                g.vertex((float) ssPos.x, (float) ssPos.y);
            }
            g.endShape();
        }

        g.stroke(0, 255, 0);
        g.strokeWeight(4f);
        g.point((float) ssCurrent.x, (float) ssCurrent.y);

        g.stroke(128, 128, 255);
        g.point((float) ssTarget.x, (float) ssTarget.y);
    }

    @Override
    protected void drawGuiContent() {
        ImGui.text("Has statespace: " + hasStateSpace);

        boolean changed = false;
        changed |= ImGui.dragScalar("target x", ImGuiDataType.Double, targetX, 0.01f);
        changed |= ImGui.dragScalar("target y", ImGuiDataType.Double, targetY, 0.01f);

        if (changed) {
            msg.prepare("Debug:ArmSetTarget")
                    .addDouble(targetX.get())
                    .addDouble(targetY.get())
                    .send();
        }

        drawViewport();
    }
}

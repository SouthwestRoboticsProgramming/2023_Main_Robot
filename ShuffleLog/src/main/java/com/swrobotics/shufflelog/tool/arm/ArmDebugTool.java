package com.swrobotics.shufflelog.tool.arm;

import com.swrobotics.mathlib.MathUtil;
import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.shufflelog.tool.ToolConstants;
import com.swrobotics.shufflelog.tool.ViewportTool;
import com.swrobotics.shufflelog.tool.field.path.grid.BitfieldGrid;
import com.swrobotics.shufflelog.util.Cooldown;
import imgui.ImGui;
import imgui.flag.ImGuiDataType;
import imgui.type.ImDouble;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ArmDebugTool extends ViewportTool {
    private static final String MSG_ARM_SET_INFO = "Pathfinder:Arm:SetInfo";
    private static final String MSG_ARM_PATH = "Pathfinder:Arm:Path";
    private static final String MSG_ARM_GET_GRID = "Pathfinder:Arm:GetGrid";
    private static final String MSG_ARM_GRID = "Pathfinder:Arm:Grid";

    private static final double MIN_BOTTOM_ANGLE = 0;
    private static final double MAX_BOTTOM_ANGLE = Math.PI;
    private static final double MIN_TOP_ANGLE = 0;
    private static final double MAX_TOP_ANGLE = 2 * Math.PI;

    private static final float SCALE = 4;

    private final MessengerClient msg;

    private BitfieldGrid grid;
    private boolean hasGrid;
    private final Cooldown gridCooldown;

    private final Vec2d current, target;
    private final List<Vec2d> path;
    private boolean hasPath;

    private final ImDouble targetX, targetY;

    public ArmDebugTool(PApplet app, MessengerClient msg) {
        super(app, "Arm");
        this.msg = msg;

        current = new Vec2d(0, 0);
        target = new Vec2d(0, 0);
        path = new ArrayList<>();

        hasGrid = false;
        hasPath = false;
        gridCooldown = new Cooldown(ToolConstants.MSG_QUERY_COOLDOWN_TIME);

        msg.addHandler(MSG_ARM_SET_INFO, this::onSetInfo);
        msg.addHandler(MSG_ARM_PATH, this::onPath);
        msg.addHandler(MSG_ARM_GRID, this::onGrid);

        msg.addDisconnectHandler(() -> {
            hasGrid = false;
            hasPath = false;
        });

        targetX = new ImDouble(1);
        targetY = new ImDouble(1);
    }

    private void onSetInfo(String type, MessageReader reader) {
        double currentX = reader.readDouble();
        double currentY = reader.readDouble();
        double targetX = reader.readDouble();
        double targetY = reader.readDouble();
        current.set(currentX, currentY);
        target.set(targetX, targetY);
    }

    private void onPath(String type, MessageReader reader) {
        hasPath = reader.readBoolean();
        if (!hasPath)
            return;

        int count = reader.readInt();
        path.clear();
        for (int i = 0; i < count; i++) {
            double bot = reader.readDouble();
            double top = reader.readDouble();
            path.add(new Vec2d(bot, top));
        }
    }

    private void onGrid(String type, MessageReader reader) {
        grid = new BitfieldGrid(new UUID(0, 0));
        grid.readContent(reader);
        hasGrid = true;
    }

    private Vec2d toSSPos(Vec2d pose, int res) {
        double bot = MathUtil.map(pose.x, MIN_BOTTOM_ANGLE, MAX_BOTTOM_ANGLE, 0, res - 1);
        double top = MathUtil.map(MathUtil.wrap(pose.y + Math.PI, 0, Math.PI * 2), MIN_TOP_ANGLE, MAX_TOP_ANGLE, 0, res - 1);
        return new Vec2d(bot, top);
    }

    @Override
    protected void drawViewportContent(PGraphics g) {
        g.background(0);
        if (!msg.isConnected())
            return;

        int res = grid.getWidth();
        g.scale(SCALE);
        for (int bot = 0; bot < res; bot++) {
            for (int top = 0; top < res; top++) {
                g.noStroke();
                g.fill(grid.get(bot, top) ? 255 : 128);
                g.rect(bot, top, 1, 1);
            }
        }

        Vec2d ssCurrent = toSSPos(current, res);
        Vec2d ssTarget = toSSPos(target, res);
        ImGui.text("Current " + ssCurrent);
        ImGui.text("Target " + ssTarget);

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
        if (!msg.isConnected()) {
            ImGui.textDisabled("Not connected");
            return;
        }

        ImGui.text("Has statespace: " + hasGrid);

        boolean changed = false;
        changed |= ImGui.dragScalar("target x", ImGuiDataType.Double, targetX, 0.01f);
        changed |= ImGui.dragScalar("target y", ImGuiDataType.Double, targetY, 0.01f);

        if (changed) {
            msg.prepare("Debug:ArmSetTarget")
                    .addDouble(targetX.get())
                    .addDouble(targetY.get())
                    .send();
        }

        if (!hasGrid && gridCooldown.request()) {
            msg.send(MSG_ARM_GET_GRID);
            return;
        }

        if (!hasGrid)
            return;

        int res = grid.getWidth();
        drawViewport(res * SCALE, res * SCALE);
    }
}

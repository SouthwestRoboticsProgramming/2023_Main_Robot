package com.swrobotics.shufflelog.tool.pathfinder;

import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.shufflelog.ShuffleLog;
import com.swrobotics.shufflelog.math.Vector2i;
import com.swrobotics.shufflelog.tool.ToolConstants;
import com.swrobotics.shufflelog.tool.ViewportTool;
import com.swrobotics.shufflelog.util.Cooldown;
import edu.wpi.first.math.util.Units;
import imgui.ImGui;
import imgui.ImVec2;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.util.ArrayList;
import java.util.List;

import static com.swrobotics.mathlib.MathUtil.*;

public final class PathfinderTool extends ViewportTool {
    private static final String MSG_CALC = "Pathfinding:Calc";
    private static final String MSG_PATH = "Pathfinding:Path";
    private static final String MSG_GET_INFO = "Pathfinding:GetInfo";
    private static final String MSG_INFO = "Pathfinding:Info";

    private static final float BOTTOM_LEN = (float) Units.inchesToMeters(35.0);
    private static final float TOP_LEN = (float) Units.inchesToMeters(29.95824774);

    private static final class Rectangle {
        double x, y, width, height;
        double rotation;
        boolean inverted;
    }

    private static final class StateSpaceInfo {
        int width, height;
        double minBottom, maxBottom;
        double minTop, maxTop;
        double frameSize;
        Rectangle[] collisionRects;

        boolean[][] passable;
    }

    private final MessengerClient msg;
    private final Cooldown getInfoCooldown;

    private StateSpaceInfo info;
    private final Vec2d startPose, goalPose;
    private final List<Vec2d> path;

    public PathfinderTool(ShuffleLog app) {
        super(app, "Pathfinder");

        msg = app.getMessenger();
        getInfoCooldown = new Cooldown(ToolConstants.MSG_QUERY_COOLDOWN_TIME);
        info = null;
        startPose = new Vec2d(0, 0);
        goalPose = new Vec2d(0, 0);
        path = new ArrayList<>();

        msg.addHandler(MSG_CALC, this::onCalc);
        msg.addHandler(MSG_PATH, this::onPath);
        msg.addHandler(MSG_INFO, this::onInfo);
    }

    private void onCalc(String type, MessageReader reader) {
        double startBot = reader.readDouble();
        double startTop = reader.readDouble();
        double goalBot = reader.readDouble();
        double goalTop = reader.readDouble();

        startPose.set(startBot, startTop);
        goalPose.set(goalBot, goalTop);
    }

    private void onPath(String type, MessageReader reader) {
        path.clear();

        boolean good = reader.readBoolean();
        if (!good)
            return;

        int len = reader.readInt();
        for (int i = 0; i < len; i++) {
            double bottom = reader.readDouble();
            double top = reader.readDouble();
            path.add(new Vec2d(bottom, top));
        }
    }

    private void onInfo(String type, MessageReader reader) {
        info = new StateSpaceInfo();

        info.width = reader.readInt();
        info.height = reader.readInt();
        info.minBottom = reader.readDouble();
        info.maxBottom = reader.readDouble();
        info.minTop = reader.readDouble();
        info.maxTop = reader.readDouble();
        info.frameSize = reader.readDouble();

        int rectCount = reader.readInt();
        info.collisionRects = new Rectangle[rectCount];
        for (int i = 0; i < rectCount; i++) {
            Rectangle rect = new Rectangle();
            rect.x = reader.readDouble();
            rect.y = reader.readDouble();
            rect.width = reader.readDouble();
            rect.height = reader.readDouble();
            rect.rotation = reader.readDouble();
            rect.inverted = reader.readBoolean();
            info.collisionRects[i] = rect;
        }

        info.passable = new boolean[info.width][info.height];
        for (int y = 0; y < info.height; y++) {
            for (int x = 0; x < info.width; x++) {
                info.passable[x][y] = reader.readBoolean();
            }
        }
    }

    private Vector2i poseToState(Vec2d pose) {
        int x = (int) (info.width * percent(wrap(pose.x, info.minBottom, info.maxBottom), info.minBottom, info.maxBottom));
        int y = (int) (info.height * percent(wrap(pose.y, info.minTop, info.maxTop), info.minTop, info.maxTop));
        return new Vector2i(x, y);
    }

    @Override
    protected void drawGuiContent() {
        if (!msg.isConnected()) {
            ImGui.textDisabled("Not connected");
            return;
        }

        if (info == null) {
            if (getInfoCooldown.request())
                msg.send(MSG_GET_INFO);

            ImGui.textDisabled("Fetching info...");
            return;
        }

        Vector2i startState = poseToState(startPose);
        Vector2i goalState = poseToState(goalPose);
        ImGui.text("Current: " + new Vec2d(startPose).componentMap(Math::toDegrees) + ", state " + startState);
        ImGui.text("Goal: " + new Vec2d(goalPose).componentMap(Math::toDegrees) + ", state " + goalState);
        ImGui.text("Path: " + (path.isEmpty() ? "NO" : (path.size() + " Points")));

        drawViewport();
    }

    @Override
    protected void drawViewportContent(PGraphics g) {
        g.background(0);

        // TODO: Maybe store in an image instead of rects
        g.noStroke();
        for (int x = 0; x < info.width; x++) {
            for (int y = 0; y < info.height; y++) {
                if (info.passable[x][y]) {
                    g.fill(255);
                } else {
                    g.fill(255, 0, 0);
                }
                g.rect(x, y, 1, 1);
            }
        }

        if (!path.isEmpty()) {
            g.stroke(255, 128, 0);
            g.strokeWeight(4);
            g.beginShape(PConstants.LINE_STRIP);
            for (Vec2d pose : path) {
                Vector2i state = poseToState(pose);
                g.vertex(state.x, state.y);
            }
            g.endShape();
        }

        Vector2i startState = poseToState(startPose);
        Vector2i goalState = poseToState(goalPose);
        g.strokeWeight(7);
        g.stroke(0, 255, 0);
        g.point(startState.x, startState.y);
        g.stroke(0, 0, 255);
        g.point(goalState.x, goalState.y);

        g.translate((info.width + g.width) / 2f, g.height / 2f);
        int minDim = Math.min(g.width - info.width, g.height);
        float scale = minDim / 2f;
        float strokeMul = 1 / scale;
        g.scale(scale, -scale);
        g.translate(0, -0.75f);

        ImVec2 windowPos = ImGui.getWindowPos();
        ImVec2 cursor = ImGui.getCursorPos();
        ImVec2 mouse = ImGui.getMousePos();
        float mouseX = mouse.x - (windowPos.x + cursor.x);
        float mouseY = mouse.y - (windowPos.y + cursor.y);

        Vec2d previewPose = startPose;
        if (mouseX >= 0 && mouseX < info.width && mouseY >= 0 && mouseY < info.height) {
            previewPose = new Vec2d(
                    lerp(info.minBottom, info.maxBottom, mouseX / (double) info.width),
                    lerp(info.minTop, info.maxTop, mouseY / (double) info.height)
            );
        }

        float midX = (float) Math.cos(previewPose.x) * BOTTOM_LEN;
        float midY = (float) Math.sin(previewPose.x) * BOTTOM_LEN;
        float axisX = midX + (float) Math.cos(previewPose.y) * TOP_LEN;
        float axisY = midY + (float) Math.sin(previewPose.y) * TOP_LEN;
        g.strokeWeight(6 * strokeMul);
        g.line(0, 0, midX, midY);
        g.line(midX, midY, axisX, axisY);
    }
}

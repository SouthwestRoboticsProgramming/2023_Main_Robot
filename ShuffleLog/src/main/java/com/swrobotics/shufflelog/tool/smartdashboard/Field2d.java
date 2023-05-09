package com.swrobotics.shufflelog.tool.smartdashboard;

import edu.wpi.first.networktables.NetworkTable;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import processing.core.PGraphics;

import java.util.HashMap;
import java.util.Map;

public final class Field2d {
    private static final double[] EMPTY_POSE = {};

    private enum LineStyle {
        BOX("Box"),
        // TODO: These styles
//        LINE("Line"),
//        CLOSED_LINE("Line (closed)"),
//        TRACK("Track"),
        HIDDEN("Hidden");

        private static final String[] NAMES;
        static {
            LineStyle[] values = values();
            NAMES = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                NAMES[i] = values[i].name;
            }
        }

        private final String name;
        LineStyle(String name) {
            this.name = name;
        }
    }

    private static final class PoseStyle {
        private float boxWidth = 0.686f;
        private float boxLength = 0.82f;

        private LineStyle lineStyle = LineStyle.BOX;
        private float lineWeight = 4;
        private float[] lineColor = {1, 0, 0, 1};

        private boolean arrows = true;
        private float arrowScale = 0.5f;
        private float arrowWeight = 4;
        private float[] arrowColor = {0, 1, 0, 1};
    }

    private final String name;
    private final NetworkTable table;
    private final Map<String, PoseStyle> styles; // TODO: Load from persistence

    public Field2d(String name, NetworkTable table) {
        this.name = name;
        this.table = table;
        styles = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    private final ImInt tempInt = new ImInt();
    private final ImFloat tempFloat = new ImFloat();
    private final ImBoolean tempBool = new ImBoolean();

    public void editStyle(PoseStyle style) {
        tempInt.set(style.lineStyle.ordinal());
        if (ImGui.combo("", tempInt, LineStyle.NAMES))
            style.lineStyle = LineStyle.values()[tempInt.get()];

        if (style.lineStyle == LineStyle.BOX) {
            tempFloat.set(style.boxWidth);
            if (ImGui.inputFloat("##boxWidth", tempFloat))
                style.boxWidth = tempFloat.get();

            tempFloat.set(style.boxLength);
            if (ImGui.inputFloat("##boxLength", tempFloat))
                style.boxLength = tempFloat.get();
        }

        tempFloat.set(style.lineWeight);
        if (ImGui.inputFloat("##lineWeight", tempFloat))
            style.lineWeight = tempFloat.get();

        ImGui.colorEdit3("##lineColor", style.lineColor);

        tempBool.set(style.arrows);
        if (ImGui.checkbox("##arrows", tempBool))
            style.arrows = tempBool.get();

        if (style.arrows) {
            tempFloat.set(style.arrowScale);
            if (ImGui.inputFloat("##arrowScale", tempFloat))
                style.arrowScale = tempFloat.get();

            tempFloat.set(style.arrowWeight);
            if (ImGui.inputFloat("##arrowWeight", tempFloat))
                style.arrowWeight = tempFloat.get();

            ImGui.colorEdit3("##arrowColor", style.arrowColor);
        }
    }

    private void drawPose(PGraphics g, PoseStyle style, float x, float y, float rot) {
        g.pushMatrix();
        g.translate(x, y);
        g.rotate(rot);

        if (style.lineStyle == LineStyle.BOX) {
            g.strokeWeight(style.lineWeight);
            g.stroke(style.lineColor[0]*255, style.lineColor[1]*255, style.lineColor[2]*255);
            g.noFill();
            g.rect(-style.boxLength / 2, -style.boxWidth / 2, style.boxLength, style.boxWidth);
        }

        if (style.arrows) {
            g.strokeWeight(style.arrowWeight);
            g.stroke(style.arrowColor[0]*255, style.arrowColor[1]*255, style.arrowColor[2]*255);
            g.noFill();

            float sz = style.arrowScale;
            g.triangle(-sz/2, -sz/2, sz/2, 0, -sz/2, sz/2);
        }

        g.popMatrix();
    }

    public void draw(PGraphics g) {
        for (String key : table.getKeys()) {
            if (key.startsWith("."))
                continue;

            PoseStyle style = styles.computeIfAbsent(key, (k) -> new PoseStyle());

            double[] poseData = table.getEntry(key).getDoubleArray(EMPTY_POSE);
            for (int i = 0; i < poseData.length / 3; i++) {
                double x = poseData[i];
                double y = poseData[i + 1];
                double rot = Math.toRadians(poseData[i + 2]);

                drawPose(g, style, (float) x, (float) y, (float) rot);
            }
        }
    }
}

package com.swrobotics.shufflelog.tool.field;

import imgui.ImGui;
import imgui.type.ImBoolean;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * Layer that shows the positions of field elements exactly as they are
 * indicated in the official field drawings.
 */
public final class FieldVectorLayer implements FieldLayer {
    private final ImBoolean show;

    public FieldVectorLayer() {
        show = new ImBoolean(true);
    }

    @Override
    public String getName() {
        return "Vector Field";
    }

    @Override
    public void draw(PGraphics g) {
        if (!show.get())
            return;

        // Transform to inches space to be consistent with FIRST drawings
        float inchesPerMeter = 39.3701f;
        g.scale(1/inchesPerMeter);

        float strokeMul = 1 / inchesPerMeter;

        g.strokeWeight(2 * strokeMul);
        g.stroke(255);

        // Annoyingly the terminals aren't exactly 45 degrees, they are 46.25 degrees
        float terminalEdgeLenY = 324 - 256.42f;
        float terminalEdgeLenX = (float) Math.tan(Math.toRadians(180 - 133.75)) * terminalEdgeLenY;

        // Walls
        g.line(-162, -324, 162 - terminalEdgeLenX, -324);
        g.line(-162, -324, -162, 324 - terminalEdgeLenY);
        g.line(162, -324 + terminalEdgeLenY, 162, 324);
        g.line(-162 + terminalEdgeLenX, 324, 162, 324);

        // Terminals
        g.stroke(0, 0, 255);
        g.line(162 - terminalEdgeLenX, -324, 162, -324 + terminalEdgeLenY);
        g.stroke(255, 0, 0);
        g.line(-162, 324 - terminalEdgeLenY, -162 + terminalEdgeLenX, 324);

        float hangarTrussSize = 13;
        float hangarTrussBaseSize = 24;
        float hangarTrussInset = hangarTrussBaseSize / 2 - hangarTrussSize / 2;
        float hangarBar1Y = 324 - 128.75f;
        float hangarBar2Y = hangarBar1Y + 41.98f;
        float hangarBar3Y = hangarBar2Y + 24;
        float hangarBar4Y = hangarBar3Y + 24;
        float hangarOuterRightX = 162 - 116;
        float hangarInnerRightX = hangarOuterRightX + hangarTrussSize;
        float hangarEdgeLen = 128.75f - hangarTrussInset;

        // Blue hangar
        g.stroke(0, 0, 255);
        g.strokeWeight(4 * strokeMul);
        g.line(-162, -hangarBar1Y, -hangarOuterRightX, -hangarBar1Y); // Tape line & bar 1
        g.line(-162 + hangarTrussInset, -hangarBar2Y, -hangarOuterRightX, -hangarBar2Y); // bar 2
        g.line(-162 + hangarTrussInset, -hangarBar3Y, -hangarOuterRightX, -hangarBar3Y); // bar 3
        g.line(-162 + hangarTrussInset, -hangarBar4Y, -hangarOuterRightX, -hangarBar4Y); // bar 4
        g.strokeWeight(2 * strokeMul);
        g.noFill();
        g.rect(-162 + hangarTrussInset, -324 + hangarTrussInset, hangarTrussSize, hangarTrussSize);
        g.rect(-162 + hangarTrussInset, -hangarBar1Y - hangarTrussSize, hangarTrussSize, hangarTrussSize);
        g.rect(-hangarInnerRightX, -324 + hangarTrussInset, hangarTrussSize, hangarTrussSize);
        g.rect(-hangarInnerRightX, -hangarBar1Y - hangarTrussSize, hangarTrussSize, hangarTrussSize);
        g.rect(-162 + hangarTrussInset, -324 + hangarTrussInset, hangarTrussSize, hangarEdgeLen); // Left side truss
        g.rect(-hangarInnerRightX, -324 + hangarTrussInset, hangarTrussSize, hangarEdgeLen);

        // Red hangar
        g.stroke(255, 0, 0);
        g.strokeWeight(4 * strokeMul);
        g.line(162, hangarBar1Y, hangarOuterRightX, hangarBar1Y); // Tape line & bar 1
        g.line(162 - hangarTrussInset, hangarBar2Y, hangarOuterRightX, hangarBar2Y); // bar 2
        g.line(162 - hangarTrussInset, hangarBar3Y, hangarOuterRightX, hangarBar3Y); // bar 3
        g.line(162 - hangarTrussInset, hangarBar4Y, hangarOuterRightX, hangarBar4Y); // bar 4
        g.strokeWeight(2 * strokeMul);
        g.noFill();
        g.rect(162 - hangarTrussInset - hangarTrussSize, 324 - hangarTrussInset - hangarTrussSize, hangarTrussSize, hangarTrussSize);
        g.rect(162 - hangarTrussInset - hangarTrussSize, hangarBar1Y, hangarTrussSize, hangarTrussSize);
        g.rect(hangarOuterRightX, 324 - hangarTrussInset - hangarTrussSize, hangarTrussSize, hangarTrussSize);
        g.rect(hangarOuterRightX, hangarBar1Y, hangarTrussSize, hangarTrussSize);
        g.rect(162 - hangarTrussInset - hangarTrussSize, 324 - hangarTrussInset - hangarEdgeLen, hangarTrussSize, hangarEdgeLen); // Left side truss
        g.rect(hangarOuterRightX, 324 - hangarTrussInset - hangarEdgeLen, hangarTrussSize, hangarEdgeLen);

        // Center line
        g.stroke(255, 255, 255);
        g.line(-162, -71.03f, 0, 0);
        g.strokeWeight(4 * strokeMul); // Wider stroke to represent cable shield
        g.line(0, 0, 162, 71.03f);

        final float sqrt2 = (float) Math.sqrt(2);
        final float halfSqrt2 = sqrt2 / 2;

        float startSpacing = 14.75f;
        float startSpacingEdge = startSpacing / 2;
        float startEdgeLen = 75.07f;
        float startWidth = 153;
        float startEdge = startWidth / sqrt2;
        float startInnerEdge = startEdge - startEdgeLen;
        float startPointDist = 84.75f + halfSqrt2 * (startEdge - startEdgeLen);
        float startPointEdge = startPointDist * halfSqrt2;

        float hubOutletInset = 62.1f;
        float hubOutletLen = startEdgeLen - hubOutletInset;

        g.rotate((float) Math.toRadians(180 - 66));

        g.pushMatrix();
        g.strokeWeight(2 * strokeMul);
        // Starting areas
        for (int i = 0; i < 4; i++) {
            g.pushMatrix();
            g.translate(-startSpacingEdge, startSpacingEdge);
            if (i >= 2)
                g.stroke(255, 0, 0);
            else
                g.stroke(0, 0, 255);

            g.line(0, startEdge, 0, startInnerEdge);
            g.line(-startEdge, 0, -startInnerEdge, 0);
            g.line(0, startEdge, -startPointEdge, startPointEdge);
            g.line(-startEdge, 0, -startPointEdge, startPointEdge);
            g.line(0, startInnerEdge, -startInnerEdge, 0);
            g.popMatrix();

            g.rotate((float) Math.PI / 2);
        }
        g.popMatrix();

        // Hub
        g.stroke(255);
        g.strokeWeight(3 * strokeMul);
        g.noFill();
        g.beginShape();
        g.vertex(-startSpacingEdge, startSpacingEdge + startInnerEdge + hubOutletLen);
        g.vertex(-startSpacingEdge, startSpacingEdge + startInnerEdge);
        g.vertex(-startSpacingEdge - startInnerEdge, startSpacingEdge);
        g.vertex(-startSpacingEdge - startInnerEdge - hubOutletLen, startSpacingEdge);
        g.vertex(-startSpacingEdge - startInnerEdge - hubOutletLen, -startSpacingEdge);
        g.vertex(-startSpacingEdge - startInnerEdge, -startSpacingEdge);
        g.vertex(-startSpacingEdge, -startSpacingEdge - startInnerEdge);
        g.vertex(-startSpacingEdge, -startSpacingEdge - startInnerEdge - hubOutletLen);
        g.vertex(startSpacingEdge, -startSpacingEdge - startInnerEdge - hubOutletLen);
        g.vertex(startSpacingEdge, -startSpacingEdge - startInnerEdge);
        g.vertex(startSpacingEdge + startInnerEdge, -startSpacingEdge);
        g.vertex(startSpacingEdge + startInnerEdge + hubOutletLen, -startSpacingEdge);
        g.vertex(startSpacingEdge + startInnerEdge + hubOutletLen, startSpacingEdge);
        g.vertex(startSpacingEdge + startInnerEdge, startSpacingEdge);
        g.vertex(startSpacingEdge, startSpacingEdge + startInnerEdge);
        g.vertex(startSpacingEdge, startSpacingEdge + startInnerEdge + hubOutletLen);
        g.endShape(PConstants.CLOSE);
        g.ellipseMode(PConstants.CENTER);
        g.ellipse(0, 0, 60.18f, 60.18f); // Lower hub inner diameter
        g.ellipse(0, 0, 48, 48); // Upper hub inner diameter

        /*
         *     \__
         * b1
         *    b2 b3
         */

        // Ball starting locations
        float startPointToLowerEdgeX = (-startSpacingEdge) - (-startSpacingEdge - startPointEdge);
        float startPointToLowerEdgeY = (startSpacingEdge + startEdge) - (startSpacingEdge + startPointEdge);
        float startPointToLowerEdgeLen = (float) Math.sqrt(startPointToLowerEdgeX * startPointToLowerEdgeX + startPointToLowerEdgeY * startPointToLowerEdgeY);
        startPointToLowerEdgeX /= startPointToLowerEdgeLen;
        startPointToLowerEdgeY /= startPointToLowerEdgeLen;

        // rotate vector 90 degrees clockwise
        // cx-sy; sx+cy
        float perpVectorX = -startPointToLowerEdgeY;
        float perpVectorY = startPointToLowerEdgeX;

        g.fill(0, 0, 255);
        g.noStroke();
        g.ellipseMode(PConstants.CENTER);
        g.translate(-startSpacingEdge - startPointEdge + 15.56f * startPointToLowerEdgeX + 40.44f * perpVectorX,
                startSpacingEdge + startPointEdge + 15.56f * startPointToLowerEdgeY + 40.44f * perpVectorY, 6);
        g.sphere(6);
    }

    @Override
    public void showGui() {
        ImGui.checkbox("Show", show);
    }
}

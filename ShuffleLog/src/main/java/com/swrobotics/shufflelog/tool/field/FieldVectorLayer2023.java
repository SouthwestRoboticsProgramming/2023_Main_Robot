package com.swrobotics.shufflelog.tool.field;

import edu.wpi.first.math.util.Units;
import imgui.ImGui;
import imgui.type.ImBoolean;
import processing.core.PConstants;
import processing.core.PGraphics;

public final class FieldVectorLayer2023 implements FieldLayer {
    float width = 54*12+1;
    float height = 216.03f + 99.07f;

    float centerX = width / 2;
    float centerY = height / 2;

    // Dimensions from official FIRST field drawings unless otherwise specified
    float gamePieceSpacingFromCenterX = 47.36f;
    float gamePieceSpacingFromCenterY = 22.39f;
    float gamePieceSpacing = 48;

    float doubleSubstationInnerFromLeft = 14; // Game manual pg. 30

    float loadingZoneInnerSpacingFromCenterX = 61.36f;
    float loadingZoneHorizSpacingFromTop = 50.50f;
    float loadingZoneOuterSpacingFromDoubleSubstationInner = 118.25f;
    float barrierSpacingFromTop = 99.07f;

    float communityLowerSpacingFromGamePieces = 85.13f;

    float gridInnerSpacingFromGamePieces = 224f;

    float gridInnerToChargeStationSpacing = 60.69f;
    float chargeStationLowerSpacing = 59.39f;
    float gridInnerToChargeStationCableProtectorCenterSpacing = 96.75f;
    float gridInnerToChargeStationCableProtectorOuterSpacing = 95.25f;

    float gridL1ToDoubleSubstationInnerSpacing = 26.19f;
    float communityHorizToGridTopCubeCenterYSpacing = 91.55f;
    float gridCubeSpacing = 66;
    float gridCubeWidth = 18.25f;

    // -----------------------------------------------------

    float barrierInnerEndpointX = doubleSubstationInnerFromLeft + loadingZoneOuterSpacingFromDoubleSubstationInner;
    float gridInnerX = centerX - gamePieceSpacingFromCenterX - gridInnerSpacingFromGamePieces;

    private final ImBoolean show = new ImBoolean(true);

    @Override
    public String getName() {
        return "Field Vector (2023)";
    }

    @Override
    public void draw(PGraphics g) {
        if (!show.get())
            return;

        float inchesScale = (float) (1 / Units.metersToInches(1));
        g.scale(inchesScale);
        float strokeMul = 1 / inchesScale;

        // Center line
        g.stroke(255);
        g.strokeWeight(2 * strokeMul);
        g.line(centerX, 0, centerX, height);

        // Game piece starting positions
        g.stroke(200);
        g.strokeWeight(4 * strokeMul);
        for (int i = 0; i < 4; i++) {
            float y = centerY + gamePieceSpacingFromCenterY - gamePieceSpacing * i;
            g.point(centerX - gamePieceSpacingFromCenterX, y);
            g.point(centerX + gamePieceSpacingFromCenterX, y);
        }

        drawFieldHalf(g, false, strokeMul);

        // Flip around center
        g.pushMatrix();
        g.scale(-1, 1);
        g.translate(-width, 0);

        drawFieldHalf(g, true, strokeMul);

        g.popMatrix();
    }

    private void red(PGraphics g, boolean flipColor) {
        if (flipColor)
            g.stroke(0, 0, 255);
        else
            g.stroke(255, 0, 0);
    }

    private void blue(PGraphics g, boolean flipColor) {
        if (flipColor)
            g.stroke(255, 0, 0);
        else
            g.stroke(0, 0, 255);
    }

    private void drawFieldHalf(PGraphics g, boolean flipColor, float strokeMul) {
        // Red loading zone tape
        red(g, flipColor);
        g.strokeWeight(2 * strokeMul);
        drawLoadingZoneBorderTape(g);

        // Blue charge station
        float chargeStationX = gridInnerX + gridInnerToChargeStationSpacing;
        g.stroke(255);
        g.strokeWeight(strokeMul);
        g.noFill();
        g.rect(
                chargeStationX,
                chargeStationLowerSpacing,
                (centerX - gamePieceSpacingFromCenterX - communityLowerSpacingFromGamePieces) - chargeStationX,
                centerY - chargeStationLowerSpacing
        );
        g.noStroke();
        g.fill(128);
        float cableProtectorWidth = (gridInnerToChargeStationCableProtectorCenterSpacing - gridInnerToChargeStationCableProtectorOuterSpacing) * 2;
        g.rect(gridInnerX + gridInnerToChargeStationCableProtectorOuterSpacing, 0, cableProtectorWidth, chargeStationLowerSpacing);

        // Blue community tape
        blue(g, flipColor);
        g.strokeWeight(2 * strokeMul);
        drawCommunityBorderTape(g);

        // Grid front tape
        g.line(gridInnerX, height - barrierSpacingFromTop, gridInnerX, 0);

        float l1x = doubleSubstationInnerFromLeft + gridL1ToDoubleSubstationInnerSpacing;
        float topCubeCenterY = height - loadingZoneHorizSpacingFromTop - communityHorizToGridTopCubeCenterYSpacing;

        // Cone nodes
        g.strokeWeight(strokeMul);
        float barrierY = height - barrierSpacingFromTop;
        blue(g, flipColor);
        if (flipColor) g.fill(128, 0, 0); else g.fill(0, 0, 128);
        float topConeRectH = gridCubeSpacing / 2 + (barrierY - topCubeCenterY);
        g.rect(0, topCubeCenterY - gridCubeSpacing / 2, l1x, topConeRectH);

        g.fill(64);
        g.stroke(128);
        g.rect(0, topCubeCenterY - gridCubeSpacing / 2 - gridCubeSpacing, l1x, gridCubeSpacing);

        blue(g, flipColor);
        if (flipColor) g.fill(128, 0, 0); else g.fill(0, 0, 128);
        g.rect(0, 0, l1x, topCubeCenterY - gridCubeSpacing / 2 - gridCubeSpacing);

        // Cube nodes
        g.fill(128);
        g.stroke(255);
        for (int i = 0; i < 3; i++) {
            float centerY = topCubeCenterY - i * gridCubeSpacing;
            g.rect(0, centerY - gridCubeWidth/2f, l1x, gridCubeWidth);
        }

        // Barrier
        g.stroke(255);
        g.strokeWeight(strokeMul);
        g.line(0, barrierY, barrierInnerEndpointX, barrierY);
    }

    private void drawLoadingZoneBorderTape(PGraphics g) {
        g.beginShape(PConstants.LINE_STRIP);
        g.vertex(centerX - loadingZoneInnerSpacingFromCenterX, height);
        g.vertex(centerX - loadingZoneInnerSpacingFromCenterX, height - loadingZoneHorizSpacingFromTop);
        g.vertex(doubleSubstationInnerFromLeft + loadingZoneOuterSpacingFromDoubleSubstationInner, height - loadingZoneHorizSpacingFromTop);
        g.vertex(doubleSubstationInnerFromLeft + loadingZoneOuterSpacingFromDoubleSubstationInner, height - barrierSpacingFromTop);
        g.endShape();
    }

    private void drawCommunityBorderTape(PGraphics g) {
        g.beginShape(PConstants.LINE_STRIP);
        g.vertex(barrierInnerEndpointX, height - barrierSpacingFromTop);
        g.vertex(barrierInnerEndpointX, centerY);
        float innerX = centerX - gamePieceSpacingFromCenterX - communityLowerSpacingFromGamePieces;
        g.vertex(innerX, centerY);
        g.vertex(innerX, 0);
        g.endShape();
    }

    @Override
    public void showGui() {
        ImGui.checkbox("Show", show);
    }
}

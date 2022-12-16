package com.swrobotics.shufflelog.tool.field;

import imgui.ImGui;
import imgui.type.ImBoolean;
import processing.core.PGraphics;

public final class MeterGridLayer implements FieldLayer {
    private final ImBoolean show = new ImBoolean(true);

    @Override
    public String getName() {
        return "Grid";
    }

    @Override
    public void draw(PGraphics g, float metersScale) {
        if (!show.get())
            return;

        float width = (float) FieldViewTool.WIDTH;
        float height = (float) FieldViewTool.HEIGHT;

        g.stroke(64);
        g.strokeWeight(1);
        for (float x = 0; x < width/2; x += 1) {
            g.line(x, -height/2, x, height/2);
            g.line(-x, -height/2, -x, height/2);
        }
        for (float y = 0; y < height/2; y += 1) {
            g.line(-width/2, y, width/2, y);
            g.line(-width/2, -y, width/2, -y);
        }
        g.noFill();
        g.rect(-width/2, -height/2, width, height);
    }

    @Override
    public void showGui() {
        ImGui.checkbox("Show", show);
    }
}

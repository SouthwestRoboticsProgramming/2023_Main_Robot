package com.swrobotics.shufflelog.tool;

import static processing.core.PConstants.P2D;

import imgui.ImGui;
import imgui.ImVec2;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.opengl.PGraphicsOpenGL;

public abstract class ViewportTool implements Tool {
    private final PApplet app;
    private final String title;
    private final int windowFlags;
    private final String renderer;
    private int pTexture;
    private PGraphicsOpenGL g;

    public ViewportTool(PApplet app, String title) {
        this(app, title, 0);
    }

    public ViewportTool(PApplet app, String title, int windowFlags) {
        this(app, title, windowFlags, P2D);
    }

    public ViewportTool(PApplet app, String title, int windowFlags, String renderer) {
        this.app = app;
        this.title = title;
        this.windowFlags = windowFlags;
        this.renderer = renderer;
        pTexture = -1;
    }

    protected abstract void drawViewportContent(PGraphics g);

    private boolean prepareGraphics(int w, int h) {
        if (g == null || g.width != w || g.height != h) {
            g = (PGraphicsOpenGL) app.createGraphics(w, h, renderer);
            return false;
        }
        return true;
    }

    protected final void drawViewport() {
        ImVec2 size = ImGui.getContentRegionAvail();
        drawViewport(size.x, size.y, true);
    }

    protected final void drawViewport(float w, float h) {
        drawViewport(w, h, true);
    }

    protected final void drawViewport(float w, float h, boolean blockEvents) {
        if (w > 0 && h > 0) {
            boolean shouldShowThisFrame = prepareGraphics((int) w, (int) h);

            g.beginDraw();
            g.textFont(app.getGraphics().textFont);
            drawViewportContent(g);
            g.endDraw();

            // PGraphicsOpenGL seems to require one frame to get started, so
            // use the previous frame if we just created a new graphics object
            int texId;
            if (shouldShowThisFrame) texId = g.getTexture().glName;
            else texId = pTexture;

            if (texId != -1) {
                if (blockEvents) ImGui.imageButton(texId, w, h, 0, 1, 1, 0, 0);
                else ImGui.image(texId, w, h, 0, 1, 1, 0);
            }

            pTexture = texId;
        }
    }

    protected void drawGuiContent() {
        drawViewport();
    }

    @Override
    public void process() {
        if (ImGui.begin(title, windowFlags)) {
            drawGuiContent();
        }
        ImGui.end();
    }
}

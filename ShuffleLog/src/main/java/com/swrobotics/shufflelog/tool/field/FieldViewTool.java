package com.swrobotics.shufflelog.tool.field;

import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.shufflelog.ShuffleLog;
import com.swrobotics.shufflelog.math.*;
import com.swrobotics.shufflelog.tool.ViewportTool;
import com.swrobotics.shufflelog.tool.field.path.PathfindingLayer;
import com.swrobotics.shufflelog.tool.field.tag.TagTrackerLayer;
import com.swrobotics.shufflelog.tool.field.waypoint.WaypointLayer;
import com.swrobotics.shufflelog.util.SmoothFloat;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImVec2;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.extension.imguizmo.flag.Mode;
import imgui.extension.imguizmo.flag.Operation;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiTableFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;
import processing.core.PGraphics;
import processing.opengl.PGraphicsOpenGL;

import java.util.ArrayList;
import java.util.List;

import static com.swrobotics.shufflelog.util.ProcessingUtils.setPMatrix;
import static processing.core.PConstants.P3D;

public final class FieldViewTool extends ViewportTool {
    private static final class SmoothMatrix {
        private final SmoothFloat[] elements;

        public SmoothMatrix(float smooth) {
            elements = new SmoothFloat[16];
            for (int i = 0; i < elements.length; i++)
                elements[i] = new SmoothFloat(smooth);
        }

        public void set(Matrix4f m) {
            for (int i = 0; i < 16; i++) {
                elements[i].set(m.get(i % 4, i / 4));
            }
        }

        public Matrix4f get() {
            Matrix4f m = new Matrix4f();
            for (int i = 0; i < 16; i++)
                m.set(i % 4, i / 4, elements[i].get());
            return m;
        }

        public void step() {
            for (SmoothFloat elem : elements)
                elem.step();
        }
    }

    public static final double WIDTH = 16.4592;
    public static final double HEIGHT = 8.2296;

    private static final float SMOOTH = 12;

    private static final int MODE_2D = 0;
    private static final int MODE_3D = 1;
    private static final String[] MODE_NAMES = {
            "2D (Orthographic)",
            "3D (Perspective)"
    };

    private final List<FieldLayer> layers;

    private final SmoothMatrix projection;
    private Matrix4f view;
    private GizmoTarget gizmoTarget;
    private int gizmoOp, gizmoMode;

    private final SmoothFloat cameraRotX, cameraRotY;
    private final SmoothFloat cameraTargetX, cameraTargetY, cameraTargetZ;
    private final SmoothFloat cameraDist;

    private final ImInt viewMode;

    private Vector2f cursorPos;
    private float orthoScale;
    private float orthoCameraRotYTarget;

    public FieldViewTool(ShuffleLog log) {
        // Be in 3d rendering mode
        super(log, "Field View", ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse, P3D);

        MessengerClient msg = log.getMessenger();
        layers = new ArrayList<>();
        layers.add(new MeterGridLayer());
        layers.add(new FieldVectorLayer2023());
        layers.add(new PathfindingLayer(msg, this));
        // layers.add(new TagTrackerLayer(this, msg));
        layers.add(new WaypointLayer(this, msg));

        projection = new SmoothMatrix(SMOOTH);

        cameraRotX = new SmoothFloat(SMOOTH, 0);
        cameraRotY = new SmoothFloat(SMOOTH, 0);
        cameraTargetX = new SmoothFloat(SMOOTH, (float) WIDTH/2);
        cameraTargetY = new SmoothFloat(SMOOTH, (float) HEIGHT/2);
        cameraTargetZ = new SmoothFloat(SMOOTH, 0);
        cameraDist = new SmoothFloat(SMOOTH, 8);

        gizmoOp = Operation.TRANSLATE;
        gizmoMode = Mode.WORLD;

        viewMode = new ImInt(MODE_2D);
    }

    // Can be null
    public Vector2f getCursorPos() {
        return cursorPos;
    }

    private float calcReqScale(float px, float field) {
        return (px - 10) / field;
    }

    @Override
    protected void drawViewportContent(PGraphics pGraphics) {
        projection.step();
        cameraRotX.step(); cameraRotY.step(); cameraDist.step();
        cameraTargetX.step(); cameraTargetY.step(); cameraTargetZ.step();

        // Custom projection and view matrices because Processing's defaults are pretty bad
        PGraphicsOpenGL g = (PGraphicsOpenGL) pGraphics;

        if (viewMode.get() == MODE_3D) {
            projection.set(new Matrix4f().perspective((float) Math.toRadians(80), g.width / (float) g.height, 0.01f, 1000f));
        } else {
            float normalScale = Math.min(calcReqScale(g.width, (float) WIDTH), calcReqScale(g.height, (float) HEIGHT));
            float rotatedScale = Math.min(calcReqScale(g.width, (float) HEIGHT), calcReqScale(g.height, (float) WIDTH));

            float scale;
            if (normalScale > rotatedScale) {
                cameraRotY.set(0);
                scale = normalScale;
            } else {
                cameraRotY.set((float) -Math.PI / 2);
                scale = rotatedScale;
            }
            cameraRotX.set(0);
            cameraDist.set(8);
            cameraTargetX.set((float) WIDTH / 2);
            cameraTargetY.set((float) HEIGHT / 2);
            cameraTargetZ.set(0);

            orthoCameraRotYTarget = cameraRotY.getTarget();
            orthoScale = scale;

            float halfW = (float) g.width / scale / 2;
            float halfH = (float) g.height / scale / 2;
            projection.set(new Matrix4f().ortho(-halfW, halfW, -halfH, halfH,
                    50, -50));
        }
        view = new Matrix4f()
                .translate(new Vector3f(cameraTargetX.get(), cameraTargetY.get(), cameraTargetZ.get()))
                .rotateZ(cameraRotY.get())
                .rotateX(cameraRotX.get())
                .translate(new Vector3f(0, 0, cameraDist.get()))
                .invert();

        setPMatrix(g.projection, projection.get());
        setPMatrix(g.modelview, view);
        g.modelviewInv.set(g.modelview);
        g.modelviewInv.invert();
        g.camera.set(g.modelview);
        g.cameraInv.set(g.modelviewInv);
        g.updateProjmodelview();

        g.background(0);
        g.noLights();

        float offset = 0;
        for (FieldLayer layer : layers) {
            g.pushMatrix();
            if (layer.shouldOffset()) {
                g.translate(0, 0, offset);
                offset += 0.01;
            }
            layer.draw(g);
            g.popMatrix();
        }
    }

    public void setGizmoTarget(GizmoTarget target) {
        gizmoTarget = target;
    }

    @Override
    protected void drawGuiContent() {
        if (ImGui.beginTable("layout", 2, ImGuiTableFlags.BordersInner | ImGuiTableFlags.Resizable)) {
            ImGui.tableNextColumn();

            ImGui.alignTextToFramePadding();
            ImGui.text("Tool:");
            ImGui.sameLine();
            if (ImGui.radioButton("Move", gizmoOp == Operation.TRANSLATE))
                gizmoOp = Operation.TRANSLATE;
            ImGui.sameLine();
            if (ImGui.radioButton("Rotate", gizmoOp == Operation.ROTATE))
                gizmoOp = Operation.ROTATE;

            ImGui.alignTextToFramePadding();
            ImGui.text("Space:");
            ImGui.sameLine();
            if (ImGui.radioButton("World", gizmoMode == Mode.WORLD))
                gizmoMode = Mode.WORLD;
            ImGui.sameLine();
            if (ImGui.radioButton("Local", gizmoMode == Mode.LOCAL))
                gizmoMode = Mode.LOCAL;

            ImGui.alignTextToFramePadding();
            ImGui.text("View mode:");
            ImGui.sameLine();
            ImGui.combo("##view_mode", viewMode, MODE_NAMES);
            ImGui.sameLine();
            if (ImGui.button("Reset View")) {
                cameraRotX.set(0);
                cameraRotY.set(viewMode.get() == MODE_2D ? orthoCameraRotYTarget : 0);
                cameraTargetX.set((float) WIDTH/2);
                cameraTargetY.set((float) HEIGHT/2);
                cameraTargetZ.set(0);
                cameraDist.set(8);
            }

            ImGui.text(String.valueOf(getCursorPos()));

            ImGui.separator();

            ImVec2 pos = ImGui.getWindowPos();
            ImVec2 cursor = ImGui.getCursorPos();
            ImVec2 size = ImGui.getContentRegionAvail();
            drawViewport(size.x, size.y, false);
            boolean hovered = ImGui.isItemHovered();

            float x = pos.x + cursor.x;
            float y = pos.y + cursor.y;

            boolean gizmoConsumesMouse = false;
            if (gizmoTarget != null) {
                float[] transArr = gizmoTarget.getTransform().getColumnMajor();
                ImGuizmo.setRect(x, y, size.x, size.y);
                ImGuizmo.setAllowAxisFlip(true);
                ImGuizmo.manipulate(view.getColumnMajor(), projection.get().getColumnMajor(), transArr, gizmoOp, gizmoMode);
                if (ImGuizmo.isUsing()) {
                    gizmoTarget.setTransform(Matrix4f.fromColumnMajor(transArr));
                }
                gizmoConsumesMouse = ImGuizmo.isOver();
            }

            ImVec2 mouse = ImGui.getIO().getMousePos();
            float mouseX = mouse.x - x;
            float mouseY = mouse.y - y;

            if (mouseX >= 0 && mouseY >= 0 && mouseX < size.x && mouseY < size.y) {
                Ray3f mouseRay;
                if (viewMode.get() == MODE_2D) {
                    float rayX = (mouseX - size.x / 2) / orthoScale;
                    float rayY = -(mouseY - size.y / 2) / orthoScale;
                    mouseRay = new Ray3f(new Vector3f(rayX, rayY, 0), new Vector3f(0, 0, 1));
                } else {
                    float normX = (2.0f * mouseX) / size.x - 1.0f;
                    float normY = 1.0f - (2.0f * mouseY) / size.y;
                    Vector4f clipSpace = new Vector4f(normX, normY, -1, 1);
                    Vector4f eyeSpace = new Matrix4f(projection.get()).invert().mul(clipSpace);
                    mouseRay = new Ray3f(new Vector3f(0, 0, 0), new Vector3f(eyeSpace.x, eyeSpace.y, -1).normalize());
                }

                Matrix4f invView = new Matrix4f(view).invert();
                Vector3f orig = invView.transformPosition(mouseRay.getOrigin());
                Vector3f dir = invView.transformDirection(mouseRay.getDirection());

                float zDelta = -orig.z;
                float dirScale = zDelta / dir.z;

                cursorPos = new Vector2f(
                        orig.x + dir.x * dirScale,
                        orig.y + dir.y * dirScale
                );
            } else {
                cursorPos = null;
            }

            if (hovered && !gizmoConsumesMouse) {
                ImGuiIO io = ImGui.getIO();

                Matrix4f viewRotInv = new Matrix4f(view).invert();
                // Remove translation
                viewRotInv.m03 = 0;
                viewRotInv.m13 = 0;
                viewRotInv.m23 = 0;

                if (io.getMouseDown(ImGuiMouseButton.Right)) {
                    // Pan

                    float deltaX = io.getMouseDeltaX();
                    float deltaY = io.getMouseDeltaY();

                    Vector3f up = viewRotInv.transformPosition(new Vector3f(0, 1, 0)).normalize();
                    Vector3f right = viewRotInv.transformPosition(new Vector3f(1, 0, 0)).normalize();

                    float dist = cameraDist.get();
                    float scaleUp = deltaY * 0.002f * dist;
                    float scaleRight = deltaX * -0.002f * dist;

                    cameraTargetX.set(cameraTargetX.getTarget() + up.x * scaleUp + right.x * scaleRight);
                    cameraTargetY.set(cameraTargetY.getTarget() + up.y * scaleUp + right.y * scaleRight);
                    cameraTargetZ.set(cameraTargetZ.getTarget() + up.z * scaleUp + right.z * scaleRight);
                }

                if (viewMode.get() == MODE_3D) {
                    if (io.getMouseDown(ImGuiMouseButton.Left)) {
                        // Turn

                        float deltaX = io.getMouseDeltaX();
                        float deltaY = io.getMouseDeltaY();

                        cameraRotX.set(cameraRotX.getTarget() - deltaY * 0.007f);
                        cameraRotY.set(cameraRotY.getTarget() - deltaX * 0.007f);
                    }

                    float scroll = io.getMouseWheel();
                    float scale = 1 + scroll * -0.05f;
                    cameraDist.set(cameraDist.getTarget() * scale);
                }
            }

            ImGui.tableNextColumn();
            ImGui.beginChild("scroller");
            for (FieldLayer layer : layers) {
                if (ImGui.collapsingHeader(layer.getName())) {
                    ImGui.pushID(layer.getName());
                    ImGui.indent();
                    layer.showGui();
                    ImGui.unindent();
                    ImGui.popID();
                }
            }
            ImGui.endChild();
            ImGui.endTable();
        }
    }

    @Override
    public void process() {
        for (FieldLayer layer : layers)
            layer.processAlways();

        super.process();
    }
}

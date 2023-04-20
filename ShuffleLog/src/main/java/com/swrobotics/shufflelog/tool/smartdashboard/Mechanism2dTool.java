package com.swrobotics.shufflelog.tool.smartdashboard;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;

public final class Mechanism2dTool implements SmartDashboardTool {
    public static final class Viewport {
        public float scale;
        public float originX, originY;

        public ImVec2 toScreenSpace(double x, double y) {
            return new ImVec2(originX + (float) x * scale, originY - (float) y * scale);
        }
    }

    private final String name;
    private final NetworkTable table;
    private final NetworkTableEntry dimensionsEntry;
    private final NetworkTableEntry colorEntry;

    public Mechanism2dTool(String name, NetworkTable table) {
        this.name = name;
        this.table = table;
        dimensionsEntry = table.getEntry("dims");
        colorEntry = table.getEntry("backgroundColor");
    }

    @Override
    public String getName() {
        return name;
    }

    private int hexToColor(String hex) {
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);
        return ImGui.getColorU32(r / 255f, g / 255f, b/255f, 1);
    }

    private void drawLigament(Viewport vp, ImDrawList draw, NetworkTable ligament, double x, double y, double angle) {
        angle += ligament.getEntry("angle").getDouble(0);
        int color = hexToColor(ligament.getEntry("color").getString("#FFFFFF"));
        double length = ligament.getEntry("length").getDouble(1);
        double weight = ligament.getEntry("weight").getDouble(4);

        double radians = Math.toRadians(angle);
        double endX = x + length * Math.cos(radians);
        double endY = y + length * Math.sin(radians);

        ImVec2 start = vp.toScreenSpace(x, y);
        ImVec2 end = vp.toScreenSpace(endX, endY);
        draw.addLine(start.x, start.y, end.x, end.y, color, (float) weight);

        for (String childName : ligament.getSubTables()) {
            NetworkTable child = ligament.getSubTable(childName);

            drawLigament(vp, draw, child, endX, endY, angle);
        }
    }

    @Override
    public void process() {
        if (ImGui.begin(SmartDashboard.WINDOW_PREFIX + name)) {
            double[] dims = dimensionsEntry.getDoubleArray(new double[] {1, 1});

            ImVec2 size = ImGui.getContentRegionAvail();
            double scaleX = size.x / dims[0];
            double scaleY = size.y / dims[1];

            ImVec2 pos = ImGui.getWindowPos();
            ImVec2 min = ImGui.getWindowContentRegionMin();
            ImVec2 max = ImGui.getWindowContentRegionMax();
            float centerX = (min.x + max.x) / 2;
            float centerY = (min.y + max.y) / 2;

            Viewport vp = new Viewport();
            vp.scale = (float) Math.min(scaleX, scaleY);
            vp.originX = (float) (centerX - dims[0] / 2 * vp.scale) + pos.x;
            vp.originY = (float) (centerY + dims[1] / 2 * vp.scale) + pos.y;

            String colorHex = colorEntry.getString("#000000");
            int bgColor = hexToColor(colorHex);

            ImDrawList draw = ImGui.getWindowDrawList();

            ImVec2 topRight = vp.toScreenSpace(dims[0], dims[1]);
            draw.addRectFilled(vp.originX, vp.originY, topRight.x, topRight.y, bgColor);

            for (String rootName : table.getSubTables()) {
                NetworkTable root = table.getSubTable(rootName);
                float x = root.getEntry("x").getFloat(0);
                float y = root.getEntry("y").getFloat(0);

                for (String ligamentName : root.getSubTables()) {
                    NetworkTable ligament = root.getSubTable(ligamentName);

                    drawLigament(vp, draw, ligament, x, y, 0);
                }
            }
        }
        ImGui.end();
    }
}

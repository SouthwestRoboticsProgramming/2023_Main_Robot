package com.swrobotics.shufflelog.tool.blockauto.part;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.shufflelog.tool.blockauto.BlockAutoTool;
import com.swrobotics.shufflelog.tool.field.waypoint.Waypoint;
import com.swrobotics.shufflelog.tool.field.waypoint.WaypointLayer;
import imgui.ImGui;
import imgui.type.ImDouble;
import imgui.type.ImInt;

import java.util.List;

public final class FieldPointPart extends ParamPart {
    public static FieldPointPart read(MessageReader reader) {
        double x = reader.readDouble();
        double y = reader.readDouble();
        return new FieldPointPart(WaypointLayer.INSTANCE, x, y);
    }

    public interface Point {
        void write(MessageBuilder builder);
    }

    public static final class WaypointPoint implements Point {
        private String waypointName;

        public WaypointPoint(String waypointName) {
            this.waypointName = waypointName;
        }

        @Override
        public void write(MessageBuilder builder) {
            builder.addBoolean(true);
            builder.addString(waypointName);
        }
    }

    public static final class SpecificPoint implements Point {
        private double x;
        private double y;

        public SpecificPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void write(MessageBuilder builder) {
            builder.addBoolean(false);
            builder.addDouble(x);
            builder.addDouble(y);
        }
    }

    private final WaypointLayer waypointLayer;
    private final double defX;
    private final double defY;

    public FieldPointPart(WaypointLayer waypointLayer, double defX, double defY) {
        this.waypointLayer = waypointLayer;
        this.defX = defX;
        this.defY = defY;
    }

    @Override
    public Object getDefault() {
        return new SpecificPoint(defX, defY);
    }

    private static final ImDouble tempDouble = new ImDouble();
    private static final ImInt tempInt = new ImInt();

    @Override
    public boolean edit(Object[] val) {
        List<Waypoint> waypoints = waypointLayer == null ? null : waypointLayer.getWaypoints();

        if (val[0] instanceof SpecificPoint) {
            ImGui.beginDisabled(waypoints == null || waypoints.isEmpty());
            if (ImGui.button("WP")) {
                if (waypoints != null)
                    val[0] = new WaypointPoint(waypoints.get(0).getName());
                ImGui.endDisabled();
                return true;
            }
            ImGui.endDisabled();

            SpecificPoint p = (SpecificPoint) val[0];

            ImGui.sameLine();
            tempDouble.set(p.x);
            ImGui.setNextItemWidth(50);
            boolean changed = ImGui.inputDouble("##x", tempDouble);
            p.x = tempDouble.get();

            ImGui.sameLine();
            tempDouble.set(p.y);
            ImGui.setNextItemWidth(50);
            changed |= ImGui.inputDouble("##y", tempDouble);
            p.y = tempDouble.get();

            return changed;
        } else if (val[0] instanceof WaypointPoint) {
            WaypointPoint wp = (WaypointPoint) val[0];

            // Placeholder for if we don't have waypoints yet
            if (waypoints == null) {
                // Allow switching to specific point even if no waypoint
                if (ImGui.button("PT")) {
                    val[0] = new SpecificPoint(defX, defY);
                    return true;
                }

                ImGui.sameLine();
                ImGui.textDisabled("Fetching...");
                return false;
            }

            Waypoint selected = null;
            int selectedIdx = -1;
            String[] options = new String[waypoints.size()];
            for (int i = 0; i < options.length; i++) {
                Waypoint w = waypoints.get(i);
                options[i] = w.getName();
                if (wp.waypointName.equals(w.getName())) {
                    selected = w;
                    selectedIdx = i;
                }
            }

            if (selected == null) {
                val[0] = new SpecificPoint(defX, defY);
                return true;
            }

            if (ImGui.button("PT")) {
                val[0] = new SpecificPoint(selected.getX().get(), selected.getY().get());
                return true;
            }

            ImGui.sameLine();

            tempInt.set(selectedIdx);
            ImGui.setNextItemWidth(50);
            boolean changed = ImGui.combo("##wp_select", tempInt, options);
            wp.waypointName = options[tempInt.get()];

            return changed;
        }

        throw new IllegalStateException();
    }

    @Override
    public Object readInst(MessageReader reader, BlockAutoTool tool) {
        boolean isWaypoint = reader.readBoolean();
        if (isWaypoint) {
            return new WaypointPoint(reader.readString());
        } else {
            double x = reader.readDouble();
            double y = reader.readDouble();
            return new SpecificPoint(x, y);
        }
    }

    @Override
    public void writeInst(MessageBuilder builder, Object value) {
        ((Point) value).write(builder);
    }

    @Override
    public boolean isFrame() {
        return true;
    }
}

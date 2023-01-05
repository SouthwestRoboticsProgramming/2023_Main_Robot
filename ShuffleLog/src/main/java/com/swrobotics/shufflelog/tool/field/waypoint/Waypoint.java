package com.swrobotics.shufflelog.tool.field.waypoint;

import com.swrobotics.messenger.client.MessengerClient;
import imgui.type.ImDouble;
import imgui.type.ImString;

public final class Waypoint {
    private ImDouble x, y;
    private String name;

    public Waypoint(double x, double y, String name) {
        this.x = new ImDouble(x);
        this.y = new ImDouble(y);
        this.name = name;
    }

    public void add(MessengerClient msg) {
        msg.prepare(WaypointLayer.MSG_ADD_WAYPOINT)
                .addString(name)
                .addDouble(x.get())
                .addDouble(y.get())
                .send();
    }

    public void delete(MessengerClient msg) {
        msg.prepare(WaypointLayer.MSG_REMOVE_WAYPOINT)
                .addString(name)
                .send();
    }

    public ImDouble getX() {
        return x;
    }

    public ImDouble getY() {
        return y;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

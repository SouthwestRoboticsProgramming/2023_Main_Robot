package com.swrobotics.robot.blockauto.part;

import com.google.gson.*;
import com.swrobotics.mathlib.Vec2d;
import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.robot.blockauto.WaypointStorage;

public final class FieldPointPart extends ParamPart {
    public interface Point {
        Vec2d getPosition();

        void write(MessageBuilder builder);
    }

    public static final class WaypointPoint implements Point {
        private final String waypointName;

        public WaypointPoint(String waypointName) {
            this.waypointName = waypointName;
        }

        @Override
        public Vec2d getPosition() {
            return WaypointStorage.getWaypointLocation(waypointName);
        }

        @Override
        public void write(MessageBuilder builder) {
            builder.addBoolean(true);
            builder.addString(waypointName);
        }
    }

    public static final class SpecificPoint implements Point {
        private final double x;
        private final double y;

        public SpecificPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public Vec2d getPosition() {
            return new Vec2d(x, y);
        }

        @Override
        public void write(MessageBuilder builder) {
            builder.addBoolean(false);
            builder.addDouble(x);
            builder.addDouble(y);
        }
    }

    private final double defX;
    private final double defY;

    public FieldPointPart(String name, double defX, double defY) {
        super(name);
        this.defX = defX;
        this.defY = defY;
    }

    @Override
    public Object readInst(MessageReader reader) {
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
    public void writeInst(MessageBuilder builder, Object val) {
        ((Point) val).write(builder);
    }

    @Override
    public void writeToMessenger(MessageBuilder builder) {
        builder.addByte(PartTypes.FIELD_POINT.getId());
        builder.addDouble(defX);
        builder.addDouble(defY);
    }

    @Override
    public Object deserializeInst(JsonElement elem, JsonDeserializationContext ctx) {
        if (elem.isJsonArray()) {
            JsonArray arr = elem.getAsJsonArray();
            return new SpecificPoint(arr.get(0).getAsDouble(), arr.get(1).getAsDouble());
        } else {
            return new WaypointPoint(elem.getAsString());
        }
    }

    @Override
    public JsonElement serializeInst(Object val, JsonSerializationContext ctx) {
        if (val instanceof SpecificPoint) {
            SpecificPoint p = (SpecificPoint) val;

            JsonArray arr = new JsonArray();
            arr.add(p.x);
            arr.add(p.y);

            return arr;
        } else {
            return new JsonPrimitive(((WaypointPoint) val).waypointName);
        }
    }
}

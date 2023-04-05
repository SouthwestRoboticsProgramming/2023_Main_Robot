package com.swrobotics.pathfinding.core.grid;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.pathfinding.core.geom.RobotShape;
import com.swrobotics.pathfinding.core.geom.Shape;
import com.swrobotics.pathfinding.field.Field;
import com.swrobotics.pathfinding.task.PathfinderTask;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public final class ShapeGrid extends BitfieldGrid {
    private final RobotShape robot;
    private final Field field;
    private final Set<Shape> shapes;
    private boolean needsRegenerateBitfield;

    public ShapeGrid(int width, int height, Field field, RobotShape robot) {
        super(width, height);
        this.field = field;
        this.robot = robot;
        shapes = new HashSet<>();
        needsRegenerateBitfield = false;
    }

    public void addShape(Shape shape) {
        shapes.add(shape);
        shape.setParent(this);
        needsRegenerateBitfield = true;
        invalidateLineOfSightCache();
    }

    public void removeShape(Shape shape) {
        shapes.remove(shape);
        shape.setParent(null);
        needsRegenerateBitfield = true;
        invalidateLineOfSightCache();
    }

    private void regenerateBitfield() {
        needsRegenerateBitfield = false;

        for (int y = 0; y < getCellHeight(); y++) {
            for (int x = 0; x < getCellWidth(); x++) {
                double robotX = field.getCellCenterX(x);
                double robotY = field.getCellCenterY(y);

                boolean canPass = true;
                for (Shape shape : shapes) {
                    if (shape.collidesWith(robot, robotX, robotY)) {
                        canPass = false;
                    }
                }

                set(x, y, canPass);
            }
        }
    }

    @Override
    public boolean canCellPass(int x, int y) {
        if (needsRegenerateBitfield) regenerateBitfield();

        return super.canCellPass(x, y);
    }

    @Override
    public void writeToMessenger(MessageBuilder builder) {
        builder.addByte(GridType.SHAPE.getTypeId());
        builder.addInt(shapes.size());
        for (Shape shape : shapes) {
            shape.writeToMessenger(builder);
        }
    }

    @Override
    public void register(PathfinderTask task) {
        super.register(task);
        for (Shape shape : shapes) {
            shape.register(task);
        }
    }

    public static final class Serializer
            implements JsonSerializer<ShapeGrid>, JsonDeserializer<ShapeGrid> {
        @Override
        public ShapeGrid deserialize(
                JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            JsonArray shapes = obj.getAsJsonArray("shapes");

            Grid.DeserializationContext ctx = Grid.DESERIALIZATION_CTX.get();
            ShapeGrid grid =
                    new ShapeGrid(ctx.getWidth(), ctx.getHeight(), ctx.getField(), ctx.getRobot());
            for (JsonElement elem : shapes) {
                grid.addShape(context.deserialize(elem, Shape.class));
            }

            return grid;
        }

        @Override
        public JsonElement serialize(
                ShapeGrid src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", GridType.SHAPE.toString());
            JsonArray shapes = new JsonArray();
            for (Shape shape : src.shapes) {
                shapes.add(context.serialize(shape));
            }
            obj.add("shapes", shapes);
            return obj;
        }
    }
}

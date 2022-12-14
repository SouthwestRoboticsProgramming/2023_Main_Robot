package com.swrobotics.pathfinding.grid;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.pathfinding.Field;
import com.swrobotics.pathfinding.Point;
import com.swrobotics.pathfinding.geom.Circle;
import com.swrobotics.pathfinding.geom.Rectangle;
import com.swrobotics.pathfinding.geom.RobotShape;
import com.swrobotics.pathfinding.geom.Shape;
import com.swrobotics.pathfinding.task.PathfinderTask;

import java.lang.reflect.Type;
import java.util.UUID;

public abstract class Grid {
    public static final ThreadLocal<DeserializationContext> DESERIALIZATION_CTX = new ThreadLocal<>();
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Grid.class, new Serializer())
            .registerTypeAdapter(BitfieldGrid.class, new BitfieldGrid.Serializer())
            .registerTypeAdapter(GridUnion.class, new GridUnion.Serializer())
            .registerTypeAdapter(ShapeGrid.class, new ShapeGrid.Serializer())
            .registerTypeAdapter(Shape.class, new Shape.Serializer())
            .registerTypeAdapter(RobotShape.class, new Shape.Serializer())
            .registerTypeAdapter(Circle.class, new Circle.Serializer())
            .registerTypeAdapter(Rectangle.class, new Rectangle.Serializer())
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    public static Grid deserialize(JsonElement elem, int width, int height, Field field, RobotShape robot) {
        DESERIALIZATION_CTX.set(new DeserializationContext(
                width, height, field, robot
        ));
        return GSON.fromJson(elem, Grid.class);
    }

    protected final int width;
    protected final int height;
    private GridUnion parent;
    private UUID id;

    // Sizes are in number of cells, points is one larger
    public Grid(int width, int height) {
        id = UUID.randomUUID();
        this.width = width;
        this.height = height;
    }

    public String serializeToString() {
        return GSON.toJson(this);
    }

    public JsonElement serializeToTree() {
        return GSON.toJsonTree(this);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public GridUnion getParent() {
        return parent;
    }

    public void setParent(GridUnion parent) {
        this.parent = parent;
    }

    public abstract boolean canCellPass(int x, int y);
    public abstract void writeToMessenger(MessageBuilder builder);
    public void register(PathfinderTask task) {
        task.registerGrid(this);
    }

    public void addToMessenger(MessageBuilder builder) {
        builder.addLong(id.getMostSignificantBits());
        builder.addLong(id.getLeastSignificantBits());
        writeToMessenger(builder);
    }

    public boolean canCellPass(Point p) {
        return canCellPass(p.x, p.y);
    }

    public boolean canEdgePass(Point p1, Point p2) {
        int dx = p2.x - p1.x;
        int dy = p2.y - p1.y;
        int ox = (dx - 2) / 2;
        int oy = (dy - 2) / 2;

        if (dx != 0 && dy != 0) {
            return canCellPass(p1.x + ox, p1.y + oy);
        } else {
            if (dx > 0)
                return canCellPass(p1) && canCellPass(p1.x, p1.y - 1);
            if (dx < 0)
                return canCellPass(p1.x - 1, p1.y) && canCellPass(p1.x - 1, p1.y - 1);
            if (dy > 0)
                return canCellPass(p1) && canCellPass(p1.x - 1, p1.y);
            if (dy < 0)
                return canCellPass(p1.x, p1.y - 1) && canCellPass(p1.x - 1, p1.y - 1);

            throw new IllegalStateException();
        }
    }

    public boolean lineOfSight(Point s, Point sp) {
        int x0 = s.x;
        int y0 = s.y;
        int x1 = sp.x;
        int y1 = sp.y;
        int dy = y1 - y0;
        int dx = x1 - x0;
        int f = 0;

        int sy;
        int sx;

        if (dy < 0) {
            dy = -dy;
            sy = -1;
        } else {
            sy = 1;
        }

        if (dx < 0) {
            dx = -dx;
            sx = -1;
        } else {
            sx = 1;
        }

        if (dx >= dy) {
            while (x0 != x1) {
                f = f + dy;
                if (f >= dx) {
                    if (!canCellPass(x0 + ((sx - 1)/2), y0 + ((sy - 1)/2))) {
                        return false;
                    }
                    y0 = y0 + sy;
                    f = f - dx;
                }
                if (f != 0 && !canCellPass(x0 + ((sx - 1)/2), y0 + ((sy - 1)/2))) {
                    return false;
                }
                if (dy == 0 && !canCellPass(x0 + ((sx - 1)/2), y0) && !canCellPass(x0 + ((sx - 1)/2), y0 - 1)) {
                    return false;
                }
                x0 = x0 + sx;
            }
        } else {
            while (y0 != y1) {
                f = f + dx;
                if (f >= dy) {
                    if (!canCellPass(x0 + ((sx - 1)/2), y0 + ((sy - 1)/2))) {
                        return false;
                    }
                    x0 = x0 + sx;
                    f = f - dy;
                }
                if (f != 0 && !canCellPass(x0 + ((sx - 1)/2), y0 + ((sy - 1)/2))) {
                    return false;
                }
                if (dx == 0 && !canCellPass(x0, y0 + ((sy - 1)/2)) && !canCellPass(x0 - 1, y0 + ((sy - 1)/2))) {
                    return false;
                }
                y0 = y0 + sy;
            }
        }

        return true;
    }

    public int getCellWidth() {
        return width;
    }

    public int getCellHeight() {
        return height;
    }

    public int getPointWidth() {
        return width + 1;
    }

    public int getPointHeight() {
        return height + 1;
    }

    public static final class DeserializationContext {
        private final int width;
        private final int height;
        private final Field field;
        private final RobotShape robot;

        public DeserializationContext(int width, int height, Field field, RobotShape robot) {
            this.width = width;
            this.height = height;
            this.field = field;
            this.robot = robot;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public Field getField() {
            return field;
        }

        public RobotShape getRobot() {
            return robot;
        }
    }

    public static final class Serializer implements JsonDeserializer<Grid> {
        @Override
        public Grid deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            GridType type = GridType.valueOf(obj.get("type").getAsString());
            return context.deserialize(obj, type.getType());
        }
    }
}

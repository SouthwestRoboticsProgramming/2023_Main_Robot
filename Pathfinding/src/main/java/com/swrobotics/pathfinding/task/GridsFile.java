package com.swrobotics.pathfinding.task;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.swrobotics.pathfinding.core.geom.Circle;
import com.swrobotics.pathfinding.core.geom.Rectangle;
import com.swrobotics.pathfinding.core.geom.RobotShape;
import com.swrobotics.pathfinding.core.geom.Shape;
import com.swrobotics.pathfinding.core.grid.BitfieldGrid;
import com.swrobotics.pathfinding.core.grid.Grid;
import com.swrobotics.pathfinding.core.grid.GridUnion;
import com.swrobotics.pathfinding.core.grid.ShapeGrid;
import com.swrobotics.pathfinding.field.Field;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class GridsFile {
    private static final ThreadLocal<DeserializationContext> DESERIALIZATION_CTX =
            new ThreadLocal<>();
    private static final Gson GSON =
            new GsonBuilder()
                    .registerTypeAdapter(GridsFile.class, new Serializer())
                    .registerTypeAdapter(Grid.class, new Grid.Serializer())
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

    private static final class DeserializationContext {
        final Field field;
        final int width;
        final int height;

        public DeserializationContext(Field field, int width, int height) {
            this.field = field;
            this.width = width;
            this.height = height;
        }
    }

    private RobotShape robot;
    private List<Grid> grids;

    private GridsFile() {}

    public GridsFile(RobotShape robot, List<Grid> grids) {
        this.robot = robot;
        this.grids = grids;
    }

    public static GridsFile load(File file, Field field) {
        try {
            DESERIALIZATION_CTX.set(
                    new DeserializationContext(field, field.getCellsX(), field.getCellsY()));
            return GSON.fromJson(new FileReader(file), GridsFile.class);
        } catch (FileNotFoundException e) {
            GridsFile def = new GridsFile();
            def.robot = new Circle(0, 0, 0.5, false);
            def.grids = new ArrayList<>();

            System.err.println("Grids file not found, saving default");
            def.save(file);

            return def;
        }
    }

    public void save(File file) {
        try {
            FileWriter writer = new FileWriter(file);
            GSON.toJson(this, writer);
            writer.close();
        } catch (Exception e2) {
            System.err.println("Failed to save grids file");
            e2.printStackTrace();
        }
    }

    public RobotShape getRobot() {
        return robot;
    }

    public void setRobot(RobotShape robot) {
        this.robot = robot;
    }

    public List<Grid> getGrids() {
        return grids;
    }

    public void setGrids(List<Grid> grids) {
        this.grids = grids;
    }

    private static final class Serializer
            implements JsonSerializer<GridsFile>, JsonDeserializer<GridsFile> {
        @Override
        public GridsFile deserialize(
                JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();

            RobotShape robot = context.deserialize(obj.get("robot"), Shape.class);

            List<Grid> grids = new ArrayList<>();
            DeserializationContext ctx = DESERIALIZATION_CTX.get();
            for (JsonElement elem : obj.getAsJsonArray("grids")) {
                Grid.DESERIALIZATION_CTX.set(
                        new Grid.DeserializationContext(ctx.width, ctx.height, ctx.field, robot));
                grids.add(context.deserialize(elem, Grid.class));
            }

            return new GridsFile(robot, grids);
        }

        @Override
        public JsonElement serialize(
                GridsFile src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.add("robot", context.serialize(src.robot));
            obj.add("grids", context.serialize(src.grids));
            return obj;
        }
    }
}

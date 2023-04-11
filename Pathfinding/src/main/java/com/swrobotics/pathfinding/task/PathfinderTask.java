package com.swrobotics.pathfinding.task;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.pathfinding.core.finder.Pathfinder;
import com.swrobotics.pathfinding.core.geom.Circle;
import com.swrobotics.pathfinding.core.geom.Rectangle;
import com.swrobotics.pathfinding.core.geom.RobotShape;
import com.swrobotics.pathfinding.core.geom.Shape;
import com.swrobotics.pathfinding.core.geom.ShapeType;
import com.swrobotics.pathfinding.core.grid.BitfieldGrid;
import com.swrobotics.pathfinding.core.grid.Grid;
import com.swrobotics.pathfinding.core.grid.GridType;
import com.swrobotics.pathfinding.core.grid.GridUnion;
import com.swrobotics.pathfinding.core.grid.Point;
import com.swrobotics.pathfinding.core.grid.ShapeGrid;
import com.swrobotics.pathfinding.field.Field;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PathfinderTask {
    private static final File CONFIG_FILE = new File("config.json");
    private static final File GRIDS_FILE = new File("grids.json");

    // Main API
    private static final String MSG_SET_POS = "Pathfinder:SetPos";
    private static final String MSG_SET_GOAL = "Pathfinder:SetGoal";
    private static final String MSG_PATH = "Pathfinder:Path";

    // ShuffleLog API
    private static final String MSG_GET_FIELD_INFO = "Pathfinder:GetFieldInfo";
    private static final String MSG_GET_GRIDS = "Pathfinder:GetGrids";
    private static final String MSG_GET_CELL_DATA = "Pathfinder:GetCellData";
    private static final String MSG_GET_ROBOT_SHAPE = "Pathfinder:GetRobotShape";
    private static final String MSG_ADD_GRID = "Pathfinder:AddGrid";
    private static final String MSG_REMOVE_GRID = "Pathfinder:RemoveGrid";
    private static final String MSG_ADD_SHAPE = "Pathfinder:AddShape";
    private static final String MSG_ALTER_SHAPE = "Pathfinder:AlterShape";
    private static final String MSG_REMOVE_SHAPE = "Pathfinder:RemoveShape";

    private static final String MSG_FIELD_INFO = "Pathfinder:FieldInfo";
    private static final String MSG_GRIDS = "Pathfinder:Grids";
    private static final String MSG_CELL_DATA = "Pathfinder:CellData";
    private static final String MSG_ROBOT_SHAPE = "Pathfinder:RobotShape";

    private final MessengerClient msg;
    private final Field field;
    private final RobotShape robot;

    private final GridUnion grids;
    private final Pathfinder<Point> pathfinder;

    private final Map<UUID, Grid> idToGrid;
    private final Map<UUID, Shape> idToShape;

    private boolean needsRecalcPath;

    public PathfinderTask() {
        PathfinderConfigFile config = PathfinderConfigFile.load(CONFIG_FILE);
        msg = config.getMessenger().createClient();
        field = config.getField().createField();

        idToGrid = new HashMap<>();
        idToShape = new HashMap<>();

        grids = new GridUnion(field.getCellsX(), field.getCellsY());
        GridsFile file = GridsFile.load(GRIDS_FILE, field);
        robot = file.getRobot();
        for (Grid grid : file.getGrids()) {
            grids.addGrid(grid);
        }
        saveGrids();

        grids.register(this);
        pathfinder = config.getFinderType().create(grids);

        msg.addHandler(MSG_SET_POS, this::onSetPos);
        msg.addHandler(MSG_SET_GOAL, this::onSetGoal);

        msg.addHandler(MSG_ADD_GRID, this::onAddGrid);
        msg.addHandler(MSG_REMOVE_GRID, this::onRemoveGrid);
        msg.addHandler(MSG_ADD_SHAPE, this::onAddShape);
        msg.addHandler(MSG_ALTER_SHAPE, this::onAlterShape);
        msg.addHandler(MSG_REMOVE_SHAPE, this::onRemoveShape);
        msg.addHandler(MSG_GET_FIELD_INFO, this::onGetFieldInfo);
        msg.addHandler(MSG_GET_GRIDS, this::onGetGrids);
        msg.addHandler(MSG_GET_CELL_DATA, this::onGetCellData);
        msg.addHandler(MSG_GET_ROBOT_SHAPE, this::onGetRobotShape);

        pathfinder.setStart(new Point(0, 0));
        pathfinder.setGoal(new Point(0, 0));

        needsRecalcPath = true;

        System.out.println("Pathfinder is running");
    }

    private void saveGrids() {
        System.out.println("Saving grids");
        GridsFile file = new GridsFile(robot, new ArrayList<>(grids.getChildren()));
        file.save(GRIDS_FILE);
    }

    private void onSetPos(String type, MessageReader reader) {
        double x = reader.readDouble();
        double y = reader.readDouble();
        Point p = field.getNearestPoint(x, y);
        pathfinder.setStart(p);
        needsRecalcPath = true;
    }

    private void onSetGoal(String type, MessageReader reader) {
        double x = reader.readDouble();
        double y = reader.readDouble();
        Point p = field.getNearestPoint(x, y);
        pathfinder.setGoal(p);
        needsRecalcPath = true;
    }

    private void removeUnion(GridUnion union) {
        for (Grid grid : union.getChildren()) {
            idToGrid.remove(grid.getId());
            if (grid instanceof GridUnion) {
                removeUnion((GridUnion) grid);
            }
        }
    }

    private void removeGrid(UUID gridId) {
        Grid existingGrid = idToGrid.remove(gridId);
        if (existingGrid == null) return;

        existingGrid.getParent().removeGrid(existingGrid);

        if (existingGrid instanceof GridUnion) {
            removeUnion((GridUnion) existingGrid);
        }
    }

    private void removeShape(UUID shapeId) {
        Shape existingShape = idToShape.remove(shapeId);
        if (existingShape != null) existingShape.getParent().removeShape(existingShape);
    }

    private void onAddGrid(String type, MessageReader reader) {
        long parentIdMsb = reader.readLong();
        long parentIdLsb = reader.readLong();
        UUID parentId = new UUID(parentIdMsb, parentIdLsb);
        GridUnion parent = (GridUnion) idToGrid.get(parentId);
        if (parent == null) return;

        // Sender specifies ID so it can identify the grid later. UUIDs are
        // practically unique so both sides can just generate random IDs with
        // almost no risk of collision
        long gridIdMsb = reader.readLong();
        long gridIdLsb = reader.readLong();
        UUID gridId = new UUID(gridIdMsb, gridIdLsb);
        removeGrid(gridId);

        byte typeId = reader.readByte();
        Grid grid;
        int w = grids.getCellWidth(), h = grids.getCellHeight();
        if (typeId == GridType.UNION.getTypeId()) {
            grid = new GridUnion(w, h);
        } else if (typeId == GridType.BITFIELD.getTypeId()) {
            grid = new BitfieldGrid(w, h);
        } else if (typeId == GridType.SHAPE.getTypeId()) {
            grid = new ShapeGrid(w, h, field, robot);
        } else {
            return;
        }
        grid.setId(gridId);
        parent.addGrid(grid);
        grid.register(this);
        needsRecalcPath = true;
        saveGrids();
    }

    private void onRemoveGrid(String type, MessageReader reader) {
        long gridIdMsb = reader.readLong();
        long gridIdLsb = reader.readLong();
        UUID gridId = new UUID(gridIdMsb, gridIdLsb);
        removeGrid(gridId);
        needsRecalcPath = true;
        saveGrids();
    }

    private void alterShape(ShapeGrid grid, UUID shapeId, MessageReader reader) {
        boolean inverted = reader.readBoolean();
        byte typeId = reader.readByte();
        Shape shape;
        if (typeId == ShapeType.CIRCLE.getTypeId()) {
            double x = reader.readDouble();
            double y = reader.readDouble();
            double radius = reader.readDouble();
            shape = new Circle(x, y, radius, inverted);
        } else if (typeId == ShapeType.RECTANGLE.getTypeId()) {
            double x = reader.readDouble();
            double y = reader.readDouble();
            double width = reader.readDouble();
            double height = reader.readDouble();
            double rotation = reader.readDouble();
            shape = new Rectangle(x, y, width, height, rotation, inverted);
        } else {
            return;
        }

        shape.setId(shapeId);
        grid.addShape(shape);
        shape.register(this);
        needsRecalcPath = true;

        System.out.println("Shape changed: " + shape);
    }

    private void onAlterShape(String type, MessageReader reader) {
        long shapeIdMsb = reader.readLong();
        long shapeIdLsb = reader.readLong();
        UUID shapeId = new UUID(shapeIdMsb, shapeIdLsb);
        ShapeGrid parent = idToShape.get(shapeId).getParent();
        removeShape(shapeId);
        alterShape(parent, shapeId, reader);
        saveGrids();
    }

    private void onAddShape(String type, MessageReader reader) {
        long parentIdMsb = reader.readLong();
        long parentIdLsb = reader.readLong();
        UUID parentId = new UUID(parentIdMsb, parentIdLsb);
        Grid plainGrid = idToGrid.get(parentId);
        if (plainGrid == null) return;
        if (!(plainGrid instanceof ShapeGrid)) {
            System.err.println("Cannot add shape to " + plainGrid);
            return;
        }
        ShapeGrid grid = (ShapeGrid) plainGrid;

        // See note in onAddGrid
        long shapeIdMsb = reader.readLong();
        long shapeIdLsb = reader.readLong();
        UUID shapeId = new UUID(shapeIdMsb, shapeIdLsb);
        removeShape(shapeId);

        alterShape(grid, shapeId, reader);
        saveGrids();
    }

    private void onRemoveShape(String type, MessageReader reader) {
        long shapeIdMsb = reader.readLong();
        long shapeIdLsb = reader.readLong();
        UUID shapeId = new UUID(shapeIdMsb, shapeIdLsb);
        removeShape(shapeId);
        saveGrids();
    }

    private void onGetFieldInfo(String type, MessageReader reader) {
        msg.prepare(MSG_FIELD_INFO)
                .addDouble(field.getCellSize())
                .addDouble(field.getWidth())
                .addDouble(field.getHeight())
                .addDouble(field.getOriginX())
                .addDouble(field.getOriginY())
                .addInt(field.getCellsX())
                .addInt(field.getCellsY())
                .send();
    }

    private void onGetGrids(String type, MessageReader reader) {
        MessageBuilder builder = msg.prepare(MSG_GRIDS);
        grids.addToMessenger(builder);
        builder.send();
    }

    private void onGetCellData(String type, MessageReader reader) {
        int width = grids.getCellWidth();
        int height = grids.getCellHeight();
        BitfieldGrid out = new BitfieldGrid(width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                out.set(x, y, grids.canCellPass(x, y));
            }
        }

        MessageBuilder builder = msg.prepare(MSG_CELL_DATA);
        out.writeToMessengerNoTypeId(builder);
        builder.send();
    }

    private void onGetRobotShape(String type, MessageReader reader) {
        MessageBuilder builder = msg.prepare(MSG_ROBOT_SHAPE);
        robot.writeToMessenger(builder);
        builder.send();
    }

    public void run() {
        while (true) {
            msg.readMessages();

            if (needsRecalcPath) {
                // Find path
                List<Point> path = pathfinder.findPath();

                // Send path
                MessageBuilder builder = msg.prepare(MSG_PATH);
                builder.addBoolean(path != null);
                if (path != null) {
                    builder.addInt(path.size());
                    for (Point p : path) {
                        builder.addDouble(field.convertPointX(p));
                        builder.addDouble(field.convertPointY(p));
                    }
                }
                builder.send();

                needsRecalcPath = false;
            }
        }
    }

    public void registerGrid(Grid grid) {
        idToGrid.put(grid.getId(), grid);
    }

    public void registerShape(Shape shape) {
        idToShape.put(shape.getId(), shape);
    }
}

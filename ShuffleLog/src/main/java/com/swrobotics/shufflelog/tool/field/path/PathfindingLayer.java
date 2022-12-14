package com.swrobotics.shufflelog.tool.field.path;

import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.messenger.client.MessengerClient;
import com.swrobotics.shufflelog.tool.ToolConstants;
import com.swrobotics.shufflelog.tool.field.FieldLayer;
import com.swrobotics.shufflelog.tool.field.path.grid.BitfieldGrid;
import com.swrobotics.shufflelog.tool.field.path.grid.Grid;
import com.swrobotics.shufflelog.tool.field.path.grid.GridUnion;
import com.swrobotics.shufflelog.tool.field.path.grid.ShapeGrid;
import com.swrobotics.shufflelog.tool.field.path.shape.Circle;
import com.swrobotics.shufflelog.tool.field.path.shape.Rectangle;
import com.swrobotics.shufflelog.tool.field.path.shape.Shape;
import com.swrobotics.shufflelog.util.Cooldown;
import imgui.flag.ImGuiTableFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImBoolean;
import processing.core.PConstants;
import processing.core.PGraphics;

import java.util.*;

import static imgui.ImGui.*;

public final class PathfindingLayer implements FieldLayer {
    // Main API
    private static final String MSG_SET_POS  = "Pathfinder:SetPos";
    private static final String MSG_SET_GOAL = "Pathfinder:SetGoal";
    private static final String MSG_PATH = "Pathfinder:Path";

    // ShuffleLog API
    private static final String MSG_GET_FIELD_INFO = "Pathfinder:GetFieldInfo";
    private static final String MSG_GET_GRIDS = "Pathfinder:GetGrids";
    private static final String MSG_GET_CELL_DATA = "Pathfinder:GetCellData";
    private static final String MSG_ADD_GRID = "Pathfinder:AddGrid";
    private static final String MSG_REMOVE_GRID = "Pathfinder:RemoveGrid";
    private static final String MSG_ADD_SHAPE = "Pathfinder:AddShape";
    private static final String MSG_ALTER_SHAPE = "Pathfinder:AlterShape";
    private static final String MSG_REMOVE_SHAPE = "Pathfinder:RemoveShape";

    private static final String MSG_FIELD_INFO = "Pathfinder:FieldInfo";
    private static final String MSG_GRIDS = "Pathfinder:Grids";
    private static final String MSG_CELL_DATA = "Pathfinder:CellData";

    private final MessengerClient msg;
    private final Cooldown reqFieldInfoCooldown;
    private final Cooldown reqGridsCooldown;
    private final Cooldown reqCellDataCooldown;

    private final ImBoolean showGridLines;
    private final ImBoolean showGridCells;
    private final ImBoolean showShapes;
    private final ImBoolean showPath;

    private final Map<UUID, Grid> idToGrid;
    private final Map<UUID, Shape> idToShape;
    private FieldInfo fieldInfo;
    private List<Point> path;
    private Grid grid;
    private BitfieldGrid cellData;
    private boolean needsRefreshCellData;

    private double startX, startY;
    private double goalX, goalY;

    private FieldNode hoveredNode;

    public PathfindingLayer(MessengerClient msg) {
        this.msg = msg;
        reqFieldInfoCooldown = new Cooldown(ToolConstants.MSG_QUERY_COOLDOWN_TIME);
        reqGridsCooldown = new Cooldown(ToolConstants.MSG_QUERY_COOLDOWN_TIME);
        reqCellDataCooldown = new Cooldown(ToolConstants.MSG_QUERY_COOLDOWN_TIME);

        msg.addHandler(MSG_PATH, this::onPath);
        msg.addHandler(MSG_FIELD_INFO, this::onFieldInfo);
        msg.addHandler(MSG_GRIDS, this::onGrids);
        msg.addHandler(MSG_CELL_DATA, this::onCellData);
        msg.addHandler(MSG_SET_POS, this::onSetPos);
        msg.addHandler(MSG_SET_GOAL, this::onSetGoal);

        showGridLines = new ImBoolean(false);
        showGridCells = new ImBoolean(true);
        showShapes = new ImBoolean(true);
        showPath = new ImBoolean(true);

        idToGrid = new HashMap<>();
        idToShape = new HashMap<>();
        fieldInfo = null;
        path = null;
        grid = null;
        cellData = null;
        needsRefreshCellData = true;
    }

    private void onPath(String type, MessageReader reader) {
        boolean valid = reader.readBoolean();
        if (valid) {
            if (path != null)
                path.clear();
            else
                path = new ArrayList<>();

            int count = reader.readInt();
            for (int i = 0; i < count; i++) {
                double x = reader.readDouble();
                double y = reader.readDouble();
                path.add(new Point(x, y));
            }
        } else {
            path = null;
        }
    }

    private void onFieldInfo(String type, MessageReader reader) {
        fieldInfo = new FieldInfo(reader);
    }

    private void onGrids(String type, MessageReader reader) {
        grid = Grid.read(reader);
        idToGrid.clear();
        idToShape.clear();
        grid.register(this);
    }

    private void onCellData(String type, MessageReader reader) {
        cellData = new BitfieldGrid(null);
        cellData.readContent(reader);
        needsRefreshCellData = false;
    }

    private void onSetPos(String type, MessageReader reader) {
        startX = reader.readDouble();
        startY = reader.readDouble();
    }

    private void onSetGoal(String type, MessageReader reader) {
        goalX = reader.readDouble();
        goalY = reader.readDouble();
    }

    @Override
    public String getName() {
        return "Pathfinding";
    }

    @Override
    public void draw(PGraphics g, float metersScale) {
        if (grid == null && reqGridsCooldown.request()) {
            msg.send(MSG_GET_GRIDS);
        }
        if (needsRefreshCellData && reqCellDataCooldown.request()) {
            msg.send(MSG_GET_CELL_DATA);
        }
        if (fieldInfo == null) {
            if (reqFieldInfoCooldown.request())
                msg.send(MSG_GET_FIELD_INFO);
            return;
        }

        // Wavy ends go wheeeeeeee (for testing latency)
        msg.prepare(MSG_SET_POS)
                .addDouble(3 * Math.sin((System.currentTimeMillis() % 1000) / 1000.0 * Math.PI * 2))
                .addDouble(-6)
                .send();
        msg.prepare(MSG_SET_GOAL)
                .addDouble(3 * Math.cos(((System.currentTimeMillis() * 1.253) % 1000 / 1000.0) * Math.PI * 2))
                .addDouble(6)
                .send();

        boolean lines = showGridLines.get();
        boolean cells = showGridCells.get();
        boolean shapes = showShapes.get();
        boolean path = showPath.get();

        float strokeMul = 1 / metersScale;
        g.pushMatrix();
        {
            // Transform into cell space
            float cellSize = (float) fieldInfo.getCellSize();
            g.scale(cellSize, -cellSize);
            g.translate((float) -fieldInfo.getOriginX(), (float) -fieldInfo.getOriginY());
            float cellStrokeMul = strokeMul / cellSize;

            int cellsX = fieldInfo.getCellsX();
            int cellsY = fieldInfo.getCellsY();

            // Show cell data content
            if (cells && cellData != null) {
                g.fill(200, 0, 0, 196);
                g.noStroke();
                for (int y = 0; y < cellsY; y++) {
                    for (int x = 0; x < cellsX; x++) {
                        boolean passable = cellData.get(x, y);
                        if (!passable) {
                            g.rect(x, y, 1, 1);
                        }
                    }
                }
            }

            // Show grid lines
            if (lines) {
                g.strokeWeight(0.5f * cellStrokeMul);
                g.stroke(96);

                for (int x = 0; x <= cellsX; x++) {
                    g.line(x, 0, x, cellsY);
                }
                for (int y = 0; y <= cellsY; y++) {
                    g.line(0, y, cellsX, y);
                }
            }
        }
        g.popMatrix();

        // Show shapes
        if (shapes) {
            drawShapes(g, grid, strokeMul, g.color(201, 101, 18), g.color(201, 101, 18, 128));
            drawShapes(g, hoveredNode, strokeMul, g.color(46, 174, 217), g.color(46, 174, 217, 128));
        }

        // Show path
        if (path) {
            if (this.path != null) {
                g.strokeWeight(4 * strokeMul);
                g.stroke(214, 196, 32, 128);
                g.beginShape(PConstants.LINE_STRIP);
                for (Point p : this.path)
                    g.vertex((float) p.x, (float) p.y);
                g.endShape();

                g.strokeWeight(2 * strokeMul);
                g.stroke(214, 196, 32);
                g.beginShape(PConstants.LINE_STRIP);
                for (Point p : this.path)
                    g.vertex((float) p.x, (float) p.y);
                g.endShape();
            }

            // Show endpoints
            g.pushMatrix();
            g.translate(0, 0, 0.005f);
            g.strokeWeight(strokeMul);
            g.ellipseMode(PConstants.CENTER);
            g.stroke(27, 196, 101, 128);
            g.fill(27, 196, 101);
            float startSize = startX == goalX && startY == goalY ? 0.12f*strokeMul : 0.10f*strokeMul;
            g.ellipse((float) startX, (float) startY, startSize, startSize);
            g.stroke(44, 62, 199, 128);
            g.fill(44, 62, 199);
            g.ellipse((float) goalX, (float) goalY, 0.10f*strokeMul, 0.10f*strokeMul);
            g.popMatrix();
        }
    }

    private void drawShape(PGraphics g, Shape shape, float strokeMul, int fg, int bg) {
        if (shape instanceof Circle) {
            Circle c = (Circle) shape;
            g.ellipseMode(PConstants.CENTER);
            g.noFill();

            float x = (float) c.x.get();
            float y = (float) c.y.get();
            float d = (float) (2 * c.radius.get());

            g.strokeWeight(4 * strokeMul);
            g.stroke(bg);
            g.ellipse(x, y, d, d);
            g.strokeWeight(2 * strokeMul);
            g.stroke(fg);
            g.ellipse(x, y, d, d);
        } else if (shape instanceof Rectangle) {
            Rectangle r = (Rectangle) shape;
            float x = (float) r.x.get();
            float y = (float) r.y.get();
            float w = (float) r.width.get();
            float h = (float) r.height.get();
            float rot = (float) r.rotation.get();

            g.pushMatrix();
            g.translate(x, y);
            g.rotate((float) Math.toRadians(rot));

            g.noFill();
            g.strokeWeight(4 * strokeMul);
            g.stroke(bg);
            g.rect(-w/2, -h/2, w, h);
            g.stroke(fg);
            g.strokeWeight(2 * strokeMul);
            g.rect(-w/2, -h/2, w, h);

            g.popMatrix();
        }
    }

    private void drawShapes(PGraphics g, FieldNode node, float strokeMul, int fg, int bg) {
        if (node instanceof Shape) {
            drawShape(g, (Shape) node, strokeMul, fg, bg);
        } else if (node instanceof ShapeGrid) {
            for (Shape shape : ((ShapeGrid) node).getShapes()) {
                drawShape(g, shape, strokeMul, fg, bg);
            }
        } else if (node instanceof GridUnion) {
            for (Grid grid : ((GridUnion) node).getChildren()) {
                drawShapes(g, grid, strokeMul, fg, bg);
            }
        }
    }

    private void showGridUnion(GridUnion union, boolean isRoot) {
        String id = "Grid Union##" + union.getId();
        int flags = ImGuiTreeNodeFlags.SpanFullWidth;
        if (isRoot) {
            flags |= ImGuiTreeNodeFlags.DefaultOpen;
        }

        tableNextColumn();
        boolean open = treeNodeEx(id, flags);
        if (isItemHovered()) hoveredNode = grid;
        tableNextColumn();
        textDisabled(union.getId().toString());

        if (open) {
            for (Grid grid : union.getChildren()) {
                showGrid(grid, false);
            }
            treePop();
        }
    }

    private void showBitfieldGrid(BitfieldGrid grid) {
        String id = "Bitfield Grid##" + grid.getId();
        tableNextColumn();
        treeNodeEx(id, ImGuiTreeNodeFlags.SpanFullWidth | ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen);
        if (isItemHovered()) hoveredNode = grid;
        tableNextColumn();
        textDisabled(grid.getId().toString());
    }

    private void showShapeGrid(ShapeGrid grid, boolean isRoot) {
        String id = "Shape Grid##" + grid.getId();
        int flags = ImGuiTreeNodeFlags.SpanFullWidth;
        if (isRoot) {
            flags |= ImGuiTreeNodeFlags.DefaultOpen;
        }

        tableNextColumn();
        boolean open = treeNodeEx(id, flags);
        if (isItemHovered()) hoveredNode = grid;
        if (beginPopupContextItem()) {
            Shape addedShape = null;
            if (selectable("Add Circle")) {
                Circle c = new Circle(UUID.randomUUID());
                c.register(this);
                c.x.set(0);
                c.y.set(0);
                c.radius.set(1);
                addedShape = c;
            }
            if (selectable("Add Rectangle")) {
                Rectangle r = new Rectangle(UUID.randomUUID());
                r.register(this);
                r.x.set(0);
                r.y.set(0);
                r.width.set(1);
                r.height.set(1);
                r.rotation.set(0);
                addedShape = r;
            }

            if (addedShape != null) {
                grid.getShapes().add(addedShape);
                MessageBuilder builder = msg.prepare(MSG_ADD_SHAPE);
                builder.addLong(grid.getId().getMostSignificantBits());
                builder.addLong(grid.getId().getLeastSignificantBits());
                addedShape.write(builder);
                builder.send();
                needsRefreshCellData = true;
            }

            endPopup();
        }
        tableNextColumn();
        textDisabled(grid.getId().toString());

        if (open) {
            for (Shape shape : new ArrayList<>(grid.getShapes())) {
                showShape(grid, shape);
            }
            treePop();
        }
    }

    private void showGrid(Grid grid, boolean isRoot) {
        if (grid instanceof GridUnion)
            showGridUnion((GridUnion) grid, isRoot);
        else if (grid instanceof BitfieldGrid)
            showBitfieldGrid((BitfieldGrid) grid);
        else if (grid instanceof ShapeGrid)
            showShapeGrid((ShapeGrid) grid, isRoot);
    }

    private void removeShape(ShapeGrid grid, Shape shape) {
        grid.getShapes().remove(shape);
        idToShape.remove(shape.getId());
        msg.prepare(MSG_REMOVE_SHAPE)
                .addLong(shape.getId().getMostSignificantBits())
                .addLong(shape.getId().getLeastSignificantBits())
                .send();
    }

    private void fieldHeader(String name) {
        tableNextColumn();
        alignTextToFramePadding();
        treeNodeEx(name, ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen | ImGuiTreeNodeFlags.SpanFullWidth);
        tableNextColumn();
        setNextItemWidth(-1);
    }

    private void showCircle(ShapeGrid grid, Circle circle) {
        String id = "Circle##" + circle.getId();

        tableNextColumn();
        boolean open = treeNodeEx(id, ImGuiTreeNodeFlags.SpanFullWidth);
        if (isItemHovered()) hoveredNode = circle;
        if (beginPopupContextItem()) {
            if (selectable("Delete")) {
                removeShape(grid, circle);
            }
            endPopup();
        }
        tableNextColumn();
        textDisabled(circle.getId().toString());

        if (open) {
            boolean changed;

            fieldHeader("X"); changed = inputDouble("##x", circle.x);
            fieldHeader("Y"); changed |= inputDouble("##y", circle.y);
            fieldHeader("Radius"); changed |= inputDouble("##radius", circle.radius);

            if (changed) {
                MessageBuilder builder = msg.prepare(MSG_ALTER_SHAPE);
                circle.write(builder);
                builder.send();
                needsRefreshCellData = true;
            }

            treePop();
        }
    }

    private void showRectangle(ShapeGrid grid, Rectangle rect) {
        String id = "Rectangle##" + rect.getId();

        tableNextColumn();
        boolean open = treeNodeEx(id, ImGuiTreeNodeFlags.SpanFullWidth);
        if (isItemHovered()) hoveredNode = rect;
        if (beginPopupContextItem()) {
            if (selectable("Delete")) {
                removeShape(grid, rect);
            }
            endPopup();
        }
        tableNextColumn();
        textDisabled(rect.getId().toString());

        if (open) {
            boolean changed;
            fieldHeader("X"); changed = inputDouble("##x", rect.x);
            fieldHeader("Y"); changed |= inputDouble("##y", rect.y);
            fieldHeader("Width"); changed |= inputDouble("##width", rect.width);
            fieldHeader("Height"); changed |= inputDouble("##height", rect.height);
            fieldHeader("Rotation"); changed |= inputDouble("##rotation", rect.rotation);

            if (changed) {
                MessageBuilder builder = msg.prepare(MSG_ALTER_SHAPE);
                rect.write(builder);
                builder.send();
                needsRefreshCellData = true;
            }

            treePop();
        }
    }

    private void showShape(ShapeGrid grid, Shape shape) {
        if (shape instanceof Circle)
            showCircle(grid, (Circle) shape);
        else if (shape instanceof Rectangle)
            showRectangle(grid, (Rectangle) shape);
    }

    @Override
    public void showGui() {
        checkbox("Show grid lines", showGridLines);
        checkbox("Show grid cells", showGridCells);
        checkbox("Show shapes", showShapes);
        checkbox("Show path", showPath);
        separator();
        if (grid != null) {
            if (beginTable("grids", 2, ImGuiTableFlags.Borders | ImGuiTableFlags.Resizable)) {
                hoveredNode = null;
                showGrid(grid, true);
                endTable();
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

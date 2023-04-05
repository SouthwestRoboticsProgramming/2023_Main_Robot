package com.swrobotics.pathfinding.field;

import com.swrobotics.pathfinding.core.grid.Point;

/**
 * Cell coordinates have +X right and +Y down, and 1 unit corresponds to 1 cell. Meter coordinates
 * have +X right and +Y up, and 1 unit corresponds to 1 meter.
 */
public final class Field {
    private final double cellSize; // meters
    private final double width, height; // meters
    private final int cellsX, cellsY; // cells
    private final double originX, originY; // cells

    /**
     * Constructs a new Field.
     *
     * @param cellSize size of each cell in meters
     * @param width width of the field in meters
     * @param height height of the field in meters
     * @param originX origin X as percentage of width in cell space (lower is left)
     * @param originY origin Y as percentage of height in cell space (lower is up)
     */
    public Field(double cellSize, double width, double height, double originX, double originY) {
        this.cellSize = cellSize;
        this.width = width;
        this.height = height;
        cellsX = (int) Math.ceil(width / cellSize);
        cellsY = (int) Math.ceil(height / cellSize);
        this.originX = originX * cellsX;
        this.originY = originY * cellsY;
    }

    public double convertCellToMetersX(double cell) {
        return (cell - originX) * cellSize;
    }

    public double convertCellToMetersY(double cell) {
        return -(cell - originY) * cellSize;
    }

    public double convertMetersToCellX(double x) {
        return x / cellSize + originX;
    }

    public double convertMetersToCellY(double y) {
        return originY - y / cellSize;
    }

    /**
     * Gets the X position of the center of a cell in meters relative to the field origin.
     *
     * @param cellX cell X position
     * @return cell center X in meters
     */
    public double getCellCenterX(int cellX) {
        return convertCellToMetersX(cellX + 0.5);
    }

    /**
     * Gets the Y position of the center of a cell in meters relative to the field origin.
     *
     * @param cellY cell Y position
     * @return cell center Y in meters
     */
    public double getCellCenterY(int cellY) {
        return convertCellToMetersY(cellY + 0.5);
    }

    public double convertPointX(Point point) {
        return convertCellToMetersX(point.x);
    }

    public double convertPointY(Point point) {
        return convertCellToMetersY(point.y);
    }

    private double clamp(double v, double min, double max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }

    public Point getNearestPoint(double x, double y) {
        double cellX = convertMetersToCellX(x);
        double cellY = convertMetersToCellY(y);
        return new Point(
                (int) clamp(Math.round(cellX), 0, cellsX),
                (int) clamp(Math.round(cellY), 0, cellsY));
    }

    public int getCellsX() {
        return cellsX;
    }

    public int getCellsY() {
        return cellsY;
    }

    public double getCellSize() {
        return cellSize;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getOriginX() {
        return originX;
    }

    public double getOriginY() {
        return originY;
    }
}

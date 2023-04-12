package com.swrobotics.shufflelog.tool.field.path;

import com.swrobotics.messenger.client.MessageReader;

public final class FieldInfo {
    private final double cellSize;
    private final double fieldWidth, fieldHeight;
    private final double originX, originY;
    private final int cellsX, cellsY;

    public FieldInfo(MessageReader reader) {
        cellSize = reader.readDouble();
        fieldWidth = reader.readDouble();
        fieldHeight = reader.readDouble();
        originX = reader.readDouble();
        originY = reader.readDouble();
        cellsX = reader.readInt();
        cellsY = reader.readInt();
    }

    public double getCellSize() {
        return cellSize;
    }

    public double getFieldWidth() {
        return fieldWidth;
    }

    public double getFieldHeight() {
        return fieldHeight;
    }

    public double getOriginX() {
        return originX;
    }

    public double getOriginY() {
        return originY;
    }

    public int getCellsX() {
        return cellsX;
    }

    public int getCellsY() {
        return cellsY;
    }

    @Override
    public String toString() {
        return "FieldInfo{"
                + "cellSize="
                + cellSize
                + ", fieldWidth="
                + fieldWidth
                + ", fieldHeight="
                + fieldHeight
                + ", originX="
                + originX
                + ", originY="
                + originY
                + ", cellsX="
                + cellsX
                + ", cellsY="
                + cellsY
                + '}';
    }
}

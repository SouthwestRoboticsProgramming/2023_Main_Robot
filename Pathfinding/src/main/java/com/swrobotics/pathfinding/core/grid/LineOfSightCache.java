package com.swrobotics.pathfinding.core.grid;

import java.util.BitSet;

// Optimization: Caches line-of-sight checks between every combination of points
public final class LineOfSightCache {
    private final Grid grid;
    private final int width;
    private final int pointCount;

    private final BitSet present;
    private final BitSet storage;

    public LineOfSightCache(Grid grid) {
        this.grid = grid;
        this.width = grid.getCellWidth();
        this.pointCount = width * grid.getCellHeight();

        int bitCount = pointCount * pointCount;
        present = new BitSet(bitCount);
        storage = new BitSet(bitCount);
    }

    public void invalidate() {
        present.clear();
    }

    private int index(int x1, int y1, int x2, int y2) {
        return x1 + y1 * width + (x2 + y2 * width) * pointCount;
    }

    public boolean lineOfSight(Point a, Point b) {
        int idx = index(a.x, a.y, b.x, b.y);
        if (present.get(idx)) return storage.get(idx);

        boolean out = grid.calcLineOfSight(a, b);
        present.set(idx);
        storage.set(idx, out);
        return out;
    }
}

package com.swrobotics.pathfinding.finder;

import com.swrobotics.pathfinding.Point;
import com.swrobotics.pathfinding.grid.Grid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class AStarPathfinder implements Pathfinder {
    protected final Grid grid;

    private Node[][] nodes;
    private Point startPoint, goalPoint;

    public AStarPathfinder(Grid grid) {
        this.grid = grid;
    }

    @Override
    public void setStart(Point start) {
        startPoint = start;
    }

    @Override
    public void setGoal(Point goal) {
        goalPoint = goal;
    }

    protected static final class Node implements Comparable<Node> {
        Point position;
        double priority;
        double cost;
        Node parent;
        boolean closed = false;

        @Override
        public int compareTo(Node o) {
            return Double.compare(priority, o.priority);
        }
    }

    protected double getHeuristic(Node node) {
        int dx = node.position.x - goalPoint.x;
        int dy = node.position.y - goalPoint.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    protected double getCost(Node current, Node next) {
        int dx = current.position.x - next.position.x;
        int dy = current.position.y - next.position.y;

        return Math.sqrt(dx * dx + dy * dy);
    }

    private List<Point> extractPath(Node node) {
        List<Point> out = new ArrayList<>();
        while (node != null) {
            out.add(0, node.position);
            node = node.parent;
        }
        return out;
    }

    private int getNeighbors(Node current, Node[] neighbors) {
        int i = 0;

        int w = grid.getPointWidth();
        int h = grid.getPointHeight();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (x == 0 && y == 0)
                    continue;

                int px = current.position.x + x;
                int py = current.position.y + y;
                if (px >= 0 && px < w && py >= 0 && py < h && grid.lineOfSight(current.position, nodes[px][py].position)) {
                    neighbors[i++] = nodes[px][py];
                }
            }
        }

        return i;
    }

    protected void computeCost(Node current, Node next) {
        double cost = current.cost + getCost(current, next);
        if (cost < next.cost) {
            next.parent = current;
            next.cost = cost;
        }
    }

    private void updateVertex(PriorityQueue<Node> open, Node current, Node next) {
        double oldCost = next.cost;
        computeCost(current, next);
        if (next.cost < oldCost) {
            open.remove(next);
            next.priority = next.cost + getHeuristic(next);
            open.add(next);
        }
    }

    @Override
    public List<Point> findPath() {
        int width = grid.getPointWidth();
        int height = grid.getPointHeight();
        nodes = new Node[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Node node = new Node();
                node.position = new Point(x, y);
                nodes[x][y] = node;
            }
        }
        Node start = nodes[startPoint.x][startPoint.y];
        Node goal = nodes[goalPoint.x][goalPoint.y];

        PriorityQueue<Node> open = new PriorityQueue<>();
        start.priority = start.cost + getHeuristic(start);
        open.add(start);

        Node[] neighbors = new Node[8];
        while (!open.isEmpty()) {
            Node current = open.remove();
            if (current == goal) {
                return extractPath(current);
            }

            current.closed = true;

            int count = getNeighbors(current, neighbors);
            for (int i = 0; i < count; i++) {
                Node next = neighbors[i];
                if (!next.closed) {
                    if (!open.contains(next)) {
                        next.cost = Double.POSITIVE_INFINITY;
                        next.parent = null;
                    }
                    updateVertex(open, current, next);
                }
            }
        }

        return null;
    }
}

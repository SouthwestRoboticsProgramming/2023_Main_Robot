package com.swrobotics.pathfinding.core.finder;

import java.util.*;

// TODO: Optimize if needed
public class AStarPathfinder<P> implements Pathfinder<P> {
    protected final Graph<P> graph;

    private final Map<P, Node<P>> nodes = new HashMap<>();
    private P startPoint, goalPoint;

    public AStarPathfinder(Graph<P> graph) {
        this.graph = graph;
    }

    @Override
    public void setStart(P start) {
        startPoint = start;
    }

    @Override
    public void setGoal(P goal) {
        goalPoint = goal;
    }

    protected static final class Node<P> implements Comparable<Node<P>> {
        P position;
        double priority;
        double cost;
        Node<P> parent;
        boolean closed = false;

        @Override
        public int compareTo(Node<P> o) {
            return Double.compare(priority, o.priority);
        }
    }

    private List<P> extractPath(Node<P> node) {
        List<P> out = new ArrayList<>();
        while (node != null) {
            out.add(0, node.position);
            node = node.parent;
        }
        return out;
    }

    protected void computeCost(Node<P> current, Node<P> next) {
        double cost = current.cost + graph.cost(current.position, next.position);
        if (cost < next.cost) {
            next.parent = current;
            next.cost = cost;
        }
    }

    private void updateVertex(PriorityQueue<Node<P>> open, Node<P> current, Node<P> next) {
        double oldCost = next.cost;
        computeCost(current, next);
        if (next.cost < oldCost) {
            open.remove(next);
            next.priority = next.cost + graph.heuristic(next.position, goalPoint);
            open.add(next);
        }
    }

    private Node<P> getOrCreateNode(P point) {
        Node<P> node = nodes.get(point);
        if (node == null) {
            node = new Node<>();
            node.position = point;
            nodes.put(point, node);
        }
        return node;
    }

    @Override
    public List<P> findPath() {
        nodes.clear();
        Node<P> start = getOrCreateNode(startPoint);
        Node<P> goal = getOrCreateNode(goalPoint);

        PriorityQueue<Node<P>> open = new PriorityQueue<>();
        start.priority = start.cost + graph.heuristic(start.position, goalPoint);
        open.add(start);

        int[] neighborCount = new int[1];
        while (!open.isEmpty()) {
            Node<P> current = open.remove();
            if (current == goal) {
                return extractPath(current);
            }

            current.closed = true;

            P[] neighborPoints = graph.getNeighbors(current.position, neighborCount);
            for (int i = 0; i < neighborCount[0]; i++) {
                Node<P> next = getOrCreateNode(neighborPoints[i]);
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

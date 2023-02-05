package com.swrobotics.robot.subsystems.arm;

import com.swrobotics.mathlib.MathUtil;
import com.swrobotics.messenger.client.MessageBuilder;
import com.swrobotics.messenger.client.MessageReader;
import com.swrobotics.messenger.client.MessengerClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

// Theta* pathfinder to find arm movement path that avoids any illegal positions
// TODO: Test on RIO, if too slow, move to Pi
//       Might want to do this anyway, since this shares a lot of code with Pathfinding
public final class ArmPathSolver {
    private static final String MSG_GET_STATESPACE = "Arm:GetStatespace";
    private static final String MSG_STATESPACE = "Arm:Statespace";
    private static final String MSG_ARM_PATH_UPDATE = "Arm:PathUpdate";

    private static final int RESOLUTION = 128;
    private static final double MIN_BOTTOM_ANGLE = 0;
    private static final double MAX_BOTTOM_ANGLE = Math.PI;
    private static final double MIN_TOP_ANGLE = 0;
    private static final double MAX_TOP_ANGLE = 2 * Math.PI;

    private static final double BOTTOM_GEAR_RATIO = 600;
    private static final double TOP_GEAR_RATIO = 300;

    private final State[][] states;

    // Messenger API for debugging in ShuffleLog
    // TODO: Add ability to disable sending state during matches to reduce network usage
    private final MessengerClient msg;

    public ArmPathSolver(MessengerClient msg) {
        states = new State[RESOLUTION][RESOLUTION];
        for (int bot = 0; bot < RESOLUTION; bot++) {
            for (int top = 0; top < RESOLUTION; top++) {
                states[bot][top] = new State(bot, top, new ArmPose(
                        MathUtil.lerp(MIN_BOTTOM_ANGLE, MAX_BOTTOM_ANGLE, (double) bot / (RESOLUTION - 1)),
                        MathUtil.wrap(MathUtil.lerp(MIN_TOP_ANGLE, MAX_TOP_ANGLE, (double) top / (RESOLUTION - 1)) + Math.PI, 0, Math.PI * 2)
                ));
            }
        }

        this.msg = msg;
        msg.addHandler(MSG_GET_STATESPACE, this::onGetStatespace);
    }

    private void onGetStatespace(String type, MessageReader reader) {
        MessageBuilder builder = msg.prepare(MSG_STATESPACE);
        builder.addInt(RESOLUTION);
        for (int bot = 0; bot < RESOLUTION; bot++) {
            for (int top = 0; top < RESOLUTION; top++) {
                builder.addBoolean(states[bot][top].valid);
            }
        }
        builder.send();
    }

    private static final class State implements Comparable<State> {
        private final ArmPose pose;
        private final int botIdx, topIdx;
        private final boolean valid;

        private double priority;
        private double cost;
        private State parent;
        private boolean closed;

        public State(int botIdx, int topIdx, ArmPose pose) {
            this.botIdx = botIdx;
            this.topIdx = topIdx;
            this.pose = pose;
            valid = pose.isValid(); // Cache validity of pose
            reset();
        }

        public void reset() {
            parent = null;
            closed = false;
            cost = 0;
        }

        @Override
        public int compareTo(State state) {
            return Double.compare(priority, state.priority);
        }
    }

    private double heuristic(State state, State goal) {
        return cost(state, goal);
    }

    private double cost(State state, State next) {
        double diffBot = (state.pose.bottomAngle - next.pose.bottomAngle) * BOTTOM_GEAR_RATIO;
        double diffTop = (state.pose.topAngle - next.pose.topAngle) * TOP_GEAR_RATIO;

        return Math.sqrt(diffBot * diffBot + diffTop * diffTop);
    }

    private boolean isStateValid(int bot, int top) {
        if (bot < 0 || bot >= RESOLUTION || top < 0 || top >= RESOLUTION)
            return false;

        return states[bot][top].valid;
    }

    private boolean canDirectlyMoveTo(State current, State target) {
        int x0 = current.botIdx;
        int y0 = current.topIdx;
        int x1 = target.botIdx;
        int y1 = target.topIdx;
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
                    if (!isStateValid(x0 + ((sx - 1)/2), y0 + ((sy - 1)/2))) {
                        return false;
                    }
                    y0 = y0 + sy;
                    f = f - dx;
                }
                if (f != 0 && !isStateValid(x0 + ((sx - 1)/2), y0 + ((sy - 1)/2))) {
                    return false;
                }
                if (dy == 0 && !isStateValid(x0 + ((sx - 1)/2), y0) && !isStateValid(x0 + ((sx - 1)/2), y0 - 1)) {
                    return false;
                }
                x0 = x0 + sx;
            }
        } else {
            while (y0 != y1) {
                f = f + dx;
                if (f >= dy) {
                    if (!isStateValid(x0 + ((sx - 1)/2), y0 + ((sy - 1)/2))) {
                        return false;
                    }
                    x0 = x0 + sx;
                    f = f - dy;
                }
                if (f != 0 && !isStateValid(x0 + ((sx - 1)/2), y0 + ((sy - 1)/2))) {
                    return false;
                }
                if (dx == 0 && !isStateValid(x0, y0 + ((sy - 1)/2)) && !isStateValid(x0 - 1, y0 + ((sy - 1)/2))) {
                    return false;
                }
                y0 = y0 + sy;
            }
        }

        return true;
    }

    private int getNeighbors(State current, State[] outNeighbors) {
        int i = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (x == 0 && y == 0)
                    continue;

                int px = current.botIdx + x;
                int py = current.topIdx + y;

                if (px >= 0 && px < RESOLUTION && py >= 0 && py < RESOLUTION && canDirectlyMoveTo(current, states[px][py])) {
                    outNeighbors[i++] = states[px][py];
                }
            }
        }
        return i;
    }

    private void computeCost(State current, State next) {
        if (current.parent != null && canDirectlyMoveTo(current.parent, next)) {
            double newCost = current.parent.cost + cost(current.parent, next);
            if (newCost < next.cost) {
                next.parent = current.parent;
                next.cost = newCost;
            }
        } else {
            double cost = current.cost + cost(current, next);
            if (cost < next.cost) {
                next.parent = current;
                next.cost = cost;
            }
        }
    }

    private void updateVertex(PriorityQueue<State> open, State current, State next, State goal) {
        double oldCost = next.cost;
        computeCost(current, next);
        if (next.cost < oldCost) {
            open.remove(next);
            next.priority = next.cost + heuristic(next, goal);
            open.add(next);
        }
    }

    private ArmPose wrap(ArmPose pose) {
        double wrapBot = MathUtil.wrap(pose.bottomAngle, 0, Math.PI * 2);
        double wrapTop = MathUtil.wrap(pose.topAngle, 0, Math.PI * 2);
        return new ArmPose(wrapBot, wrapTop);
    }

    private State closestState(ArmPose pose) {
        pose = wrap(pose);
        double bot = MathUtil.map(pose.bottomAngle, MIN_BOTTOM_ANGLE, MAX_BOTTOM_ANGLE, 0, RESOLUTION - 1);
        double top = MathUtil.map(MathUtil.wrap(pose.topAngle + Math.PI, 0, Math.PI * 2), MIN_TOP_ANGLE, MAX_TOP_ANGLE, 0, RESOLUTION - 1);

        int botIdx = (int) (bot + 0.5);
        if (botIdx < 0) botIdx = 0;
        if (botIdx >= RESOLUTION) botIdx = RESOLUTION - 1;

        int topIdx = (int) (top + 0.5);
        if (topIdx < 0) topIdx = 0;
        if (topIdx >= RESOLUTION) topIdx = RESOLUTION - 1;

        return states[botIdx][topIdx];
    }

    private List<ArmPose> extractPath(State targetState, ArmPose targetPose) {
        List<ArmPose> out = new ArrayList<>();
        out.add(targetPose);
        targetState = targetState.parent;
        while (targetState != null) {
            out.add(0, targetState.pose);
            targetState = targetState.parent;
        }
        return out;
    }

    public Optional<List<ArmPose>> findPath(ArmPose currentPose, ArmPose targetPose) {
        currentPose = wrap(currentPose);
        targetPose = wrap(targetPose);
        Optional<List<ArmPose>> path = findPathImpl(currentPose, targetPose);

        MessageBuilder builder = msg.prepare(MSG_ARM_PATH_UPDATE);
        builder.addDouble(currentPose.bottomAngle);
        builder.addDouble(currentPose.topAngle);
        builder.addDouble(targetPose.bottomAngle);
        builder.addDouble(targetPose.topAngle);

        boolean hasPath = path.isPresent();
        builder.addBoolean(hasPath);
        if (hasPath) {
            List<ArmPose> poses = path.get();
            builder.addInt(poses.size());
            for (ArmPose pose : poses) {
                builder.addDouble(pose.bottomAngle);
                builder.addDouble(pose.topAngle);
            }
        }
        builder.send();

        return path;
    }

    public Optional<List<ArmPose>> findPathImpl(ArmPose currentPose, ArmPose targetPose) {
        for (int bot = 0; bot < RESOLUTION; bot++)
            for (int top = 0; top < RESOLUTION; top++)
                states[bot][top].reset();

        State start = closestState(currentPose);
        State goal = closestState(targetPose);

        PriorityQueue<State> open = new PriorityQueue<>();
        start.priority = start.cost + heuristic(start, goal);
        open.add(start);

        State[] neighbors = new State[8];
        while (!open.isEmpty()) {
            State current = open.remove();
            if (current == goal)
                return Optional.of(extractPath(current, targetPose));

            current.closed = true;

            int count = getNeighbors(current, neighbors);
            for (int i = 0; i < count; i++) {
                State next = neighbors[i];
                if (next.closed)
                    continue;

                if (!open.contains(next)) {
                    next.cost = Double.POSITIVE_INFINITY;
                    next.parent = null;
                }
                updateVertex(open, current, next, goal);
            }
        }

        // It's impossible for the arm to reach the target state
        return Optional.empty();
    }

    public void printStateSpaceDebug(ArmPose current, ArmPose target) {
        State currentState = closestState(current);
        State targetState = closestState(target);

        for (int bot = 0; bot < RESOLUTION; bot++) {
            StringBuilder builder = new StringBuilder();
            for (int top = 0; top < RESOLUTION; top++) {
                if (bot == currentState.botIdx && top == currentState.topIdx)
                    builder.append("C ");
                else if (bot == targetState.botIdx && top == targetState.topIdx)
                    builder.append("T ");
                else if (states[bot][top].valid)
                    builder.append(". ");
                else
                    builder.append("# ");
            }
            System.out.println(builder);
        }
    }
}

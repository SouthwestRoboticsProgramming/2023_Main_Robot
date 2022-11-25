package com.swrobotics.lib.schedule;

import edu.wpi.first.wpilibj.DriverStation;

import java.util.*;

import com.swrobotics.lib.profile.Profiler;
import com.swrobotics.lib.wpilib.RobotState;

/**
 * The {@code Scheduler} is the core of the robot code. It runs
 * {@code Command}s and {@code Subsystem}s every periodic, and
 * manages the starting and stopping of {@code Command}s. The
 * {@code Scheduler} instance is used by {@code AbstractRobot}
 * within the main loop, to make it easier to organize your
 * robot code.
 *
 * @see Command
 * @see Subsystem
 * @see com.swrobotics.lib.wpilib.AbstractRobot
 */
public final class Scheduler {
    private static final Scheduler INSTANCE = new Scheduler();

    /**
     * Gets the global singleton instance of the {@code Scheduler}.
     *
     * @return singleton instance
     */
    public static Scheduler get() {
        return INSTANCE;
    }

    private static abstract class Node {
        private boolean selfSuspended, parentSuspended;
        private RobotState prevInitializedState;
        private SubsystemNode parent;

        public Node() {
            selfSuspended = false;
            parentSuspended = false;

            // Intentionally not equal to any valid state in order to call
            // disabledInit() properly on first periodic
            prevInitializedState = null;
        }

        public abstract void init(RobotState state);
        public abstract void periodic(RobotState state);
        public abstract void remove();

        public abstract void suspend();
        public abstract void resume();

        public void doPeriodic(RobotState state) {
            // Don't do the periodic if this node is suspended
            if (isSuspended())
                return;

            // Initialize state if it changed
            // This is intentionally after the suspended check so state
            // initialization will wait until the node is resumed
            if (prevInitializedState != state) {
                init(state);
                prevInitializedState = state;
            }

            periodic(state);
        }

        public boolean hasParent() {
            return parent != null;
        }

        public void setParent(SubsystemNode parent) {
            this.parent = parent;
            this.parentSuspended = parent == null ? false : parent.isSuspended();
        }

        protected void updateSuspend(boolean self, boolean parent) {
            boolean prev = isSuspended();
            selfSuspended = self;
            parentSuspended = parent;
            boolean sus = isSuspended();
            if (!prev && sus)
                suspend();
            if (prev && !sus)
                resume();
        }

        public boolean isSuspended() {
            return selfSuspended || parentSuspended;
        }

        public void setSelfSuspended(boolean suspended) {
            updateSuspend(suspended, parentSuspended);
        }

        public void setParentSuspended(boolean suspended) {
            updateSuspend(selfSuspended, suspended);
        }

        public SubsystemNode getParent() {
            return parent;
        }
    }

    private static final class CommandNode extends Node {
        private final Command cmd;
        private boolean initialized, finished;

        public CommandNode(Command cmd) {
            this.cmd = cmd;
            initialized = false;
            finished = false;
        }

        @Override
        public void init(RobotState state) {}

        @Override
        public void periodic(RobotState state) {
            // TODO: Do the interval properly

            Profiler.push(cmd.getClass().getSimpleName());
            if (!initialized) {
                cmd.init();
                initialized = true;
            }

            finished = cmd.run();
            Profiler.pop();

            if (finished) {
                cmd.end(false);
                INSTANCE.removeCommand(cmd);
            }
        }

        @Override
        public void remove() {
            if (initialized && !finished) {
                cmd.end(true);
            }
        }

        @Override
        public void suspend() {
            cmd.suspend();
        }

        @Override
        public void resume() {
            cmd.resume();
        }
    }

    private static final class SubsystemNode extends Node {
        private final Subsystem ss;
        private final List<Node> children;

        public SubsystemNode(Subsystem ss) {
            this.ss = ss;
            children = new ArrayList<>();

            ss.onAdd();
        }

        public void addChild(Node child) {
            children.add(child);
            child.setParent(this);
        }

        public void removeChild(Node child) {
            children.remove(child);
        }

        @Override
        public void init(RobotState state) {
            switch (state) {
                case DISABLED:   ss.disabledInit();   break;
                case AUTONOMOUS: ss.autonomousInit(); break;
                case TELEOP:     ss.teleopInit();     break;
                case TEST:       ss.testInit();       break;
            }

            for (Node child : new ArrayList<>(children)) {
                child.init(state);
            }
        }

        @Override
        public void periodic(RobotState state) {
            Profiler.push(ss.getClass().getSimpleName());
            ss.periodic();
            switch (state) {
                case DISABLED:   ss.disabledPeriodic();   break;
                case AUTONOMOUS: ss.autonomousPeriodic(); break;
                case TELEOP:     ss.teleopPeriodic();     break;
                case TEST:       ss.testPeriodic();       break;
            }

            for (Node child : new ArrayList<>(children)) {
                child.periodic(state);
            }
            Profiler.pop();
        }

        @Override
        public void remove() {
            ss.onRemove();
        }

        @Override
        public void suspend() {
            ss.suspend();
        }

        @Override
        public void resume() {
            ss.resume();
        }

        @Override
        protected void updateSuspend(boolean self, boolean parent) {
            super.updateSuspend(self, parent);
            boolean sus = isSuspended();
            for (Node child : children) {
                child.setParentSuspended(sus);
            }
        }
    }

    private final Map<Command, CommandNode> commands;
    private final Map<Subsystem, SubsystemNode> subsystems;
    private final List<CommandNode> rootCommands;
    private final List<SubsystemNode> rootSubsystems;
    private final Map<Subsystem, Set<Node>> incompleteLinks;

    private Scheduler() {
        commands = new IdentityHashMap<>();
        subsystems = new IdentityHashMap<>();
        rootCommands = new ArrayList<>();
        rootSubsystems = new ArrayList<>();
        incompleteLinks = new IdentityHashMap<>();
    }

    /**
     * Registers a {@code Command} to be executed until it ends or
     * is cancelled using {@link #removeCommand}.
     *
     * @param cmd Command to schedule
     * @throws IllegalStateException if the command is already scheduled
     */
    public void addCommand(Command cmd) {
        addCommand(null, cmd);
    }

    /**
     * Registers a {@code Command} to be executed within a parent
     * {@code Subsystem}. The execution of the command will be
     * suspended if the parent is suspended, and the command will
     * be cancelled if the parent is removed.
     *
     * This method will print an error and have no effect if the
     * command is already scheduled.
     *
     * @param parent parent Subsystem
     * @param cmd Command to schedule
     */
    public void addCommand(Subsystem parent, Command cmd) {
        if (cmd == null) {
            DriverStation.reportError("Cannot schedule null command", true);
            return;
        }

        if (commands.containsKey(cmd)) {
            DriverStation.reportError("Command already scheduled: " + cmd, true);
            return;
        }

        CommandNode node = new CommandNode(cmd);
        commands.put(cmd, node);

        if (parent == null)
            rootCommands.add(node);
        else
            linkNodes(parent, node);
    }

    /**
     * Registers a {@code Subsystem} to be executed every periodic.
     *
     * @param ss Subsystem to schedule
     * @throws IllegalStateException if the subsystem is already scheduled
     */
    public void addSubsystem(Subsystem ss) {
        addSubsystem(null, ss);
    }

    /**
     * Registers a {@code Subsystem} to be executed every periodic
     * within a parent {@code Subsystem}. The execution of the
     * subsystem will be suspended if the parent is suspended, and
     * will be removed if the parent is removed.
     *
     * This method will print an error and have no effect if the
     * subsystem is already scheduled.
     *
     * Do not create a loop of subsystems, as none of them will be
     * executed if you do.
     * Example of what NOT to do:
     *
     * {@code
     * Subsystem A = ..., B = ...;
     * Scheduler.get().addSubsystem(A, B);
     * Scheduler.get().addSubsystem(B, A);
     * }
     *
     * @param parent parent Subsystem
     * @param ss Subsystem to schedule
     */
    public void addSubsystem(Subsystem parent, Subsystem ss) {
        if (ss == null) {
            DriverStation.reportError("Cannot schedule null subsystem", true);
            return;
        }

        if (subsystems.containsKey(ss)) {
            DriverStation.reportError("Subsystem already scheduled: " + ss, true);
            return;
        }

        SubsystemNode node = new SubsystemNode(ss);
        subsystems.put(ss, node);

        if (parent == null)
            rootSubsystems.add(node);
        else
            linkNodes(parent, node);

        Set<Node> incompleteChildren = incompleteLinks.remove(ss);
        if (incompleteChildren == null)
            return;

        for (Node child : incompleteChildren) {
            node.addChild(child);
        }
    }

    /**
     * Unregisters a {@code Command}, causing it to be cancelled if
     * it is currently running. If the command is running, its
     * execution will be immediately stopped, and the
     * {@link Command#end(boolean)} method will be called.
     *
     * This method will print a warning and have no effect if the
     * command is not currently scheduled.
     *
     * @param cmd Command to cancel
     */
    public void removeCommand(Command cmd) {
        CommandNode node = commands.remove(cmd);
        if (node == null) {
            DriverStation.reportWarning("Cannot remove unscheduled command", true);
            return;
        }

        SubsystemNode parent = node.getParent();
        if (parent != null)
            parent.removeChild(node);
        node.setParent(null);

        node.remove();
        rootCommands.remove(node);

        invalidateIncompleteLinks(node);
    }

    /**
     * Unregisters a {@code Subsystem}, causing its execution to
     * immediately stop. The {@link Subsystem#onRemove()} method
     * will be called, and any registered child {@code Subsystem}s
     * and {@code Command} will also be removed.
     *
     * This method will print a warning and have no effect if the
     * subsystem is not currently scheduled.
     *
     * @param ss Subsystem to remove
     */
    public void removeSubsystem(Subsystem ss) {
        SubsystemNode node = subsystems.remove(ss);
        if (node == null) {
            DriverStation.reportWarning("Cannot remove unscheduled subsystem", true);
            return;
        }

        SubsystemNode parent = node.getParent();
        if (parent != null)
            parent.removeChild(node);
        node.setParent(null);

        node.remove();
        rootSubsystems.remove(node);

        invalidateIncompleteLinks(node);
    }

    /**
     * Sets whether a {@code Command} is currently suspended. If a
     * command is suspended, it is still scheduled, but its
     * {@link Command#run()} method will not be called.
     *
     * This method will print a warning and have no effect if the
     * command is not currently scheduled.
     *
     * @param cmd Command to set suspended state
     * @param suspended whether the command should be suspended
     */
    public void setCommandSuspended(Command cmd, boolean suspended) {
        CommandNode node = commands.get(cmd);
        if (node == null) {
            DriverStation.reportWarning("Cannot set suspended state of unscheduled command: " + cmd, true);
            return;
        }

        node.setSelfSuspended(suspended);
    }

    /**
     * Sets whether a {@code Subsystem} is currently suspended. If a
     * subsystem is suspended, it is still scheduled, but its init and
     * periodic methods are not called. If the robot state is changed
     * while the subsystem is suspended, the subsystem's init method for
     * the new state will be called when it is resumed. When a subsystem
     * is suspended, its child {@code Command}s and {@code Subsystem}s
     * will also be suspended.
     *
     * This method will print a warning and have no effect if the
     * subsystem is not currently scheduled.
     *
     * @param ss Subsystem to set suspended state
     * @param suspended whether the subsystem should be suspended
     */
    public void setSubsystemSuspended(Subsystem ss, boolean suspended) {
        SubsystemNode node = subsystems.get(ss);
        if (node == null) {
            DriverStation.reportWarning("Cannot set suspended state of unscheduled subsystem: " + ss, true);
            return;
        }

        node.setSelfSuspended(suspended);
    }

    /**
     * Gets whether a {@code Command} is currently known and registered. 
     * This will return {@code true} even if the command or its parents 
     * are suspended, or if the command's parent is not scheduled.
     *
     * @param cmd Command to check
     * @return whether the command is registered and
     */
    public boolean isCommandScheduled(Command cmd) {
        return commands.containsKey(cmd);
    }

    /**
     * Gets whether a {@code Command} is registered and is currently
     * running. This will only return {@code true} if the command and
     * all of its parents are properly scheduled and not suspended.
     *
     * @param cmd Command to check
     * @return whether the command is running
     */
    public boolean isCommandRunning(Command cmd) {
        if (cmd == null)
            return false;

        CommandNode node = commands.get(cmd);
        if (node == null)
            return false;

        return checkIfValidLink(node) && !node.isSuspended();
    }

    // Checks whether a node is fully linked to the root
    private boolean checkIfValidLink(Node node) {
        if ((node instanceof CommandNode && rootCommands.contains(node)) ||
            (node instanceof SubsystemNode && rootSubsystems.contains(node)))
            return true;

        if (!node.hasParent())
            return false;

        return checkIfValidLink(node.getParent());
    }

    // Link parent and child nodes
    // Will immediately link if the parent is scheduled, otherwise
    // it will be linked once the parent is added
    private void linkNodes(Subsystem parent, Node child) {
        SubsystemNode parentNode = subsystems.get(parent);
        if (parentNode != null) {
            parentNode.addChild(child);
        } else {
            incompleteLinks
                    .computeIfAbsent(parent, (k) -> new HashSet<>())
                    .add(child);
        }
    }

    // Removes a stored incomplete link to a child when it is
    private void invalidateIncompleteLinks(Node child) {
        // Iterator here so we can remove without concurrent modification
        for (Iterator<Set<Node>> iter = incompleteLinks.values().iterator(); iter.hasNext();) {
            Set<Node> next = iter.next();
            next.remove(child);
            if (next.isEmpty())
                iter.remove();
        }
    }

    /**
     * Called by AbstractRobot every periodic. This should
     * typically not by called by your robot code.
     *
     * @param state current robot state
     */
    public void periodicState(RobotState state) {
        for (CommandNode cmd : new ArrayList<>(rootCommands)) {
            cmd.doPeriodic(state);
        }

        for (SubsystemNode ss : new ArrayList<>(rootSubsystems)) {
            ss.doPeriodic(state);
        }

        if (!incompleteLinks.isEmpty()) {
            DriverStation.reportWarning("Incomplete parent links exist after periodic, did you forget to schedule a parent?", false);
        }
    }
}

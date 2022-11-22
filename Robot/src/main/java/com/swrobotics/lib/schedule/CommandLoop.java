package com.swrobotics.lib.schedule;

import java.util.Collections;

import com.swrobotics.lib.schedule.debug.CommandDebugDesc;
import com.swrobotics.lib.schedule.debug.CompoundCommandDebugCallback;

/**
 * Repeats a command a specified number of times.
 */
public final class CommandLoop implements CompoundCommand {
    private final Command cmd;
    private int repeats;
    
    /**
     * Creates a new loop that repeats a specified number of times.
     * 
     * @param cmd Command to repeat
     * @param repeats number of times to repeat the command
     */
    public CommandLoop(Command cmd, int repeats) {
        this.cmd = cmd;
        this.repeats = repeats;
    }

    @Override
    public void init() {
        cmd.init();
    }

    @Override
    public boolean run() {
        if (!cmd.run())
            return false;

        cmd.end(false);
        repeats--;

        if (repeats > 0) {
            cmd.init();
            return false;
        }

        return true;
    }

    @Override
    public void end(boolean cancelled) {
        if (repeats > 0) {
            cmd.end(true);
        }
    }

    @Override
    public void setDebugCallback(CompoundCommandDebugCallback cb) {
        cb.onChildrenInfoChanged(Collections.singletonList(new CommandDebugDesc(cmd, true)));
    }
}

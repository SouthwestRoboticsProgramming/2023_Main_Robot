package com.swrobotics.lib.schedule;

import com.swrobotics.lib.schedule.debug.CompoundCommandDebugCallback;

/**
 * Represents a command that can contain other commands.
 * Most commands should not implement this and instead implement
 * {@link Command}.
 */
public interface CompoundCommand extends Command {
    /**
     * Sets the debug callback for scheduler info in ShuffleLog.
     * 
     * @param debugCallback
     */
    void setDebugCallback(CompoundCommandDebugCallback debugCallback);
}

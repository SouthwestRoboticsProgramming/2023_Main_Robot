package com.swrobotics.shufflelog.tool;

public final class ToolConstants {
    // Normal query that stops once it gets a response
    public static final long MSG_QUERY_COOLDOWN_TIME = 500_000_000L;

    // Query that will be repeated constantly, slower to reduce bandwidth usage
    public static final long MSG_CONSTANT_QUERY_COOLDOWN_TIME = 4_000_000_000L;

    private ToolConstants() {
        throw new AssertionError();
    }
}

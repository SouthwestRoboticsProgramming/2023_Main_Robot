package com.swrobotics.lib;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class ThreadUtils {
    private static final Queue<Runnable> mainThreadQueue = new ConcurrentLinkedQueue<>();

    public static void runOnMainThread(Runnable r) {
        mainThreadQueue.add(r);
    }

    public static void runMainThreadOperations() {
        Runnable r;
        while ((r = mainThreadQueue.poll()) != null) {
            r.run();
        }
    }
}

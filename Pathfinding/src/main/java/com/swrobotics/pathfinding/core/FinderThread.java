package com.swrobotics.pathfinding.core;

import com.swrobotics.pathfinding.core.finder.Pathfinder;

import java.util.List;

public abstract class FinderThread<P> extends Thread {
    private static final class Endpoints<P> {
        private final P start, end;

        public Endpoints(P start, P end) {
            this.start = start;
            this.end = end;
        }
    }

    private final Pathfinder<P> finder;
    private volatile Endpoints<P> endpoints;
    private final Object lock;

    public FinderThread(String name, Pathfinder<P> finder) {
        super(name);
        this.finder = finder;
        lock = new Object();
    }

    public void setEndpoints(P start, P end) {
        endpoints = new Endpoints<>(start, end);
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    protected abstract void reportResult(List<P> path);

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            // Wait to be woken up when endpoints change
            try {
                synchronized (lock) {
                    lock.wait();
                }
            } catch (InterruptedException e) {
                break;
            }

            if (endpoints == null) continue;

            Endpoints<P> endpoints;
            do {
                endpoints = this.endpoints;

                finder.setStart(endpoints.start);
                finder.setGoal(endpoints.end);
                reportResult(finder.findPath());

                // Repeat if changed, since endpoints may have changed while solving
            } while (endpoints != this.endpoints);
        }
    }
}

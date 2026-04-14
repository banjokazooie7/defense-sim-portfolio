package io.github.banjokazooie7.simcore.engine;

import io.github.banjokazooie7.simcore.SimClock;
import io.github.banjokazooie7.simcore.event.EventListener;
import io.github.banjokazooie7.simcore.event.SimEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public final class DiscreteEventEngine {

    private final PriorityQueue<SimEvent> queue;
    private final List<EventListener> listeners;
    private final SimClock clock;
    private final long endTimeMillis;
    private long eventsProcessed;
    private long eventsCancelled;
    private boolean running;

    public DiscreteEventEngine() { this(Long.MAX_VALUE); }

    public DiscreteEventEngine(long endTimeMillis) {
        if (endTimeMillis <= 0) throw new IllegalArgumentException("endTimeMillis must be > 0");
        this.queue = new PriorityQueue<>();
        this.listeners = new ArrayList<>();
        this.clock = new SimClock(0);
        this.endTimeMillis = endTimeMillis;
        this.eventsProcessed = 0;
        this.eventsCancelled = 0;
        this.running = false;
    }

    public void schedule(SimEvent event) {
        if (event.getTimeMillis() < clock.now())
            throw new IllegalArgumentException("Cannot schedule in the past");
        queue.add(event);
    }

    public void schedule(long timeMillis, String type, Runnable action) {
        schedule(new SimEvent(timeMillis, 0, type, action));
    }

    public void addListener(EventListener listener) {
        if (listener == null) throw new IllegalArgumentException("listener must not be null");
        listeners.add(listener);
    }

    public List<EventListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    public RunStats run() {
        running = true;
        long wallStartNs = System.nanoTime();
        while (running && !queue.isEmpty()) {
            SimEvent next = queue.peek();
            if (next.getTimeMillis() > endTimeMillis) break;
            queue.poll();
            if (next.isCancelled()) { eventsCancelled++; continue; }
            long delta = next.getTimeMillis() - clock.now();
            if (delta > 0) clock.advance(delta);
            next.execute();
            eventsProcessed++;
            for (EventListener listener : listeners) listener.onEvent(next, clock.now());
        }
        running = false;
        long wallElapsedNs = System.nanoTime() - wallStartNs;
        return new RunStats(eventsProcessed, eventsCancelled, clock.now(), wallElapsedNs, queue.size());
    }

    public void stop() { this.running = false; }

    public void reset() {
        queue.clear();
        clock.reset();
        eventsProcessed = 0;
        eventsCancelled = 0;
        running = false;
    }

    public SimClock getClock()       { return clock; }
    public long getEventsProcessed() { return eventsProcessed; }
    public long getEventsCancelled() { return eventsCancelled; }
    public int getQueueSize()        { return queue.size(); }
    public boolean isRunning()       { return running; }

    public record RunStats(long eventsProcessed, long eventsCancelled,
                           long finalSimTimeMillis, long wallClockNanos,
                           int eventsRemaining) {
        public double wallClockMs() { return wallClockNanos / 1_000_000.0; }
        @Override public String toString() {
            return String.format("RunStats{processed=%d, cancelled=%d, simTime=%dms, wall=%.2fms, remaining=%d}",
                    eventsProcessed, eventsCancelled, finalSimTimeMillis, wallClockMs(), eventsRemaining);
        }
    }
}

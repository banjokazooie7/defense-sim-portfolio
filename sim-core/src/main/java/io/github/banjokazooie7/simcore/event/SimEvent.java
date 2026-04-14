package io.github.banjokazooie7.simcore.event;

public final class SimEvent implements Comparable<SimEvent> {

    private final long timeMillis;
    private final int priority;
    private final String type;
    private final Runnable action;
    private boolean cancelled;

    public SimEvent(long timeMillis, int priority, String type, Runnable action) {
        if (timeMillis < 0) throw new IllegalArgumentException("Event time must be >= 0, got: " + timeMillis);
        if (type == null || type.isBlank()) throw new IllegalArgumentException("Event type must not be null or blank");
        if (action == null) throw new IllegalArgumentException("Event action must not be null");
        this.timeMillis = timeMillis;
        this.priority = priority;
        this.type = type;
        this.action = action;
        this.cancelled = false;
    }

    public long getTimeMillis()  { return timeMillis; }
    public int getPriority()     { return priority; }
    public String getType()      { return type; }
    public boolean isCancelled() { return cancelled; }
    public void cancel()         { this.cancelled = true; }
    public void execute()        { action.run(); }

    @Override
    public int compareTo(SimEvent other) {
        int cmp = Long.compare(this.timeMillis, other.timeMillis);
        return (cmp != 0) ? cmp : Integer.compare(this.priority, other.priority);
    }

    @Override
    public String toString() {
        return String.format("SimEvent{t=%dms, pri=%d, type=%s, cancelled=%b}",
                timeMillis, priority, type, cancelled);
    }
}

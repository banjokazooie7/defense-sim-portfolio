package io.github.banjokazooie7.simcore;

public final class SimClock {

    private long nowMillis;

    public SimClock(long startMillis) {
        if (startMillis < 0) {
            throw new IllegalArgumentException("startMillis must be >= 0, got: " + startMillis);
        }
        this.nowMillis = startMillis;
    }

    public long now() {
        return nowMillis;
    }

    public void advance(long deltaMillis) {
        if (deltaMillis < 0) {
            throw new IllegalArgumentException("deltaMillis must be >= 0, got: " + deltaMillis);
        }
        nowMillis += deltaMillis;
    }

    public void reset() {
        this.nowMillis = 0;
    }

    @Override
    public String toString() {
        return "SimClock{nowMillis=" + nowMillis + "}";
    }
}

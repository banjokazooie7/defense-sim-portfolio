package io.github.banjokazooie7.simcore.metrics;

import io.github.banjokazooie7.simcore.event.EventListener;
import io.github.banjokazooie7.simcore.event.SimEvent;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MetricsCollector implements EventListener {

    private final Map<String, Long> countsByType;
    private long totalEvents;
    private long firstEventTimeMillis;
    private long lastEventTimeMillis;
    private boolean receivedAny;

    public MetricsCollector() {
        this.countsByType = new LinkedHashMap<>();
        this.totalEvents = 0;
        this.firstEventTimeMillis = 0;
        this.lastEventTimeMillis = 0;
        this.receivedAny = false;
    }

    @Override
    public void onEvent(SimEvent event, long simTime) {
        totalEvents++;
        countsByType.merge(event.getType(), 1L, Long::sum);

        if (!receivedAny) {
            firstEventTimeMillis = simTime;
            receivedAny = true;
        }
        lastEventTimeMillis = simTime;
    }

    public long getTotalEvents() {
        return totalEvents;
    }

    public long getCount(String eventType) {
        return countsByType.getOrDefault(eventType, 0L);
    }

    public Map<String, Long> getCountsByType() {
        return Collections.unmodifiableMap(countsByType);
    }

    public long getFirstEventTimeMillis() {
        return firstEventTimeMillis;
    }

    public long getLastEventTimeMillis() {
        return lastEventTimeMillis;
    }

    public long getSimDurationMillis() {
        return lastEventTimeMillis - firstEventTimeMillis;
    }

    public int getDistinctTypeCount() {
        return countsByType.size();
    }

    public void reset() {
        countsByType.clear();
        totalEvents = 0;
        firstEventTimeMillis = 0;
        lastEventTimeMillis = 0;
        receivedAny = false;
    }

    public String toSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Metrics Summary ===\n");
        sb.append(String.format("Total events:    %d%n", totalEvents));
        sb.append(String.format("Distinct types:  %d%n", countsByType.size()));
        sb.append(String.format("Sim duration:    %d ms%n", getSimDurationMillis()));
        sb.append("Counts by type:\n");
        countsByType.forEach((type, count) ->
                sb.append(String.format("  %-20s %d%n", type, count)));
        return sb.toString();
    }
}

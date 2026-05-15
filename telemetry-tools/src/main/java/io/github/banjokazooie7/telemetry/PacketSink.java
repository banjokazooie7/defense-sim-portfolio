package io.github.banjokazooie7.telemetry;

/**
 * A consumer of telemetry packets in the receive pipeline.
 *
 * Implementations observe each packet as it arrives — to record stats,
 * write logs, trigger alerts, etc. Mirrors the EventListener pattern
 * from sim-core: one event, many independent observers.
 *
 * Sinks should be fast and non-blocking. Heavy work (disk I/O, network
 * calls) belongs in a buffered flush, not in onPacket().
 */
@FunctionalInterface
public interface PacketSink {
    void onPacket(TelemetryPacket packet);
}

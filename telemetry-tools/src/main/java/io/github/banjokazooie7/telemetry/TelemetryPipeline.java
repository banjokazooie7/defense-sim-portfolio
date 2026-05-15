package io.github.banjokazooie7.telemetry;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Coordinates the telemetry receive side.
 *
 * Holds a TelemetryListener and a list of PacketSinks. Each call to
 * pump() drains the listener and delivers every packet to every sink
 * in registration order. Exceptions from a sink are caught so one
 * misbehaving sink can't starve the others — this matters because
 * sinks are independent (a log writer's disk error shouldn't break
 * the drop detector's stats).
 *
 * Same fan-out pattern as sim-core's event dispatch, scoped to
 * network packets instead of sim events.
 */
public final class TelemetryPipeline implements Closeable {

    private final TelemetryListener listener;
    private final List<PacketSink> sinks;

    public TelemetryPipeline(TelemetryListener listener) {
        this.listener = Objects.requireNonNull(listener, "listener");
        this.sinks = new ArrayList<>();
    }

    /** Register a sink. Sinks receive every packet in registration order. */
    public TelemetryPipeline addSink(PacketSink sink) {
        sinks.add(Objects.requireNonNull(sink, "sink"));
        return this;
    }

    /**
     * Drain the listener and dispatch all received packets to every sink.
     * Returns the number of packets dispatched.
     */
    public int pump() {
        List<TelemetryPacket> batch = listener.receiveAll();
        for (TelemetryPacket pkt : batch) {
            dispatch(pkt);
        }
        return batch.size();
    }

    private void dispatch(TelemetryPacket packet) {
        for (PacketSink sink : sinks) {
            try {
                sink.onPacket(packet);
            } catch (RuntimeException ex) {
                // A failing sink shouldn't stop the others. Log to stderr
                // for now; a production version would route to a proper
                // error handler.
                System.err.println("Sink " + sink.getClass().getSimpleName()
                        + " threw: " + ex.getMessage());
            }
        }
    }

    public int getSinkCount() {
        return sinks.size();
    }

    @Override
    public void close() {
        listener.close();
    }
}

package io.github.banjokazooie7.telemetry.examples;

import io.github.banjokazooie7.simcore.engine.DiscreteEventEngine;
import io.github.banjokazooie7.simcore.entity.Position2D;
import io.github.banjokazooie7.simcore.entity.Target;
import io.github.banjokazooie7.simcore.event.SimEvent;
import io.github.banjokazooie7.telemetry.TelemetryListener;
import io.github.banjokazooie7.telemetry.TelemetryPacket;
import io.github.banjokazooie7.telemetry.TelemetrySender;

import java.util.List;

public final class Week4Demo {

    private static final int PORT = 19_900;
    private static final long SIM_DURATION_MS = 5_000;
    private static final long UPDATE_INTERVAL = 1_000;

    public static void main(String[] args) {
        System.out.println("=== Week 4 Demo: Telemetry over UDP ===");
        System.out.println();

        // Start listener in a background thread
        TelemetryListener listener = new TelemetryListener(PORT, 3000);
        Thread listenerThread = new Thread(() -> {
            List<TelemetryPacket> received = listener.receiveAll();
            System.out.println();
            System.out.println("--- Listener received " + received.size() + " packets ---");
            for (TelemetryPacket pkt : received) {
                System.out.println("  " + pkt);
            }
            listener.close();
        });
        listenerThread.start();

        // Small delay to let listener bind
        try { Thread.sleep(100); } catch (InterruptedException e) { /* ignore */ }

        // Set up sim
        DiscreteEventEngine engine = new DiscreteEventEngine(SIM_DURATION_MS);
        Target target = new Target("TGT-A", new Position2D(0, 0), 200.0, 150.0);
        TelemetrySender sender = new TelemetrySender("localhost", PORT);

        System.out.println("Target: " + target);
        System.out.println("Sending telemetry to localhost:" + PORT);
        System.out.println();

        // Schedule target updates that also send telemetry
        for (long t = UPDATE_INTERVAL; t <= SIM_DURATION_MS; t += UPDATE_INTERVAL) {
            final long time = t;
            engine.schedule(new SimEvent(t, 0, "TARGET_UPDATE", () -> {
                target.update(UPDATE_INTERVAL);
                TelemetryPacket pkt = new TelemetryPacket(
                        time, target.getId(),
                        target.getPosition().xMeters(), target.getPosition().yMeters(),
                        target.getVx(), target.getVy(), "TARGET_UPDATE");
                sender.send(pkt);
                System.out.printf("  [t=%dms] Sent: %s at %s%n",
                        time, target.getId(), target.getPosition());
            }));
        }

        // Run sim
        System.out.println("--- Simulation running ---");
        DiscreteEventEngine.RunStats stats = engine.run();
        System.out.println("--- Simulation complete ---");
        System.out.println("Packets sent: " + sender.getPacketsSent());

        sender.close();

        // Wait for listener thread to finish
        try { listenerThread.join(5000); } catch (InterruptedException e) { /* ignore */ }

        System.out.println();
        System.out.println(stats);
    }
}


package io.github.banjokazooie7.telemetry.examples;

import io.github.banjokazooie7.simcore.engine.DiscreteEventEngine;
import io.github.banjokazooie7.simcore.entity.Position2D;
import io.github.banjokazooie7.simcore.entity.Target;
import io.github.banjokazooie7.simcore.event.SimEvent;
import io.github.banjokazooie7.telemetry.DropDetector;
import io.github.banjokazooie7.telemetry.TelemetryListener;
import io.github.banjokazooie7.telemetry.TelemetryLogger;
import io.github.banjokazooie7.telemetry.TelemetryPacket;
import io.github.banjokazooie7.telemetry.TelemetryPipeline;
import io.github.banjokazooie7.telemetry.TelemetrySender;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Same as Week4Demo, but with simulated packet loss.
 *
 * Every DROP_EVERY_NTH packet is built and assigned a sequence number,
 * then deliberately NOT sent — simulating what real UDP packet loss looks
 * like from the receiver's perspective. The DropDetector should catch
 * the resulting sequence-number gaps and report a non-zero drop rate.
 *
 * This demonstrates that the audit layer works on something other than
 * a perfect localhost connection.
 *
 * Run with:
 *   ./gradlew :telemetry-tools:runLossy
 */
public final class Week4DemoLossy {

    private static final int PORT = 19_901;
    private static final long SIM_DURATION_MS = 10_000;
    private static final long UPDATE_INTERVAL = 500;   // 20 packets total
    private static final int DROP_EVERY_NTH = 3;       // drop seq 2, 5, 8, ...
    private static final Path CSV_OUT = Paths.get("build", "telemetry-out", "week4-demo-lossy.csv");

    public static void main(String[] args) {
        System.out.println("=== Week 4 Demo: Telemetry with Simulated Packet Loss ===");
        System.out.println("Dropping every " + DROP_EVERY_NTH + "rd packet to test drop detection.");
        System.out.println();

        DropDetector detector = new DropDetector();
        TelemetryLogger logger = new TelemetryLogger();

        TelemetryListener listener = new TelemetryListener(PORT, 3000);
        TelemetryPipeline pipeline = new TelemetryPipeline(listener)
                .addSink(detector)
                .addSink(logger);

        Thread listenerThread = new Thread(() -> {
            int dispatched = pipeline.pump();
            System.out.println();
            System.out.println("--- Pipeline dispatched " + dispatched + " packets ---");
        });
        listenerThread.start();

        try { Thread.sleep(100); } catch (InterruptedException e) { /* ignore */ }

        DiscreteEventEngine engine = new DiscreteEventEngine(SIM_DURATION_MS);
        Target target = new Target("TGT-A", new Position2D(0, 0), 200.0, 150.0);
        TelemetrySender sender = new TelemetrySender("localhost", PORT);

        System.out.println("Target: " + target);
        System.out.println("Sending telemetry to localhost:" + PORT);
        System.out.println();
        System.out.println("--- Simulation running ---");

        final int[] seq = {0};
        final int[] skipped = {0};
        for (long t = UPDATE_INTERVAL; t <= SIM_DURATION_MS; t += UPDATE_INTERVAL) {
            final long time = t;
            engine.schedule(new SimEvent(t, 0, "TARGET_UPDATE", () -> {
                target.update(UPDATE_INTERVAL);
                int thisSeq = seq[0]++;
                TelemetryPacket pkt = new TelemetryPacket(
                        thisSeq, time, target.getId(),
                        target.getPosition().xMeters(), target.getPosition().yMeters(),
                        target.getVx(), target.getVy(), "TARGET_UPDATE");

                // Simulate a dropped packet: build it, assign sequence number,
                // then deliberately fail to send. From the receiver's view this
                // is indistinguishable from real UDP loss.
                if (thisSeq > 0 && thisSeq % DROP_EVERY_NTH == 0) {
                    skipped[0]++;
                    System.out.printf("  [t=%dms] DROP   seq=%d (simulated loss)%n",
                            time, thisSeq);
                    return;
                }
                sender.send(pkt);
                System.out.printf("  [t=%dms] Sent   seq=%d at %s%n",
                        time, thisSeq, target.getPosition());
            }));
        }

        DiscreteEventEngine.RunStats stats = engine.run();
        System.out.println("--- Simulation complete ---");
        System.out.println("Packets sent:    " + sender.getPacketsSent());
        System.out.println("Packets dropped: " + skipped[0] + " (simulated at sender)");
        sender.close();

        try { listenerThread.join(5000); } catch (InterruptedException e) { /* ignore */ }
        pipeline.close();

        logger.flush(CSV_OUT);

        System.out.println();
        System.out.println("--- Telemetry Audit ---");
        System.out.println(detector.toSummary());
        System.out.println("CSV written to: " + CSV_OUT.toAbsolutePath());
        System.out.println();
        System.out.println(stats);

        // Sanity check: the detector should have caught exactly the packets we skipped.
        if (detector.getTotalDropped() == skipped[0]) {
            System.out.println("\nAudit matches expected loss. Drop detection working correctly.");
        } else {
            System.out.printf("%nMISMATCH: skipped %d at sender, detector reported %d dropped.%n",
                    skipped[0], detector.getTotalDropped());
        }
    }
}

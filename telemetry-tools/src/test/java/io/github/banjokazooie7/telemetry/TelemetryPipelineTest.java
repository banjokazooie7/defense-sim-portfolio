package io.github.banjokazooie7.telemetry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.DatagramSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TelemetryPipelineTest {

    /** Grab a free UDP port from the OS so tests don't collide. */
    private static int findFreePort() throws IOException {
        try (DatagramSocket probe = new DatagramSocket(0)) {
            return probe.getLocalPort();
        }
    }

    private TelemetryPacket makePacket(int seq, String entityId, String eventType) {
        return new TelemetryPacket(
                seq,
                seq * 100L,
                entityId,
                10.0 + seq,
                20.0 + seq,
                1.5,
                -2.5,
                eventType);
    }

    @Test
    void packetsReachBothSinks() throws Exception {
        int port = findFreePort();
        DropDetector detector = new DropDetector();
        TelemetryLogger logger = new TelemetryLogger();

        try (TelemetryListener listener = new TelemetryListener(port, 200);
             TelemetryPipeline pipeline = new TelemetryPipeline(listener);
             TelemetrySender sender = new TelemetrySender("127.0.0.1", port)) {

            pipeline.addSink(detector).addSink(logger);

            sender.send(makePacket(0, "drone-1", "POSITION"));
            sender.send(makePacket(1, "drone-1", "POSITION"));
            sender.send(makePacket(2, "drone-2", "CONTACT"));

            int dispatched = pipeline.pump();

            assertEquals(3, dispatched, "Pipeline should dispatch all 3 sent packets");
            assertEquals(3, detector.getTotalReceived(), "DropDetector should see all 3");
            assertEquals(0, detector.getTotalDropped(), "No gaps means no drops");
            assertEquals(3, logger.getRowCount(), "Logger should record all 3");
        }
    }

    @Test
    void detectsGapsAcrossPipeline() throws Exception {
        int port = findFreePort();
        DropDetector detector = new DropDetector();

        try (TelemetryListener listener = new TelemetryListener(port, 200);
             TelemetryPipeline pipeline = new TelemetryPipeline(listener);
             TelemetrySender sender = new TelemetrySender("127.0.0.1", port)) {

            pipeline.addSink(detector);

            // Send seq 0, skip 1 and 2, send 3 — simulates two dropped packets.
            sender.send(makePacket(0, "drone-1", "POSITION"));
            sender.send(makePacket(3, "drone-1", "POSITION"));

            pipeline.pump();

            assertEquals(2, detector.getTotalReceived());
            assertEquals(2, detector.getTotalDropped(),
                    "Sequences 1 and 2 should be marked dropped. Got: "
                            + detector.getDroppedSequences());
            assertTrue(detector.getDroppedSequences().containsAll(List.of(1, 2)));
        }
    }

    @Test
    void logToDiskAfterPipelineRun(@TempDir Path tmp) throws Exception {
        int port = findFreePort();
        TelemetryLogger logger = new TelemetryLogger();

        try (TelemetryListener listener = new TelemetryListener(port, 200);
             TelemetryPipeline pipeline = new TelemetryPipeline(listener);
             TelemetrySender sender = new TelemetrySender("127.0.0.1", port)) {

            pipeline.addSink(logger);
            sender.send(makePacket(0, "drone-1", "POSITION"));
            sender.send(makePacket(1, "drone-1", "CONTACT"));
            pipeline.pump();

            Path out = tmp.resolve("logs/run.csv");
            logger.flush(out);

            List<String> lines = Files.readAllLines(out);
            assertEquals(3, lines.size(), "Header + 2 packet rows");
            assertTrue(lines.get(1).contains("POSITION"));
            assertTrue(lines.get(2).contains("CONTACT"));
        }
    }

    @Test
    void failingSinkDoesNotStopOtherSinks() throws Exception {
        int port = findFreePort();
        DropDetector detector = new DropDetector();
        PacketSink bombSink = pkt -> { throw new RuntimeException("boom"); };

        try (TelemetryListener listener = new TelemetryListener(port, 200);
             TelemetryPipeline pipeline = new TelemetryPipeline(listener);
             TelemetrySender sender = new TelemetrySender("127.0.0.1", port)) {

            // Register the failing sink FIRST so we prove the second one
            // still gets called.
            pipeline.addSink(bombSink).addSink(detector);

            sender.send(makePacket(0, "drone-1", "POSITION"));
            sender.send(makePacket(1, "drone-1", "POSITION"));
            pipeline.pump();

            assertEquals(2, detector.getTotalReceived(),
                    "Detector should still receive packets even when an earlier sink throws");
        }
    }

    @Test
    void sinkCountTracksRegistrations() throws Exception {
        int port = findFreePort();
        try (TelemetryListener listener = new TelemetryListener(port, 100);
             TelemetryPipeline pipeline = new TelemetryPipeline(listener)) {

            assertEquals(0, pipeline.getSinkCount());
            pipeline.addSink(new DropDetector());
            pipeline.addSink(new TelemetryLogger());
            assertEquals(2, pipeline.getSinkCount());
        }
    }
}

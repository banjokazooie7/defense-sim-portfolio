package io.github.banjokazooie7.telemetry;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SenderListenerTest {

    private static final int PORT = 19_876;
    private static final double EPSILON = 1e-9;

    @Test
    void sendAndReceiveOnePacket() {
        TelemetryPacket original = new TelemetryPacket(
                1, 1000, "TGT-001", 500.0, 300.0, 100.0, 50.0, "TARGET_UPDATE");

        try (TelemetryListener listener = new TelemetryListener(PORT, 2000);
             TelemetrySender sender = new TelemetrySender("localhost", PORT)) {

            sender.send(original);
            TelemetryPacket received = listener.receiveOne();

            assertNotNull(received);
            assertEquals(original, received);
            assertEquals(1, sender.getPacketsSent());
            assertEquals(1, listener.getPacketsReceived());
        }
    }

    @Test
    void sendAndReceiveMultiplePackets() {
        try (TelemetryListener listener = new TelemetryListener(PORT + 1, 2000);
             TelemetrySender sender = new TelemetrySender("localhost", PORT + 1)) {

            for (int i = 0; i < 5; i++) {
                sender.send(new TelemetryPacket(
                        i, i * 1000, "TGT-" + i, i * 10.0, i * 20.0,
                        100.0, 0.0, "UPDATE"));
            }

            List<TelemetryPacket> received = listener.receiveAll();

            assertEquals(5, received.size());
            assertEquals(5, sender.getPacketsSent());
            assertEquals("TGT-0", received.get(0).entityId());
            assertEquals("TGT-4", received.get(4).entityId());
        }
    }

    @Test
    void receiveOneReturnsNullOnTimeout() {
        try (TelemetryListener listener = new TelemetryListener(PORT + 2, 500)) {
            TelemetryPacket result = listener.receiveOne();
            assertNull(result);
        }
    }

    @Test
    void receiveAllReturnsEmptyOnTimeout() {
        try (TelemetryListener listener = new TelemetryListener(PORT + 3, 500)) {
            List<TelemetryPacket> results = listener.receiveAll();
            assertTrue(results.isEmpty());
        }
    }

    @Test
    void packetDataSurvivesNetworkRoundTrip() {
        TelemetryPacket original = new TelemetryPacket(
                42, 9999, "RADAR-NORTH", -1234.5, 6789.0, -50.0, 200.0, "RADAR_SWEEP");

        try (TelemetryListener listener = new TelemetryListener(PORT + 4, 2000);
             TelemetrySender sender = new TelemetrySender("localhost", PORT + 4)) {

            sender.send(original);
            TelemetryPacket received = listener.receiveOne();

            assertNotNull(received);
            assertEquals(original, received);
        }
    }
}


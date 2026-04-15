package io.github.banjokazooie7.telemetry;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PacketCodecTest {

    private static final double EPSILON = 1e-9;

    @Test
    void encodeDecodeRoundTrip() {
        TelemetryPacket original = new TelemetryPacket(
                7, 5000, "TGT-001", 1500.0, 3000.0, 250.0, -50.0, "TARGET_UPDATE");

        byte[] bytes = PacketCodec.encode(original);
        TelemetryPacket decoded = PacketCodec.decode(bytes);

        assertEquals(original, decoded);
    }

    @Test
    void encodeDecodePreservesSequenceNumber() {
        TelemetryPacket original = new TelemetryPacket(
                999, 1000, "RADAR-N", 0.0, 0.0, 0.0, 0.0, "RADAR_SWEEP");

        byte[] bytes = PacketCodec.encode(original);
        TelemetryPacket decoded = PacketCodec.decode(bytes);

        assertEquals(999, decoded.sequenceNumber());
        assertEquals(original, decoded);
    }

    @Test
    void encodeDecodeWithZeroValues() {
        TelemetryPacket original = new TelemetryPacket(
                0, 0, "X", 0.0, 0.0, 0.0, 0.0, "START");

        byte[] bytes = PacketCodec.encode(original);
        TelemetryPacket decoded = PacketCodec.decode(bytes);

        assertEquals(original, decoded);
    }

    @Test
    void encodeDecodeWithNegativeValues() {
        TelemetryPacket original = new TelemetryPacket(
                3, 9999, "TGT-NEG", -500.0, -300.0, -100.0, -200.0, "MANEUVER");

        byte[] bytes = PacketCodec.encode(original);
        TelemetryPacket decoded = PacketCodec.decode(bytes);

        assertEquals(original, decoded);
    }

    @Test
    void encodeDecodeWithLongStrings() {
        TelemetryPacket original = new TelemetryPacket(
                1, 42, "ENTITY-WITH-LONG-ID-12345", 1.0, 2.0, 3.0, 4.0,
                "VERY_LONG_EVENT_TYPE_NAME");

        byte[] bytes = PacketCodec.encode(original);
        TelemetryPacket decoded = PacketCodec.decode(bytes);

        assertEquals(original, decoded);
    }

    @Test
    void encodeProducesBytesOfExpectedSize() {
        TelemetryPacket pkt = new TelemetryPacket(
                0, 100, "ABC", 0, 0, 0, 0, "XY");

        byte[] bytes = PacketCodec.encode(pkt);

        // 4 (int) + 8 (long) + 4*8 (doubles) + 2+3 (entityId) + 2+2 (eventType) = 53
        assertEquals(53, bytes.length);
    }

    @Test
    void differentPacketsProduceDifferentBytes() {
        TelemetryPacket a = new TelemetryPacket(1, 100, "A", 1, 2, 3, 4, "X");
        TelemetryPacket b = new TelemetryPacket(2, 200, "B", 5, 6, 7, 8, "Y");

        byte[] bytesA = PacketCodec.encode(a);
        byte[] bytesB = PacketCodec.encode(b);

        assertFalse(java.util.Arrays.equals(bytesA, bytesB));
    }
}

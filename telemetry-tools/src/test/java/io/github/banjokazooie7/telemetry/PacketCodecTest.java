package io.github.banjokazooie7.telemetry;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PacketCodecTest {

    private static final double EPSILON = 1e-9;

    @Test
    void encodeDecodeRoundTrip() {
        TelemetryPacket original = new TelemetryPacket(
                5000, "TGT-001", 1500.0, 3000.0, 250.0, -50.0, "TARGET_UPDATE");

        byte[] bytes = PacketCodec.encode(original);
        TelemetryPacket decoded = PacketCodec.decode(bytes);

        assertEquals(original.timestampMillis(), decoded.timestampMillis());
        assertEquals(original.entityId(), decoded.entityId());
        assertEquals(original.xMeters(), decoded.xMeters(), EPSILON);
        assertEquals(original.yMeters(), decoded.yMeters(), EPSILON);
        assertEquals(original.vxMetersPerSec(), decoded.vxMetersPerSec(), EPSILON);
        assertEquals(original.vyMetersPerSec(), decoded.vyMetersPerSec(), EPSILON);
        assertEquals(original.eventType(), decoded.eventType());
    }

    @Test
    void encodeDecodePreservesRecordEquality() {
        TelemetryPacket original = new TelemetryPacket(
                1000, "RADAR-N", 0.0, 0.0, 0.0, 0.0, "RADAR_SWEEP");

        byte[] bytes = PacketCodec.encode(original);
        TelemetryPacket decoded = PacketCodec.decode(bytes);

        assertEquals(original, decoded);
    }

    @Test
    void encodeDecodeWithZeroValues() {
        TelemetryPacket original = new TelemetryPacket(
                0, "X", 0.0, 0.0, 0.0, 0.0, "START");

        byte[] bytes = PacketCodec.encode(original);
        TelemetryPacket decoded = PacketCodec.decode(bytes);

        assertEquals(original, decoded);
    }

    @Test
    void encodeDecodeWithNegativeValues() {
        TelemetryPacket original = new TelemetryPacket(
                9999, "TGT-NEG", -500.0, -300.0, -100.0, -200.0, "MANEUVER");

        byte[] bytes = PacketCodec.encode(original);
        TelemetryPacket decoded = PacketCodec.decode(bytes);

        assertEquals(original, decoded);
    }

    @Test
    void encodeDecodeWithLongStrings() {
        TelemetryPacket original = new TelemetryPacket(
                42, "ENTITY-WITH-LONG-ID-12345", 1.0, 2.0, 3.0, 4.0,
                "VERY_LONG_EVENT_TYPE_NAME");

        byte[] bytes = PacketCodec.encode(original);
        TelemetryPacket decoded = PacketCodec.decode(bytes);

        assertEquals(original, decoded);
    }

    @Test
    void encodeProducesBytesOfExpectedSize() {
        TelemetryPacket pkt = new TelemetryPacket(
                100, "ABC", 0, 0, 0, 0, "XY");

        byte[] bytes = PacketCodec.encode(pkt);

        // 8 (long) + 4*8 (doubles) + 2+3 (entityId) + 2+2 (eventType) = 47
        assertEquals(49, bytes.length);
    }

    @Test
    void differentPacketsProduceDifferentBytes() {
        TelemetryPacket a = new TelemetryPacket(100, "A", 1, 2, 3, 4, "X");
        TelemetryPacket b = new TelemetryPacket(200, "B", 5, 6, 7, 8, "Y");

        byte[] bytesA = PacketCodec.encode(a);
        byte[] bytesB = PacketCodec.encode(b);

        assertNotEquals(bytesA.length == bytesB.length
                && java.util.Arrays.equals(bytesA, bytesB), true);
    }
}

package io.github.banjokazooie7.telemetry;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TelemetryPacketTest {

    private static final double EPSILON = 1e-9;

    @Test
    void constructsWithValidParams() {
        TelemetryPacket pkt = new TelemetryPacket(
                1000, "TGT-001", 500.0, 300.0, 100.0, 50.0, "TARGET_UPDATE");
        assertEquals(1000, pkt.timestampMillis());
        assertEquals("TGT-001", pkt.entityId());
        assertEquals(500.0, pkt.xMeters(), EPSILON);
        assertEquals(300.0, pkt.yMeters(), EPSILON);
        assertEquals(100.0, pkt.vxMetersPerSec(), EPSILON);
        assertEquals(50.0, pkt.vyMetersPerSec(), EPSILON);
        assertEquals("TARGET_UPDATE", pkt.eventType());
    }

    @Test
    void rejectsNullEntityId() {
        assertThrows(IllegalArgumentException.class, () ->
                new TelemetryPacket(0, null, 0, 0, 0, 0, "UPDATE"));
    }

    @Test
    void rejectsBlankEntityId() {
        assertThrows(IllegalArgumentException.class, () ->
                new TelemetryPacket(0, "  ", 0, 0, 0, 0, "UPDATE"));
    }

    @Test
    void rejectsNullEventType() {
        assertThrows(IllegalArgumentException.class, () ->
                new TelemetryPacket(0, "TGT", 0, 0, 0, 0, null));
    }

    @Test
    void rejectsBlankEventType() {
        assertThrows(IllegalArgumentException.class, () ->
                new TelemetryPacket(0, "TGT", 0, 0, 0, 0, "  "));
    }

    @Test
    void recordEqualityWorks() {
        TelemetryPacket a = new TelemetryPacket(1000, "TGT-1", 10, 20, 30, 40, "UPDATE");
        TelemetryPacket b = new TelemetryPacket(1000, "TGT-1", 10, 20, 30, 40, "UPDATE");
        assertEquals(a, b);
    }

    @Test
    void toStringContainsKeyFields() {
        TelemetryPacket pkt = new TelemetryPacket(
                5000, "TGT-042", 1500.0, 3000.0, 250.0, 0.0, "RADAR_SWEEP");
        String s = pkt.toString();
        assertTrue(s.contains("5000"));
        assertTrue(s.contains("TGT-042"));
        assertTrue(s.contains("RADAR_SWEEP"));
    }

    @Test
    void zeroTimestampIsValid() {
        TelemetryPacket pkt = new TelemetryPacket(
                0, "TGT-1", 0, 0, 0, 0, "START");
        assertEquals(0, pkt.timestampMillis());
    }

    @Test
    void negativePositionIsValid() {
        TelemetryPacket pkt = new TelemetryPacket(
                100, "TGT-1", -500.0, -300.0, -10.0, -20.0, "UPDATE");
        assertEquals(-500.0, pkt.xMeters(), EPSILON);
        assertEquals(-300.0, pkt.yMeters(), EPSILON);
    }
}

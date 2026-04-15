package io.github.banjokazooie7.telemetry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DropDetectorTest {

    private DropDetector detector;

    @BeforeEach
    void setUp() {
        detector = new DropDetector();
    }

    @Test
    void startsEmpty() {
        assertEquals(0, detector.getTotalReceived());
        assertEquals(0, detector.getTotalDropped());
        assertEquals(0, detector.getExpectedNext());
        assertTrue(detector.getDroppedSequences().isEmpty());
    }

    @Test
    void consecutivePacketsNoDrop() {
        detector.record(makePacket(0));
        detector.record(makePacket(1));
        detector.record(makePacket(2));

        assertEquals(3, detector.getTotalReceived());
        assertEquals(0, detector.getTotalDropped());
        assertEquals(3, detector.getExpectedNext());
        assertTrue(detector.getDroppedSequences().isEmpty());
    }

    @Test
    void detectsSingleDrop() {
        detector.record(makePacket(0));
        detector.record(makePacket(2));

        assertEquals(2, detector.getTotalReceived());
        assertEquals(1, detector.getTotalDropped());
        assertEquals(1, detector.getDroppedSequences().size());
        assertEquals(1, detector.getDroppedSequences().get(0));
    }

    @Test
    void detectsMultipleConsecutiveDrops() {
        detector.record(makePacket(0));
        detector.record(makePacket(5));

        assertEquals(2, detector.getTotalReceived());
        assertEquals(4, detector.getTotalDropped());
        assertEquals(java.util.List.of(1, 2, 3, 4), detector.getDroppedSequences());
    }

    @Test
    void detectsDropAtStart() {
        detector.record(makePacket(3));

        assertEquals(1, detector.getTotalReceived());
        assertEquals(3, detector.getTotalDropped());
        assertEquals(java.util.List.of(0, 1, 2), detector.getDroppedSequences());
    }

    @Test
    void detectsMultipleGaps() {
        detector.record(makePacket(0));
        detector.record(makePacket(2));
        detector.record(makePacket(3));
        detector.record(makePacket(6));

        assertEquals(4, detector.getTotalReceived());
        assertEquals(3, detector.getTotalDropped());
        assertEquals(java.util.List.of(1, 4, 5), detector.getDroppedSequences());
    }

    @Test
    void dropRateIsZeroWhenNothingDropped() {
        detector.record(makePacket(0));
        detector.record(makePacket(1));
        assertEquals(0.0, detector.getDropRate(), 1e-9);
    }

    @Test
    void dropRateCalculatesCorrectly() {
        detector.record(makePacket(0));
        detector.record(makePacket(3));

        // received=2, dropped=2, total=4, rate=0.5
        assertEquals(0.5, detector.getDropRate(), 1e-9);
    }

    @Test
    void dropRateIsZeroWhenEmpty() {
        assertEquals(0.0, detector.getDropRate(), 1e-9);
    }

    @Test
    void resetClearsEverything() {
        detector.record(makePacket(0));
        detector.record(makePacket(5));
        detector.reset();

        assertEquals(0, detector.getTotalReceived());
        assertEquals(0, detector.getTotalDropped());
        assertEquals(0, detector.getExpectedNext());
        assertTrue(detector.getDroppedSequences().isEmpty());
    }

    @Test
    void droppedSequencesListIsUnmodifiable() {
        detector.record(makePacket(0));
        detector.record(makePacket(2));
        assertThrows(UnsupportedOperationException.class, () ->
                detector.getDroppedSequences().add(99));
    }

    @Test
    void toSummaryContainsKeyInfo() {
        detector.record(makePacket(0));
        detector.record(makePacket(3));
        String summary = detector.toSummary();
        assertTrue(summary.contains("received=2"));
        assertTrue(summary.contains("dropped=2"));
    }

    @Test
    void ignoreDuplicatePackets(){
        detector.record(makePacket(0));
        detector.record(makePacket(0));

        assertEquals(1, detector.getTotalReceived(), "Should ignore duplicate sequence numbers");
        assertEquals(0, detector.getTotalDropped());
    }

    @Test
    void handlesLatePacketsGracefully(){
        detector.record(makePacket(0));
        detector.record(makePacket(2));//detect drop of 1
        detector.record(makePacket(1));//1 arrives late

        assertFalse(detector.getDroppedSequences().contains(1), "Late packet should be removed from dropped list");
    }

    private TelemetryPacket makePacket(int seq) {
        return new TelemetryPacket(seq, 0, "TGT", 0, 0, 0, 0, "UPDATE");
    }
}
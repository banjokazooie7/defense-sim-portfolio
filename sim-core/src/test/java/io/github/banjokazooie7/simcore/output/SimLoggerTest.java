package io.github.banjokazooie7.simcore.output;

import io.github.banjokazooie7.simcore.event.SimEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimLoggerTest {

    private SimLogger logger;

    @BeforeEach
    void setUp() {
        logger = new SimLogger();
    }

    @Test
    void startsWithZeroRows() {
        assertEquals(0, logger.getRowCount());
    }

    @Test
    void buffersEventsAsRows() {
        fireEvent("RADAR_SWEEP", 0, 1000);
        fireEvent("TARGET_UPDATE", 0, 2000);
        assertEquals(2, logger.getRowCount());
    }

    @Test
    void csvStartsWithHeader() {
        String csv = logger.toCsvString();
        assertTrue(csv.startsWith("sim_time_ms,event_type,priority,detail\n"));
    }

    @Test
    void csvContainsEventData() {
        fireEvent("RADAR_SWEEP", 0, 1000);
        String csv = logger.toCsvString();
        assertTrue(csv.contains("1000,RADAR_SWEEP,0,"));
    }

    @Test
    void csvIncludesPriority() {
        SimEvent event = new SimEvent(500, -1, "THREAT_ALERT", () -> {});
        logger.onEvent(event, 500);
        String csv = logger.toCsvString();
        assertTrue(csv.contains("500,THREAT_ALERT,-1,"));
    }

    @Test
    void detailSupplierAddsContext() {
        logger.setDetailSupplier((event, simTime) -> "pos=(100.0 200.0)");
        fireEvent("TARGET_UPDATE", 0, 1000);
        String csv = logger.toCsvString();
        assertTrue(csv.contains("pos=(100.0 200.0)"));
    }

    @Test
    void nullDetailSupplier_producesEmptyDetail() {
        logger.setDetailSupplier(null);
        fireEvent("A", 0, 100);
        String csv = logger.toCsvString();
        assertTrue(csv.contains("100,A,0,"));
    }

    @Test
    void detailSupplierReturningNull_treatedAsEmpty() {
        logger.setDetailSupplier((event, simTime) -> null);
        fireEvent("A", 0, 100);
        assertEquals(1, logger.getRowCount());
        String csv = logger.toCsvString();
        assertNotNull(csv);
    }

    @Test
    void escapesCommasInDetail() {
        logger.setDetailSupplier((event, simTime) -> "x=100,y=200");
        fireEvent("A", 0, 100);
        String csv = logger.toCsvString();
        assertTrue(csv.contains("\"x=100,y=200\""));
    }

    @Test
    void plainDetailNotQuoted() {
        String result = SimLogger.escapeCsv("simple text");
        assertEquals("simple text", result);
    }

    @Test
    void flushWritesToFile(@TempDir Path tempDir) throws IOException {
        fireEvent("RADAR_SWEEP", 0, 1000);
        fireEvent("TARGET_UPDATE", 0, 2000);

        Path outFile = tempDir.resolve("test-log.csv");
        logger.flush(outFile);

        assertTrue(Files.exists(outFile));
        List<String> lines = Files.readAllLines(outFile);
        assertEquals(3, lines.size());
        assertEquals("sim_time_ms,event_type,priority,detail", lines.get(0));
        assertTrue(lines.get(1).startsWith("1000,RADAR_SWEEP,"));
        assertTrue(lines.get(2).startsWith("2000,TARGET_UPDATE,"));
    }

    @Test
    void flushCreatesParentDirectories(@TempDir Path tempDir) {
        fireEvent("A", 0, 100);
        Path nested = tempDir.resolve("sub/dir/log.csv");
        logger.flush(nested);
        assertTrue(Files.exists(nested));
    }

    @Test
    void resetClearsRows() {
        fireEvent("A", 0, 100);
        fireEvent("B", 0, 200);
        assertEquals(2, logger.getRowCount());
        logger.reset();
        assertEquals(0, logger.getRowCount());
    }

    private void fireEvent(String type, int priority, long simTime) {
        SimEvent event = new SimEvent(simTime, priority, type, () -> {});
        logger.onEvent(event, simTime);
    }
}

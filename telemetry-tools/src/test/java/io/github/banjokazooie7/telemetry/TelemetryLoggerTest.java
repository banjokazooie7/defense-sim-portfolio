package io.github.banjokazooie7.telemetry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TelemetryLoggerTest {

    private TelemetryLogger logger;

    @BeforeEach
    void setUp() {
        logger = new TelemetryLogger();
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
    void logsSinglePacketAsCsvRow() {
        logger.log(makePacket(0, "drone-1", "POSITION"));

        assertEquals(1, logger.getRowCount());
        String row = logger.getRows().get(0);
        assertTrue(row.startsWith("0,0,drone-1,"), "Row should start with seq,time,entity. Got: " + row);
        assertTrue(row.endsWith(",POSITION"), "Row should end with event type. Got: " + row);
    }

    @Test
    void logAllBatchesPackets() {
        List<TelemetryPacket> batch = List.of(
                makePacket(0, "drone-1", "POSITION"),
                makePacket(1, "drone-1", "POSITION"),
                makePacket(2, "drone-2", "CONTACT"));

        logger.logAll(batch);

        assertEquals(3, logger.getRowCount());
    }

    @Test
    void csvStringIncludesHeader() {
        logger.log(makePacket(0, "drone-1", "POSITION"));

        String csv = logger.toCsvString();
        assertTrue(csv.startsWith("seq,sim_time_ms,entity_id,x_meters,y_meters,vx_m_s,vy_m_s,event_type\n"),
                "CSV should begin with header line");
        assertTrue(csv.contains("drone-1"), "CSV should contain logged row");
    }

    @Test
    void escapesCommasInFields() {
        logger.log(makePacket(0, "drone,alpha", "POSITION"));

        String row = logger.getRows().get(0);
        assertTrue(row.contains("\"drone,alpha\""),
                "Comma-containing entity ID should be quoted. Got: " + row);
    }

    @Test
    void escapesQuotesInFields() {
        logger.log(makePacket(0, "drone\"x\"", "POSITION"));

        String row = logger.getRows().get(0);
        assertTrue(row.contains("\"drone\"\"x\"\"\""),
                "Embedded quotes should be doubled and field wrapped. Got: " + row);
    }

    @Test
    void flushWritesCsvToDisk(@TempDir Path tmp) throws Exception {
        logger.log(makePacket(0, "drone-1", "POSITION"));
        logger.log(makePacket(1, "drone-1", "CONTACT"));

        Path out = tmp.resolve("logs/telemetry.csv");
        logger.flush(out);

        assertTrue(Files.exists(out), "Output file should exist");
        List<String> lines = Files.readAllLines(out);
        assertEquals(3, lines.size(), "Header + 2 rows");
        assertEquals("seq,sim_time_ms,entity_id,x_meters,y_meters,vx_m_s,vy_m_s,event_type", lines.get(0));
        assertTrue(lines.get(1).contains("POSITION"));
        assertTrue(lines.get(2).contains("CONTACT"));
    }

    @Test
    void resetClearsBuffer() {
        logger.log(makePacket(0, "drone-1", "POSITION"));
        logger.log(makePacket(1, "drone-1", "POSITION"));
        assertEquals(2, logger.getRowCount());

        logger.reset();

        assertEquals(0, logger.getRowCount());
        assertTrue(logger.getRows().isEmpty());
    }
}

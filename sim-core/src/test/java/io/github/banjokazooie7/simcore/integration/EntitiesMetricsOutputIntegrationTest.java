package io.github.banjokazooie7.simcore.integration;

import io.github.banjokazooie7.simcore.engine.DiscreteEventEngine;
import io.github.banjokazooie7.simcore.entity.Position2D;
import io.github.banjokazooie7.simcore.entity.Target;
import io.github.banjokazooie7.simcore.event.SimEvent;
import io.github.banjokazooie7.simcore.metrics.MetricsCollector;
import io.github.banjokazooie7.simcore.output.SimLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntitiesMetricsOutputIntegrationTest {

    private static final double EPSILON = 1e-3;

    @Test
    void targetMovesCorrectlyThroughEngine() {
        DiscreteEventEngine engine = new DiscreteEventEngine(5_000);
        Target target = new Target("TGT-1", new Position2D(0, 0), 100.0, 0.0);

        for (long t = 1000; t <= 5000; t += 1000) {
            engine.schedule(new SimEvent(t, 0, "TARGET_UPDATE", () -> target.update(1000)));
        }

        engine.run();

        assertEquals(500.0, target.getPosition().xMeters(), EPSILON);
        assertEquals(0.0, target.getPosition().yMeters(), EPSILON);
    }

    @Test
    void maneuverChangesTargetDirection() {
        DiscreteEventEngine engine = new DiscreteEventEngine(4_000);
        Target target = new Target("TGT-M", new Position2D(0, 0), 100.0, 0.0);

        engine.schedule(new SimEvent(1_000, 0, "TARGET_UPDATE", () -> target.update(1000)));
        engine.schedule(new SimEvent(2_000, -1, "MANEUVER", () -> target.setVelocity(0.0, 100.0)));
        engine.schedule(new SimEvent(2_000, 0, "TARGET_UPDATE", () -> target.update(1000)));
        engine.schedule(new SimEvent(3_000, 0, "TARGET_UPDATE", () -> target.update(1000)));

        engine.run();

        assertEquals(100.0, target.getPosition().xMeters(), EPSILON);
        assertEquals(200.0, target.getPosition().yMeters(), EPSILON);
    }

    @Test
    void metricsCollectorCountsEntityEvents() {
        DiscreteEventEngine engine = new DiscreteEventEngine(5_000);
        MetricsCollector metrics = new MetricsCollector();
        engine.addListener(metrics);

        Target target = new Target("TGT-1", new Position2D(0, 0), 50.0, 0.0);

        for (long t = 1000; t <= 5000; t += 1000) {
            engine.schedule(new SimEvent(t, 0, "TARGET_UPDATE", () -> target.update(1000)));
        }
        for (long t = 0; t <= 4000; t += 2000) {
            engine.schedule(new SimEvent(t, 0, "RADAR_SWEEP", () -> {}));
        }

        engine.run();

        assertEquals(5, metrics.getCount("TARGET_UPDATE"));
        assertEquals(3, metrics.getCount("RADAR_SWEEP"));
        assertEquals(8, metrics.getTotalEvents());
    }

    @Test
    void loggerWritesCorrectCsv(@TempDir Path tempDir) throws IOException {
        DiscreteEventEngine engine = new DiscreteEventEngine(3_000);
        SimLogger logger = new SimLogger();
        Target target = new Target("TGT-1", new Position2D(0, 0), 100.0, 0.0);

        logger.setDetailSupplier((event, simTime) -> {
            if ("TARGET_UPDATE".equals(event.getType())) {
                return target.getId() + " at " + target.getPosition();
            }
            return "";
        });

        engine.addListener(logger);

        for (long t = 1000; t <= 3000; t += 1000) {
            engine.schedule(new SimEvent(t, 0, "TARGET_UPDATE", () -> target.update(1000)));
        }

        engine.run();

        Path csvFile = tempDir.resolve("test-output.csv");
        logger.flush(csvFile);

        List<String> lines = Files.readAllLines(csvFile);
        assertEquals(4, lines.size());
        assertEquals("sim_time_ms,event_type,priority,detail", lines.get(0));
        assertTrue(lines.get(1).startsWith("1000,TARGET_UPDATE,0,"));
        assertTrue(lines.get(1).contains("TGT-1"));
    }

    @Test
    void metricsAndLoggerWorkSimultaneously() {
        DiscreteEventEngine engine = new DiscreteEventEngine(2_000);
        MetricsCollector metrics = new MetricsCollector();
        SimLogger logger = new SimLogger();

        engine.addListener(metrics);
        engine.addListener(logger);

        engine.schedule(new SimEvent(500, 0, "A", () -> {}));
        engine.schedule(new SimEvent(1000, 0, "B", () -> {}));
        engine.schedule(new SimEvent(1500, 0, "A", () -> {}));

        engine.run();

        assertEquals(3, metrics.getTotalEvents());
        assertEquals(3, logger.getRowCount());
        assertEquals(2, metrics.getCount("A"));
        assertEquals(1, metrics.getCount("B"));
    }

    @Test
    void distanceBetweenTargetsChangesOverTime() {
        DiscreteEventEngine engine = new DiscreteEventEngine(3_000);
        Target tgtA = new Target("A", new Position2D(0, 0), 100.0, 0.0);
        Target tgtB = new Target("B", new Position2D(0, 0), 0.0, 100.0);

        assertEquals(0.0, tgtA.getPosition().distanceTo(tgtB.getPosition()), EPSILON);

        for (long t = 1000; t <= 2000; t += 1000) {
            engine.schedule(new SimEvent(t, 0, "UPDATE_A", () -> tgtA.update(1000)));
            engine.schedule(new SimEvent(t, 0, "UPDATE_B", () -> tgtB.update(1000)));
        }

        engine.run();

        double finalDist = tgtA.getPosition().distanceTo(tgtB.getPosition());
        assertEquals(Math.sqrt(200 * 200 + 200 * 200), finalDist, EPSILON);
    }
}

package io.github.banjokazooie7.simcore.examples;

import io.github.banjokazooie7.simcore.engine.DiscreteEventEngine;
import io.github.banjokazooie7.simcore.entity.Position2D;
import io.github.banjokazooie7.simcore.entity.Target;
import io.github.banjokazooie7.simcore.event.SimEvent;
import io.github.banjokazooie7.simcore.metrics.MetricsCollector;
import io.github.banjokazooie7.simcore.output.SimLogger;

import java.nio.file.Path;

public final class Week2Demo {

    private static final long SIM_DURATION_MS = 10_000;
    private static final long SWEEP_INTERVAL  = 2_000;
    private static final long UPDATE_INTERVAL = 500;

    public static void main(String[] args) {
        System.out.println("=== Week 2 Demo: Entities + Metrics + Output ===");
        System.out.println();

        DiscreteEventEngine engine = new DiscreteEventEngine(SIM_DURATION_MS);

        Target tgtA = new Target("TGT-A", new Position2D(1000, 5000), 250.0, 0.0);
        Target tgtB = new Target("TGT-B", new Position2D(2000, 2000), 212.0, 212.0);

        System.out.println("Initial state:");
        System.out.println("  " + tgtA);
        System.out.println("  " + tgtB);
        System.out.println();

        MetricsCollector metrics = new MetricsCollector();
        SimLogger logger = new SimLogger();

        logger.setDetailSupplier((event, simTime) -> switch (event.getType()) {
            case "TARGET_UPDATE_A" -> tgtA.getId() + " at " + tgtA.getPosition();
            case "TARGET_UPDATE_B" -> tgtB.getId() + " at " + tgtB.getPosition();
            case "TARGET_MANEUVER" -> tgtB.getId() + " turns north";
            default -> "";
        });

        engine.addListener(metrics);
        engine.addListener(logger);

        scheduleRepeating(engine, 0, SWEEP_INTERVAL, SIM_DURATION_MS, "RADAR_SWEEP", () ->
                System.out.printf("  [RADAR]  sweep at t=%dms%n", engine.getClock().now()));

        scheduleRepeating(engine, 0, UPDATE_INTERVAL, SIM_DURATION_MS, "TARGET_UPDATE_A", () ->
                tgtA.update(UPDATE_INTERVAL));

        scheduleRepeating(engine, 0, UPDATE_INTERVAL, SIM_DURATION_MS, "TARGET_UPDATE_B", () ->
                tgtB.update(UPDATE_INTERVAL));

        engine.schedule(new SimEvent(5_000, -1, "TARGET_MANEUVER", () -> {
            System.out.printf("  [MANEUVER] %s turns north at t=%dms%n",
                    tgtB.getId(), engine.getClock().now());
            tgtB.setVelocity(0.0, 300.0);
        }));

        System.out.println("--- Simulation running ---");
        DiscreteEventEngine.RunStats stats = engine.run();
        System.out.println("--- Simulation complete ---");
        System.out.println();

        System.out.println("Final state:");
        System.out.println("  " + tgtA);
        System.out.println("  " + tgtB);
        System.out.println();

        System.out.println(metrics.toSummary());
        System.out.println(stats);

        Path csvPath = Path.of("docs/proof/week2-event-log.csv");
        logger.flush(csvPath);

	logger.flush(csvPath);
        System.out.println("Event log written to: " + csvPath);
    }

    private static void scheduleRepeating(DiscreteEventEngine engine,
                                          long atTime, long interval, long endTime,
                                          String type, Runnable action) {
        if (atTime > endTime) return;
        engine.schedule(new SimEvent(atTime, 0, type, () -> {
            action.run();
            scheduleRepeating(engine, atTime + interval, interval, endTime, type, action);
        }));
    }
}

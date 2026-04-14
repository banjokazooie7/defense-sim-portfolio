package io.github.banjokazooie7.telemetry;

/**
 * One telemetry message — the unit of data sent over the wire.
 *
 * Fields match what a real radar telemetry feed would carry:
 * - timestamp: when this data was generated (sim time ms)
 * - entityId: which entity this data is about
 * - x, y: position in meters
 * - vx, vy: velocity in m/s
 * - eventType: what kind of event triggered this packet
 */
public record TelemetryPacket(
        long timestampMillis,
        String entityId,
        double xMeters,
        double yMeters,
        double vxMetersPerSec,
        double vyMetersPerSec,
        String eventType
) {
    public TelemetryPacket {
        if (entityId == null || entityId.isBlank()) {
            throw new IllegalArgumentException("entityId must not be null or blank");
        }
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("eventType must not be null or blank");
        }
    }

    @Override
    public String toString() {
        return String.format(
                "TelemetryPacket{t=%dms, id=%s, pos=(%.1f,%.1f), vel=(%.1f,%.1f), type=%s}",
                timestampMillis, entityId, xMeters, yMeters,
                vxMetersPerSec, vyMetersPerSec, eventType);
    }
}

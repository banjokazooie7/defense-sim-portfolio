package io.github.banjokazooie7.telemetry;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Encodes TelemetryPackets to binary bytes and decodes bytes back to packets.
 *
 * Uses java.nio.ByteBuffer with big-endian byte order, which is the
 * network standard (and the default for ByteBuffer). Defense systems
 * call this "network byte order."
 */
public final class PacketCodec {

    private PacketCodec() {}

    /**
     * Encode a TelemetryPacket into a byte array for transmission.
     */
    public static byte[] encode(TelemetryPacket packet) {
        byte[] entityIdBytes = packet.entityId().getBytes(StandardCharsets.UTF_8);
        byte[] eventTypeBytes = packet.eventType().getBytes(StandardCharsets.UTF_8);

        int totalSize = 8 + 8 + 8 + 8 + 8   // long + 4 doubles
                + 2 + entityIdBytes.length     // short + string
                + 2 + eventTypeBytes.length;   // short + string

        ByteBuffer buf = ByteBuffer.allocate(totalSize);
        buf.putLong(packet.timestampMillis());
        buf.putDouble(packet.xMeters());
        buf.putDouble(packet.yMeters());
        buf.putDouble(packet.vxMetersPerSec());
        buf.putDouble(packet.vyMetersPerSec());
        buf.putShort((short) entityIdBytes.length);
        buf.put(entityIdBytes);
        buf.putShort((short) eventTypeBytes.length);
        buf.put(eventTypeBytes);

        return buf.array();
    }

    /**
     * Decode a byte array back into a TelemetryPacket.
     */
    public static TelemetryPacket decode(byte[] data) {
        ByteBuffer buf = ByteBuffer.wrap(data);

        long timestamp = buf.getLong();
        double x = buf.getDouble();
        double y = buf.getDouble();
        double vx = buf.getDouble();
        double vy = buf.getDouble();

        short entityIdLen = buf.getShort();
        byte[] entityIdBytes = new byte[entityIdLen];
        buf.get(entityIdBytes);
        String entityId = new String(entityIdBytes, StandardCharsets.UTF_8);

        short eventTypeLen = buf.getShort();
        byte[] eventTypeBytes = new byte[eventTypeLen];
        buf.get(eventTypeBytes);
        String eventType = new String(eventTypeBytes, StandardCharsets.UTF_8);

        return new TelemetryPacket(timestamp, entityId, x, y, vx, vy, eventType);
    }
}

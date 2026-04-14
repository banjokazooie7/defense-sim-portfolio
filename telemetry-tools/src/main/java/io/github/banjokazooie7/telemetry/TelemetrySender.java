package io.github.banjokazooie7.telemetry;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Sends TelemetryPackets over UDP.
 *
 * Usage: create a sender with a target host/port, call send() for each
 * packet, then close() when done. Implements Closeable so it works
 * with try-with-resources.
 */
public final class TelemetrySender implements Closeable {

    private final DatagramSocket socket;
    private final InetAddress address;
    private final int port;
    private long packetsSent;

    public TelemetrySender(String host, int port) {
        try {
            this.socket = new DatagramSocket();
            this.address = InetAddress.getByName(host);
            this.port = port;
            this.packetsSent = 0;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create sender for " + host + ":" + port, e);
        }
    }

    /**
     * Encode and send one telemetry packet over UDP.
     */
    public void send(TelemetryPacket packet) {
        byte[] data = PacketCodec.encode(packet);
        DatagramPacket dgram = new DatagramPacket(data, data.length, address, port);
        try {
            socket.send(dgram);
            packetsSent++;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to send packet", e);
        }
    }

    public long getPacketsSent() {
        return packetsSent;
    }

    @Override
    public void close() {
        socket.close();
    }
}

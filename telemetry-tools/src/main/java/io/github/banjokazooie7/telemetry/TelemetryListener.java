package io.github.banjokazooie7.telemetry;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Listens for TelemetryPackets on a UDP port.
 *
 * Call receiveOne() to block and wait for a single packet, or
 * receiveAll() to collect packets until a timeout expires with
 * no new data.
 */
public final class TelemetryListener implements Closeable {

    private static final int MAX_PACKET_SIZE = 4096;

    private final DatagramSocket socket;
    private long packetsReceived;

    /**
     * @param port           UDP port to listen on
     * @param timeoutMillis  how long receiveOne() blocks before giving up (ms)
     */
    public TelemetryListener(int port, int timeoutMillis) {
        try {
            this.socket = new DatagramSocket(port);
            this.socket.setSoTimeout(timeoutMillis);
            this.packetsReceived = 0;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create listener on port " + port, e);
        }
    }

    /**
     * Block and wait for one packet. Returns null if the timeout expires.
     */
    public TelemetryPacket receiveOne() {
        byte[] buf = new byte[MAX_PACKET_SIZE];
        DatagramPacket dgram = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(dgram);
            byte[] actual = new byte[dgram.getLength()];
            System.arraycopy(dgram.getData(), 0, actual, 0, dgram.getLength());
            packetsReceived++;
            return PacketCodec.decode(actual);
        } catch (SocketTimeoutException e) {
            return null;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to receive packet", e);
        }
    }

    /**
     * Receive packets until the timeout expires with no new data.
     * Returns all packets collected.
     */
    public List<TelemetryPacket> receiveAll() {
        List<TelemetryPacket> packets = new ArrayList<>();
        TelemetryPacket pkt;
        while ((pkt = receiveOne()) != null) {
            packets.add(pkt);
        }
        return Collections.unmodifiableList(packets);
    }

    public long getPacketsReceived() {
        return packetsReceived;
    }

    @Override
    public void close() {
        socket.close();
    }
}

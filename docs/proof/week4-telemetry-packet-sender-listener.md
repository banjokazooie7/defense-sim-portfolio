# Week 4 Proof Artifact — Telemetry Packet + Sender/Listener

**Date:** Week of Mar 09–15, 2026
**Focus:** Binary telemetry over UDP

## What was built

### telemetry-tools module (new)
- TelemetryPacket — record holding timestamp, entityId, position, velocity, eventType
- PacketCodec — binary encode/decode using ByteBuffer (network byte order)
- TelemetrySender — sends encoded packets over UDP
- TelemetryListener — receives and decodes UDP packets with configurable timeout
- Week4Demo — sim engine drives a target, sends position updates over UDP to a listener

### Key design decisions
- Binary format over JSON: smaller packets, faster parsing, predictable size
- UDP over TCP: defense telemetry prioritizes low latency over guaranteed delivery
- ByteBuffer with big-endian: matches network byte order standard
- Listener uses socket timeout: receiveAll() collects until no data arrives, then returns

## Tests
- TelemetryPacketTest: 9 tests (construction, validation, equality)
- PacketCodecTest: 7 tests (round-trip, zero values, negative values, size check)
- SenderListenerTest: 5 tests (single packet, multiple packets, timeout, network round-trip)

## Demo command
```bash
./gradlew :telemetry-tools:run
```


# defense-sim-portfolio

Java / Linux simulation + telemetry portfolio — DES engine, radar tracking sim, UDP telemetry tools.

Built as a portfolio project targeting defense-contractor software engineering roles.

## Requirements
- Java 17+
- Linux / WSL recommended (all demos tested on Ubuntu)

## Quick start
Run the sim-core demo (targets, radar sweeps, metrics, CSV output):
```bash
./gradlew :sim-core:run
```

Run the Week 4 telemetry demo (UDP sender + listener + pipeline, writes CSV):
```bash
./gradlew :telemetry-tools:run
```

Run all tests:
```bash
./gradlew test
```

## Project structure

| Module            | Status     | Description                                         |
|-------------------|------------|-----------------------------------------------------|
| `sim-core`        | **v1.0**   | DES engine, entities, metrics, CSV output           |
| `telemetry-tools` | **v1.0**   | UDP packet sender/listener, binary telemetry codec  |
| `radar-sim`       | planned    | Radar sensor model, target tracking, metrics        |

## sim-core architecture

    sim-core/
    ├── engine/
    │   └── DiscreteEventEngine    # priority-queue event scheduler
    ├── entity/
    │   ├── Entity                 # interface — contract for sim objects
    │   ├── Position2D             # immutable 2D coordinate (meters)
    │   └── Target                 # constant-velocity target with heading
    ├── event/
    │   ├── SimEvent               # time + priority + type + callback
    │   └── EventListener          # observer interface
    ├── metrics/
    │   └── MetricsCollector       # counts events by type, tracks timing
    ├── output/
    │   └── SimLogger              # writes event log to CSV
    └── SimClock                   # monotonic millisecond clock

## telemetry-tools architecture

    telemetry-tools/
    ├── TelemetryPacket            # immutable packet record (seq, time, pos, vel, type)
    ├── PacketCodec                # binary encode/decode for the wire format
    ├── TelemetrySender            # UDP sender (Closeable)
    ├── TelemetryListener          # UDP receiver with timeout-bounded receiveAll()
    ├── PacketSink                 # observer interface — one packet, many consumers
    ├── DropDetector               # gap detection, late-packet reconciliation, drop rate
    ├── TelemetryLogger            # buffered CSV writer for post-mission analysis
    ├── TelemetryPipeline          # owns a listener, fans packets out to sinks
    └── examples/
        └── Week4Demo              # end-to-end run across both modules

### Data flow

    sim-core    ──┐
                  ├──> TelemetrySender ──(UDP)──> TelemetryListener
                  │                                       │
                  │                                       v
                  │                               TelemetryPipeline
                  │                                    │       │
                  │                                    v       v
                  │                            DropDetector  TelemetryLogger
                  │                                    │            │
                  │                                    v            v
                  │                              audit summary    CSV file
                  │
              schedules packet sends via SimEvent callbacks

### Design notes

- **UDP over TCP.** Real defense telemetry prioritizes freshness over reliability — a 200ms-old position is worse than a missing one. Drops are audited rather than retried.
- **Observer fan-out (`PacketSink`).** Same pattern as `sim-core`'s `EventListener`. One packet, many independent consumers. Adding a new sink (e.g. an alerting module) costs one class and one `addSink()` call — no changes to the pipeline or existing sinks.
- **Sink failure isolation.** `TelemetryPipeline.dispatch` catches `RuntimeException` per sink so one misbehaving consumer (disk full on the logger, say) can't starve the others.
- **Drop detection algorithm.** Gap-in-sequence-numbers with late-packet reconciliation: when a packet arrives whose sequence number is in the dropped list, it's removed and the drop count decremented. Duplicates are ignored via a seen-set.

## Week log

| Week | Focus                    | Key deliverables                                          |
|------|--------------------------|-----------------------------------------------------------|
| 0    | Bootstrapping            | Gradle skeleton, CI, SimClock, HelloSim                   |
| 1    | DES engine core          | SimEvent, DiscreteEventEngine, EventListener              |
| 2    | Entities + metrics + out | Position2D, Entity, Target, MetricsCollector, SimLogger   |
| 3    | sim-core v1.0 polish     | Heading, expanded tests, package-info, bug fixes          |
| 4    | Telemetry packet         | TelemetryPacket, PacketCodec, Sender, Listener            |
| 6    | Telemetry over UDP       | Packet/Codec/Sender/Listener + PacketSink, DropDetector, TelemetryLogger, TelemetryPipeline |

### Sample output

Running `./gradlew :telemetry-tools:run`:

    === Week 4 Demo: Telemetry over UDP with Pipeline ===
    Target: Target{id=TGT-A, pos=(0.0m, 0.0m), vel=(200.0, 150.0) m/s, hdg=53.1°, spd=250.0 m/s}
    Sending telemetry to localhost:19900
    --- Simulation running ---
      [t=1000ms] Sent seq=0: TGT-A at (200.0m, 150.0m)
      [t=2000ms] Sent seq=1: TGT-A at (400.0m, 300.0m)
      [t=3000ms] Sent seq=2: TGT-A at (600.0m, 450.0m)
      [t=4000ms] Sent seq=3: TGT-A at (800.0m, 600.0m)
      [t=5000ms] Sent seq=4: TGT-A at (1000.0m, 750.0m)
    --- Simulation complete ---
    Packets sent: 5
    --- Pipeline dispatched 5 packets to 2 sinks ---
    --- Telemetry Audit ---
    DropDetector{received=5, dropped=0, dropRate=0.00%, gaps=[]}
    CSV written to: telemetry-tools/build/telemetry-out/week4-demo.csv

CSV output:

    seq,sim_time_ms,entity_id,x_meters,y_meters,vx_m_s,vy_m_s,event_type
    0,1000,TGT-A,200.0,150.0,200.0,150.0,TARGET_UPDATE
    1,2000,TGT-A,400.0,300.0,200.0,150.0,TARGET_UPDATE
    2,3000,TGT-A,600.0,450.0,200.0,150.0,TARGET_UPDATE
    3,4000,TGT-A,800.0,600.0,200.0,150.0,TARGET_UPDATE
    4,5000,TGT-A,1000.0,750.0,200.0,150.0,TARGET_UPDATE

## License

MIT

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

## Week log

| Week | Focus                    | Key deliverables                                          |
|------|--------------------------|-----------------------------------------------------------|
| 0    | Bootstrapping            | Gradle skeleton, CI, SimClock, HelloSim                   |
| 1    | DES engine core          | SimEvent, DiscreteEventEngine, EventListener              |
| 2    | Entities + metrics + out | Position2D, Entity, Target, MetricsCollector, SimLogger   |
| 3    | sim-core v1.0 polish     | Heading, expanded tests, package-info, bug fixes          |
| 4    | Telemetry packet         | TelemetryPacket, PacketCodec, Sender, Listener            |
## License

MIT

# Week 3 Proof Artifact — sim-core v1.0 Polish

**Date:** Week of Mar 02–08, 2026
**Focus:** Harden sim-core to production quality for v1.0

## What changed

### Target heading
- Added getHeadingDegrees() using compass-bearing convention (0=north, clockwise)
- 7 new heading tests (N, E, S, W, NE, stationary, post-maneuver)

### SimClock fixes
- Added input validation on constructor (rejects negative start time)
- Added toString() method
- Fixed advance() referencing wrong variable
- Expanded SimClockTest from 2 to 8 tests

### Package-info files
- Added package-info.java for all 6 packages
- Enables proper Javadoc generation

### Cleanup
- Removed orphaned HelloSimMain.java
- Updated README with architecture diagram and week log

## Demo command
```bash
./gradlew :sim-core:run
```

## Version
sim-core is now at v1.0.

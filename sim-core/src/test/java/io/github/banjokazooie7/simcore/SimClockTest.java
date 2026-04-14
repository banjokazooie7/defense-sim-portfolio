package io.github.banjokazooie7.simcore;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SimClockTest {

    @Test
    void advancesTime() {
        SimClock clock = new SimClock(1000);
        clock.advance(250);
        assertEquals(1250, clock.now());
    }

    @Test
    void rejectsNegativeAdvance() {
        SimClock clock = new SimClock(0);
        assertThrows(IllegalArgumentException.class, () -> clock.advance(-1));
    }

    @Test
    void rejectsNegativeStartTime() {
        assertThrows(IllegalArgumentException.class, () -> new SimClock(-100));
    }

    @Test
    void zeroAdvanceIsAllowed() {
        SimClock clock = new SimClock(500);
        clock.advance(0);
        assertEquals(500, clock.now());
    }

    @Test
    void resetReturnsClock_toZero() {
        SimClock clock = new SimClock(1000);
        clock.advance(500);
        assertEquals(1500, clock.now());
        clock.reset();
        assertEquals(0, clock.now());
    }

    @Test
    void toStringContainsCurrentTime() {
        SimClock clock = new SimClock(42);
        assertTrue(clock.toString().contains("42"));
    }

    @Test
    void multipleAdvancesAccumulate() {
        SimClock clock = new SimClock(0);
        clock.advance(100);
        clock.advance(200);
        clock.advance(300);
        assertEquals(600, clock.now());
    }

    @Test
    void startsAtZero() {
        SimClock clock = new SimClock(0);
        assertEquals(0, clock.now());
    }
}

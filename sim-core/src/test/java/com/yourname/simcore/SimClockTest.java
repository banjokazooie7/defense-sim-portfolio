package com.yourname.simcore;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SimClockTest{

	@Test
	void advancesTime(){
		SimClock clock = new SimClock(1000);
		clock.advance(250);
		assertEquals(1250, clock.now());
	}

	@Test
	void rejectsNegativeAdvance(){
		SimClock clock = new SimClock(0);
		assertThrows(IllegalArgumentException.class, () -> clock.advance(-1));
	}
}

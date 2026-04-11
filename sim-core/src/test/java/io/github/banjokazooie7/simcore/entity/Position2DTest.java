package io.github.banjokazooie7.simcore.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Position2DTest{
	private static final double EPSILON = 1e-9;

	void distanceToSelf_isZero(){
		Position2D p = new Position2D(100.0, 200.0);
		assertEquals(0.0, p.distanceTo(p), EPSILON);
	}
	
	void distanceToOrigin_matchesPythagoras(){
		Position2D origin = new Position2D(0, 0);
		Position2D point = new Position2D(3.0, 4.0);
		assertEquals(5.0, origin.distanceTo(point), EPSILON);
	}

	void distanceIsSymmetric(){
		Position2D a = new Position2D(10, 20);
		Position2D b = new Position2D(40, 60);
		assertEquals(a.distanceTo(b), b.distanceTo(a), EPSILON);
	}
	
	void translateCreatesNewPosition(){
		Position2D original = new Position2D(100, 200);
		Position2D moved = original.translate(50, -30);

		//Original remains unchanged
		assertEquals(100.0, original.xMeters(), EPSILON);
		assertEquals(200.0, original.yMeters(), EPSILON);

		//New position is offset
		assertEquals(150.0, moved.xMeters(), EPSILON);
		assertEquals(170.0, moved.yMeters(), EPSILON);
	}

	void translateByZero_returnSameCoords(){
		Position2D p = new Position2D(42, 99);
		Position2D same = p.translate(0, 0);
		assertEquals(p.xMeters(), same.xMeters(), EPSILON);
		assertEquals(p.yMeters(), same.yMeters(), EPSILON);
	}

	void recordEquality_worksAsExpected(){
		Position2D a = new Position2D(10, 20);
		Position2D b = new Position2D(10, 20);
		assertEquals(a, b);
	}
}

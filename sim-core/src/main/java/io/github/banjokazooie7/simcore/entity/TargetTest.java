package io.github.banjokazooie7.simcore.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TargetTest {
    private static final double EPSILON = 1e-6;

    @Test
    void constructsWithValidParams(){
        Target t = new Target("TGT-001", t.getId());
        assertEquals("TGT-001", t.getId());
        assertEquals(0.0, t.getPosition().xMeters(), EPSILON);
        assertEquals(100.0, t.getVx(), EPSILON);
        assertEquals(50.0, t.getVy(), EPSILON);
    }

    @Test
    void rejectsNullId(){
        assertThrows(IllegalArgumentException.class, () 
            -> new Target(null, new Position2D(0, 0), 0, 0));
    }

    void rejectsBlankId(){
        assertThrows(IllegalArgumentException.class, ()
            -> new Target(" ", new Position2D(0, 0), 0, 0));
    }

    void rejectsNullPosition(){
        assertThrows(IllegalArgumentException.class, ()
            -> new Target("TGT", null, 0, 0));
    }
    
    void updateMovesTarget_eastward(){
        Target t = new Target("T1", new Position2D(0, 0), 100.0, 0.0);
        t.update(1000);
        assertEquals(100.0, t.getPosition().xMeters(), EPSILON);
        assertEquals(0.0, t.getPosition().yMeters(), EPSILON);
    }

    void updateMovesTarget_diagonally(){
        Target t = new Target("T2", new Position2D(0, 0), 30.0, 40.0);
        t.update(2000);
        assertEquals(60.0, t.getPosition().xMeters(), EPSILON);
        assertEquals(80.0, t.getPosition().yMeters(), EPSILON);
    }

    void updateByZero_doesNotMove(){
        Target t = new Target("T3", new Position2D(10, 20), 100.0, 100.0);
        t.update(0);
        assertEquals(10.0, t.getPosition().xMeters(), EPSILON);
        assertEquals(20.0, t.getPosition().yMeters(), EPSILON);
    }

    void multipleUpdatesAccumulate(){
        Target t = new Target("T4", new Position2D(0, 0), 10.0, 0.0);
        t.update(1000);
        t.update(1000);
        t.update(1000);
        assertEquals(30.0, t.getPosition().xMeters(), EPSILON);
    }

    void rejectsNegativeDelta(){
        Target t = new Target("T5", new Position2D(0, 0), 10.0, 10.0);
        assertThrows(IllegalArgumentException.class, () -> t.update(-100));
    }

    void setVelocityChangesDirection(){
        Target t = new Target("T6", new Position2D(0, 0), 100.0, 0.0);
        t.update(1000);
        t.setVelocity(0.0, 100.0);
        t.update(1000);
        assertEquals(100.0, t.getPosition().xMeters(), EPSILON);
        assertEquals(100.0, t.getPosition().yMeters(), EPSILON);
    }

    void getSpeedCalculatesMagnitude(){
        Target t = new Target("T7", new Position2D(0, 0), 3.0, 4.0);
        assertEquals(5.0, t.getSpeed(), EPSILON);
    }

    void stationaryTargetHasZeroSpeed(){
        Target t = new Target("T8", new Position2D(0, 0), 0.0, 0.0);
        assertEquals(0.0, t.getSpeed(), EPSILON);
        t.update(5000);
        assertEquals(0.0, t.getPosition().xMeters(), EPSILON);
    }

    void toStringContainsIdAndPosition(){
        Target t = new Target("TGT-042", new Position2D(100, 200), 10, 20);
        String s = t.toString();
        assertTrue(s.contains("TGT-042"));
        assertTrue(s.contains("100"));
    }
}

package io.github.banjokazooie7.simcore.examples;

import io.github.banjokazooie7.simcore.SimClock;

public final class HelloSimMain {
    public static void main(String[] args) {
        SimClock clock = new SimClock(0);
        clock.advance(250);
        System.out.println("HelloSim: nowMillis=" + clock.now());
    }
}


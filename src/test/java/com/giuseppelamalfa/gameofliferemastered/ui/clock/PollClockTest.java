/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.ui.clock;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author glitchedcode
 */
public class PollClockTest {

    public static double eps = 0.0000001;

    /**
     * Test of pollTicks method, of class PollClock.
     */
    @Test
    public void testPollClock() {
        System.out.println("pollClock");
        PollClock instance = new PollClock(10f);
        assertEquals(true, Math.abs(instance.period - 10f) < eps);
        double delta = 1d;
        int iters = 0;
        while (instance.pollTicks(delta) == 0) {
            ++iters;
            assertEquals(true, Math.abs(instance.pollElapsed(0) - (delta * iters)) < eps);
            assertEquals(true, Math.abs(instance.pollElapsedNormalized(0) - (delta * iters) / 10f) < eps);
        }
        assertEquals(9, iters);
    }

    /**
     * Test of getType method, of class PollSineClock.
     */
    @Test
    public void testGetType() {
        System.out.println("getType");
        PollClock instance = new PollClock(10f);
        assertEquals(PollClock.Type.LINEAR, instance.getType());
    }
}

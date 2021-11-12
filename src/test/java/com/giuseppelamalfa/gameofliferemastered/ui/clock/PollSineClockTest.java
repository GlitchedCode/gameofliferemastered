/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.ui.clock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author glitchedcode
 */
public class PollSineClockTest {

    public static double eps = 0.0000001;

    public PollSineClockTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of pollElapsed method, of class PollSineClock.
     */
    @Test
    public void testPollSine() {
        System.out.println("pollSine");
        double delta = 1.0;
        PollSineClock instance = new PollSineClock(10f);
        int iters = 0;
        assertEquals(true, (instance.pollElapsed(0f) - 0f) < eps);
        assertEquals(true, (instance.pollElapsedNormalized(0f) - 0f) < eps);

        assertEquals(true, (instance.pollElapsedNormalized(10f) - 10f) < eps);
        assertEquals(true, (instance.pollElapsedNormalized(0f) - 1f) < eps);

        assertEquals(true, (instance.pollElapsedNormalized(10f) - 0f) < eps);
        assertEquals(true, (instance.pollElapsedNormalized(0f) - 0f) < eps);

    }

    /**
     * Test of getType method, of class PollSineClock.
     */
    @Test
    public void testGetType() {
        System.out.println("getType");
        PollClock instance = new PollSineClock(10f);
        assertEquals(PollClock.Type.SINE, instance.getType());
    }
}

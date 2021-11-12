/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.ui.colors;

import java.awt.Color;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author glitchedcode
 */
public class AnimatedHSBProviderTest {

    public AnimatedHSBProviderTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of mainColor method, of class AnimatedHSBProvider.
     */
    @Test
    public void testMainColor() throws Exception {
        System.out.println("mainColor");
        AnimatedHSBProvider instance = new AnimatedHSBProvider(0d, 1d, 0d, 1d, 1d, 1d, 1d, 1d, 1d);
        assertEquals(Color.RED.getRGB(), instance.mainColor().getRGB());
    }

    /**
     * Test of update method, of class AnimatedHSBProvider.
     */
    @Test
    public void testUpdate() throws Exception {
        /*System.out.println("update"); SCRIVI UN TEST MIGLIORE
        double delta = 0.0;
        AnimatedHSBProvider instance = new AnimatedHSBProvider(0d, 1d, 1d,
                1d, 1d, 1d, 
                1d, 1d, 1d);
        instance.update(0.5d);
        assertEquals(0x00FFFF, instance.currentColor().getRGB());*/
    }
}

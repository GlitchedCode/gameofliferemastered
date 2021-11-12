/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.ui.colors;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author glitchedcode
 */
public class NormalizedRangeTest {
    
    public NormalizedRangeTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of get method, of class NormalizedRange.
     */
    @Test
    public void testGet() {
        System.out.println("normalizedGet");
        double arg = 0.0;
        NormalizedRange instance = null;
        double expResult = 0.0;
        double result = instance.get(arg);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }    
}

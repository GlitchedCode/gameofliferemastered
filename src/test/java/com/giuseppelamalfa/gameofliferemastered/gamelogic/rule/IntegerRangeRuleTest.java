/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.rule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author glitchedcode
 */
public class IntegerRangeRuleTest {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of test method, of class IntegerRangeRule.
     */
    @Test
    public void testSingleValue() {
        System.out.println("testSingleValue");
        IntegerRangeRule instance = new IntegerRangeRule(2, 2);
        boolean expResult = true;
        boolean result = instance.test(2);
        assertEquals(expResult, result);
    }

    @Test
    public void testInsideRange() {
        System.out.println("testInsideRange");
        IntegerRangeRule instance = new IntegerRangeRule(2, 8);
        boolean expResult = true;
        boolean result = instance.test(4);
        assertEquals(expResult, result);
    }

    @Test
    public void testOutsideRange() {
        System.out.println("testOutsideRange");
        IntegerRangeRule instance = new IntegerRangeRule(2, 8);
        boolean expResult = false;
        boolean result = instance.test(1) | instance.test(9);
        assertEquals(expResult, result);
    }

}

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
public class IntegerSetRuleTest {

    IntegerSetRule rule;

    public IntegerSetRuleTest() {
        rule = new IntegerSetRule(0, 2, 4, 6, 8);
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAcceptedValue() {
        System.out.println("testAcceptedValue");
        assertEquals(rule.test(0) & rule.test(2) & rule.test(4) & rule.test(6) & rule.test(8), true);
    }

    @Test
    public void testRejectedValue() {
        System.out.println("testRejectedValue");
        assertEquals(rule.test(1) | rule.test(3) | rule.test(5) | rule.test(7) | rule.test(9), false);
    }

}

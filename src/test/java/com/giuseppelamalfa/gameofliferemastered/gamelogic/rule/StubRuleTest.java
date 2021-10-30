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
public class StubRuleTest {
    
    public StubRuleTest() {
    }

    /**
     * Test of test method, of class StubRule.
     */
    @Test
    public void testTest() {
        System.out.println("stubRuleTest");
        
        StubRule<Integer> rule = new StubRule<>();
        assertEquals(false, rule.test(0));
        rule = new StubRule<>(false);
        assertEquals(false, rule.test(0));
        rule = new StubRule<>(true);
        assertEquals(true, rule.test(0));
        
    }
    
}

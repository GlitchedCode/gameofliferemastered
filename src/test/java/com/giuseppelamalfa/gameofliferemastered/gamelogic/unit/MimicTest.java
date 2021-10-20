/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.RuleInterface;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author glitchedcode
 */
public class MimicTest {
    
    public MimicTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of initSpeciesData method, of class Mimic.
     */
    @Test
    public void testInitSpeciesData() {
        System.out.println("initSpeciesData");
        SpeciesData data = null;
        Mimic instance = null;
        instance.initSpeciesData(data);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of boardStep method, of class Mimic.
     */
    @Test
    public void testBoardStep() {
        System.out.println("boardStep");
        Unit[] adjacentUnits = null;
        Mimic instance = null;
        instance.boardStep(adjacentUnits);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of attack method, of class Mimic.
     */
    @Test
    public void testAttack() {
        System.out.println("attack");
        int adjacencyPosition = 0;
        Unit unit = null;
        Mimic instance = null;
        boolean expResult = false;
        boolean result = instance.attack(adjacencyPosition, unit);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of update method, of class Mimic.
     */
    @Test
    public void testUpdate() {
        System.out.println("update");
        Mimic instance = null;
        instance.update();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of replicate method, of class Mimic.
     */
    @Test
    public void testReplicate() {
        System.out.println("replicate");
        Unit unit = null;
        Mimic instance = null;
        instance.replicate(unit);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSpeciesID method, of class Mimic.
     */
    @Test
    public void testGetSpeciesID() {
        System.out.println("getSpeciesID");
        Mimic instance = null;
        int expResult = 0;
        int result = instance.getSpeciesID();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFriendlySpecies method, of class Mimic.
     */
    @Test
    public void testGetFriendlySpecies() {
        System.out.println("getFriendlySpecies");
        Mimic instance = null;
        Set<Integer> expResult = null;
        Set<Integer> result = instance.getFriendlySpecies();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getHostileSpecies method, of class Mimic.
     */
    @Test
    public void testGetHostileSpecies() {
        System.out.println("getHostileSpecies");
        Mimic instance = null;
        Set<Integer> expResult = null;
        Set<Integer> result = instance.getHostileSpecies();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFriendlyCountSelector method, of class Mimic.
     */
    @Test
    public void testGetFriendlyCountSelector() {
        System.out.println("getFriendlyCountSelector");
        Mimic instance = null;
        RuleInterface<Integer> expResult = null;
        RuleInterface<Integer> result = instance.getFriendlyCountSelector();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getHostileCountSelector method, of class Mimic.
     */
    @Test
    public void testGetHostileCountSelector() {
        System.out.println("getHostileCountSelector");
        Mimic instance = null;
        RuleInterface<Integer> expResult = null;
        RuleInterface<Integer> result = instance.getHostileCountSelector();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getReproductionSelector method, of class Mimic.
     */
    @Test
    public void testGetReproductionSelector() {
        System.out.println("getReproductionSelector");
        Mimic instance = null;
        RuleInterface<Integer> expResult = null;
        RuleInterface<Integer> result = instance.getReproductionSelector();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of equals method, of class Mimic.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object obj = null;
        Mimic instance = null;
        boolean expResult = false;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of hashCode method, of class Mimic.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        Mimic instance = null;
        int expResult = 0;
        int result = instance.hashCode();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}

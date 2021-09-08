/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.RuleInterface;
import java.awt.image.BufferedImageOp;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author glitchedcode
 */
public class DeadUnitTest {

    static DeadUnit dead = new DeadUnit();

    @BeforeClass
    public static void setUpClass() {
        try {
            SpeciesLoader.loadSpeciesFromLocalJSON("testSpecies.json");
        } catch (Exception ex) {
            Logger.getLogger(LifeUnitTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @AfterClass
    public static void tearDownClass() {
        SpeciesLoader.tearDown();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getBornUnit method, of class DeadUnit.
     */
    @Test
    public void testGetBornUnit() {
        System.out.println("getBornUnit");
        Unit[] notEnoughUnits = {
            SpeciesLoader.getNewUnit(1),
            dead,
            dead,
            dead,
            dead,
            dead,
            dead,
            dead,};
        for (Unit unit : notEnoughUnits) {
            unit.update();
        }

        Unit[] enoughUnits = {
            SpeciesLoader.getNewUnit(1),
            SpeciesLoader.getNewUnit(1),
            SpeciesLoader.getNewUnit(1),
            dead,
            dead,
            dead,
            dead,
            dead,};
        for (Unit unit : enoughUnits) {
            unit.update();
        }

        Unit[] tooManyUnits = {
            SpeciesLoader.getNewUnit(1),
            SpeciesLoader.getNewUnit(1),
            SpeciesLoader.getNewUnit(1),
            SpeciesLoader.getNewUnit(1),
            SpeciesLoader.getNewUnit(1),
            SpeciesLoader.getNewUnit(1),
            SpeciesLoader.getNewUnit(1),
            SpeciesLoader.getNewUnit(1),
        };
        for (Unit unit : tooManyUnits) {
            unit.update();
        }

        assertEquals(true, DeadUnit.getBornUnit(notEnoughUnits) == null);
        assertEquals(true, DeadUnit.getBornUnit(tooManyUnits) == null);
        
        Unit result = DeadUnit.getBornUnit(enoughUnits);
        assertEquals(true, result != null);
    }
}

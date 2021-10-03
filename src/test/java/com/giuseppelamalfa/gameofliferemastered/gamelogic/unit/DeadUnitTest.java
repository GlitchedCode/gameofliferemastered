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
    SpeciesLoader loader = new SpeciesLoader();

    public DeadUnitTest() throws Exception {
        loader.loadSpeciesFromLocalJSON("testSpecies.json");
    }
    
    /**
     * Test of getBornUnit method, of class DeadUnit.
     */
    @Test
    public void testGetBornUnit() {
        System.out.println("getBornUnit");
        Unit[] notEnoughUnits = {
            loader.getNewUnit(1),
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
            loader.getNewUnit(1),
            loader.getNewUnit(1),
            loader.getNewUnit(1),
            dead,
            dead,
            dead,
            dead,
            dead,};
        for (Unit unit : enoughUnits) {
            unit.update();
        }

        Unit[] tooManyUnits = {
            loader.getNewUnit(1),
            loader.getNewUnit(1),
            loader.getNewUnit(1),
            loader.getNewUnit(1),
            loader.getNewUnit(1),
            loader.getNewUnit(1),
            loader.getNewUnit(1),
            loader.getNewUnit(1),
        };
        for (Unit unit : tooManyUnits) {
            unit.update();
        }

        assertEquals(true, dead.getBornUnit(notEnoughUnits, loader) == null);
        assertEquals(true, dead.getBornUnit(tooManyUnits, loader) == null);
        
        Unit result = dead.getBornUnit(enoughUnits, loader);
        assertEquals(true, result != null);
    }
}

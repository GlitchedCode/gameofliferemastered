/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

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
        loader.loadSpeciesFromLocalJSON("species/testSpecies.json");
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

        Unit[] enoughUnits = {
            loader.getNewUnit(1),
            loader.getNewUnit(1),
            loader.getNewUnit(1),
            dead,
            dead,
            dead,
            dead,
            dead,};

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

        assertEquals(true, dead.getBornUnit(notEnoughUnits, loader) == null);
        assertEquals(true, dead.getBornUnit(tooManyUnits, loader) == null);
        
        Unit result = dead.getBornUnit(enoughUnits, loader);
        assertEquals(true, result != null);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.RuleInterface;
import java.awt.Color;
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

    static DeadUnit dead = new DeadUnit();
    SpeciesLoader loader = new SpeciesLoader();

    public MimicTest() throws Exception {
        loader.loadSpeciesFromLocalJSON("species/testSpecies.json");
    }

    /**
     * Test of boardStep method, of class Mimic.
     */
    @Test
    public void testComputeNextTurn() {
        System.out.println("mimicComputeNextTurn");

        Mimic instance = (Mimic) loader.getNewUnit(2);
        Unit[] foreign = {
            loader.getNewUnit(1),
            loader.getNewUnit(1),
            loader.getNewUnit(1),
            loader.getNewUnit(0),
            dead,
            dead,
            dead,
            dead
        };

        instance.computeNextTurn(foreign);
        instance.update();
        assertEquals(true, instance.hasReplicated());

        instance = (Mimic) loader.getNewUnit(2);
        Unit[] friendly = {
            loader.getNewUnit(2),
            dead,
            dead,
            dead,
            dead,
            dead,
            dead,
            dead
        };

        instance.computeNextTurn(friendly);
        instance.update();
        assertEquals(false, instance.hasReplicated());
    }

    /**
     * Test of attack method, of class Mimic.
     */
    @Test
    public void testAttack() {
        System.out.println("mimicAttack");

        Mimic instance = (Mimic) loader.getNewUnit(2);
        Unit[] foreign = {
            loader.getNewUnit(0),
            loader.getNewUnit(0),
            loader.getNewUnit(0),
            loader.getNewUnit(0),
            dead,
            dead,
            dead,
            dead
        };

        assertEquals(true, instance.attack(0, loader.getNewUnit(0)));
        instance.computeNextTurn(foreign);
        instance.update();
        assertEquals(false, instance.attack(0, loader.getNewUnit(0)));
    }

    /**
     * Test of replicate method, of class Mimic.
     */
    @Test
    public void testReplicate() {
        System.out.println("replicate");
        Mimic instance = (Mimic) loader.getNewUnit(2);
        Unit other = loader.getNewUnit(0);
        
        assertEquals(2, instance.getSpeciesID());
        
        instance.replicate(other);
        
        assertEquals(true, instance.hasReplicated());
        assertEquals(other.getSpeciesID(), instance.getSpeciesID());
        assertEquals("cell", loader.getSpeciesTextureCode(instance.getSpeciesID()));
        assertEquals(other.getFriendlySpecies(), instance.getFriendlySpecies());
        assertEquals(other.getHostileSpecies(), instance.getHostileSpecies());
        assertEquals(other.getFriendlyCountSelector(), instance.getFriendlyCountSelector());
        assertEquals(other.getHostileCountSelector(), instance.getHostileCountSelector());
        assertEquals(other.getReproductionSelector(), instance.getReproductionSelector());
        
    }
}

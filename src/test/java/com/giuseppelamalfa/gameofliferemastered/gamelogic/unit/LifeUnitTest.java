/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

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
public class LifeUnitTest {

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

    @Test
    public void testFriendlyRule() {
        System.out.println("testFriendlyRule");

        Unit[] enoughUnits = {
            SpeciesLoader.getNewUnit(1),
            SpeciesLoader.getNewUnit(1),
            SpeciesLoader.getNewUnit(1),
            SpeciesLoader.getNewUnit(0),
            dead,
            dead,
            dead,
            dead
        };
        for (Unit unit : enoughUnits) {
            unit.update();
        }

        Unit[] tooManyUnits = {
            SpeciesLoader.getNewUnit(1),
            SpeciesLoader.getNewUnit(1),
            SpeciesLoader.getNewUnit(1),
            SpeciesLoader.getNewUnit(1),
            SpeciesLoader.getNewUnit(0),
            dead,
            dead,
            dead
        };
        for (Unit unit : tooManyUnits) {
            unit.update();
        }

        Unit[] notEnoughUnits = {
            SpeciesLoader.getNewUnit(1),
            SpeciesLoader.getNewUnit(0),
            dead,
            dead,
            dead,
            dead,
            dead,
            dead
        };
        for (Unit unit : notEnoughUnits) {
            unit.update();
        }

        Unit instance = SpeciesLoader.getNewUnit(1); // snake unit with 2hp
        instance.update();

        int previousHealth = instance.getHealth();
        instance.computeNextTurn(enoughUnits);
        assertEquals(previousHealth == instance.getHealth(), true);

        instance.computeNextTurn(tooManyUnits);
        assertEquals(instance.getHealth() < previousHealth, true);

        previousHealth = instance.getHealth();
        instance.computeNextTurn(notEnoughUnits);
        assertEquals(instance.getHealth() < previousHealth, true);
    }

    @Test
    public void testHostileRule() {
        System.out.println("testHostileRule");

        Unit[] enoughUnits = {
            SpeciesLoader.getNewUnit(0),
            SpeciesLoader.getNewUnit(0),
            SpeciesLoader.getNewUnit(1),
            SpeciesLoader.getNewUnit(1),
            dead,
            dead,
            dead,
            dead
        };
        for(Unit unit: enoughUnits){
            unit.update();
        }

        Unit[] tooManyUnits = {
            SpeciesLoader.getNewUnit(0),
            SpeciesLoader.getNewUnit(0),
            SpeciesLoader.getNewUnit(0),
            SpeciesLoader.getNewUnit(0),
            SpeciesLoader.getNewUnit(1),
            SpeciesLoader.getNewUnit(1),
            dead,
            dead,
        };
        for(Unit unit: tooManyUnits){
            unit.update();
        }

        Unit[] notEnoughUnits = {
            SpeciesLoader.getNewUnit(1),
            SpeciesLoader.getNewUnit(1),
            dead,
            dead,
            dead,
            dead,
            dead,
            dead
        };

        Unit instance = SpeciesLoader.getNewUnit(1); // snake unit with 2hp
        instance.update();
        int previousHealth = instance.getHealth();
        instance.computeNextTurn(enoughUnits);
        assertEquals(previousHealth == instance.getHealth(), true);

        instance.computeNextTurn(tooManyUnits);
        assertEquals(instance.getHealth() < previousHealth, true);

        previousHealth = instance.getHealth();
        instance.computeNextTurn(notEnoughUnits);
        assertEquals(instance.getHealth() < previousHealth, true);
    }

    /**
     * Test of kill method, of class LifeUnit.
     */
    @Test
    public void testKill() {
        System.out.println("kill");
        Unit instance = SpeciesLoader.getNewUnit(0);
        instance.kill();
        assertEquals(instance.isAlive(), false);
    }

    /**
     * Test of isAlive method, of class LifeUnit.
     */
    @Test
    public void testIsAlive() {
        System.out.println("isAlive");
        Unit instance = SpeciesLoader.getNewUnit(0);
        instance.update();
        boolean result = instance.isAlive();
        assertEquals(result, true);
    }

    /**
     * Test of attack method, of class LifeUnit.
     */
    @Test
    public void testAttack() {
        System.out.println("attack");
        LifeUnit snake = (LifeUnit) SpeciesLoader.getNewUnit(1);
        LifeUnit cell = (LifeUnit) SpeciesLoader.getNewUnit(0);
        Unit mimic = SpeciesLoader.getNewUnit(2);
        
        snake.update();
        cell.update();
        mimic.update();
        
        assertEquals(true, cell.attack(0, snake));
        assertEquals(true, snake.attack(0, cell));
        assertEquals(false, snake.attack(0, mimic));

        snake.endStep();
        cell.endStep();

        snake.update();
        cell.update();

        assertEquals(false, cell.isAlive());
        assertEquals(true, snake.isAlive());

    }

    @Test
    public void testIncrementHealth() {
        System.out.println("incrementHealth");
        int increment = 2;
        Unit instance = SpeciesLoader.getNewUnit(1);
        int previous = instance.getHealth();
        instance.incrementHealth(increment);
        assertEquals(true, previous + increment == instance.getHealth());
    }
}

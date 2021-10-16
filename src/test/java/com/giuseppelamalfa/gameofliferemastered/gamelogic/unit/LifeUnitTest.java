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
public class LifeUnitTest {

    static DeadUnit dead = new DeadUnit();
    SpeciesLoader loader = new SpeciesLoader();

    public LifeUnitTest() throws Exception {
        loader.loadSpeciesFromLocalJSON("species/testSpecies.json");    
    }

    @Test
    public void testFriendlyRule() {
        System.out.println("testFriendlyRule");

        Unit[] enoughUnits = {
            loader.getNewUnit(1),
            loader.getNewUnit(1),
            loader.getNewUnit(1),
            loader.getNewUnit(0),
            dead,
            dead,
            dead,
            dead
        };
        for (Unit unit : enoughUnits) {
            unit.update();
        }

        Unit[] tooManyUnits = {
            loader.getNewUnit(1),
            loader.getNewUnit(1),
            loader.getNewUnit(1),
            loader.getNewUnit(1),
            loader.getNewUnit(0),
            dead,
            dead,
            dead
        };
        for (Unit unit : tooManyUnits) {
            unit.update();
        }

        Unit[] notEnoughUnits = {
            loader.getNewUnit(1),
            loader.getNewUnit(0),
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

        Unit instance = loader.getNewUnit(1); // snake unit with 2hp
        instance.update();

        int previousHealth = instance.getHealth();
        instance.computeNextTurn(enoughUnits);
        instance.update();
        assertEquals(previousHealth == instance.getHealth(), true);
        
        instance.computeNextTurn(tooManyUnits);
        assertEquals(true, instance.getHealth() == previousHealth);
        instance.update();
        assertEquals(instance.getHealth() < previousHealth, true);

        previousHealth = instance.getHealth();
        instance.computeNextTurn(notEnoughUnits);
        instance.update();
        assertEquals(instance.getHealth() < previousHealth, true);
    }

    @Test
    public void testHostileRule() {
        System.out.println("testHostileRule");

        Unit[] enoughUnits = {
            loader.getNewUnit(0),
            loader.getNewUnit(0),
            loader.getNewUnit(1),
            loader.getNewUnit(1),
            dead,
            dead,
            dead,
            dead
        };
        for(Unit unit: enoughUnits){
            unit.update();
        }

        Unit[] tooManyUnits = {
            loader.getNewUnit(0),
            loader.getNewUnit(0),
            loader.getNewUnit(0),
            loader.getNewUnit(0),
            loader.getNewUnit(1),
            loader.getNewUnit(1),
            dead,
            dead,
        };
        for(Unit unit: tooManyUnits){
            unit.update();
        }

        Unit[] notEnoughUnits = {
            loader.getNewUnit(1),
            loader.getNewUnit(1),
            dead,
            dead,
            dead,
            dead,
            dead,
            dead
        };

        Unit instance = loader.getNewUnit(1); // snake unit with 2hp
        instance.update();
        int previousHealth = instance.getHealth();
        instance.computeNextTurn(enoughUnits);
        instance.update();
        assertEquals(previousHealth == instance.getHealth(), true);

        instance.computeNextTurn(tooManyUnits);
        assertEquals(true, instance.getHealth() == previousHealth);
        instance.update();
        assertEquals(instance.getHealth() < previousHealth, true);

        previousHealth = instance.getHealth();
        instance.computeNextTurn(notEnoughUnits);
        instance.update();
        assertEquals(instance.getHealth() < previousHealth, true);
    }

    /**
     * Test of kill method, of class LifeUnit.
     */
    @Test
    public void testKill() {
        System.out.println("kill");
        Unit instance = loader.getNewUnit(0);
        instance.kill();
        assertEquals(instance.isAlive(), false);
    }

    /**
     * Test of isAlive method, of class LifeUnit.
     */
    @Test
    public void testIsAlive() {
        System.out.println("isAlive");
        Unit instance = loader.getNewUnit(0);
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
        LifeUnit snake = (LifeUnit) loader.getNewUnit(1);
        LifeUnit cell = (LifeUnit) loader.getNewUnit(0);
        Unit mimic = loader.getNewUnit(2);
        
        snake.update();
        cell.update();
        mimic.update();
        
        //assertEquals(true, cell.attack(0, snake));
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
        Unit[] enoughUnits = {
            loader.getNewUnit(0),
            loader.getNewUnit(0),
            loader.getNewUnit(1),
            loader.getNewUnit(1),
            dead,
            dead,
            dead,
            dead
        };
        for(Unit unit: enoughUnits){
            unit.update();
        }
        
        Unit instance = loader.getNewUnit(1);
        instance.update();
        
        int increment = 2;
        int previous = instance.getHealth();
        instance.incrementHealth(increment);
        
        instance.computeNextTurn(enoughUnits);
        instance.update();
        
        assertEquals(true, previous + increment == instance.getHealth());
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author glitchedcode
 */
public class SpeciesLoaderIT {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        SpeciesLoader.tearDown();
    }

    @Test
    public void testLoadIncorrectSpecies() {
        System.out.println("tesLoadIncorrectSpecies");
        try {
            SpeciesLoader.loadSpeciesFromLocalJSON("badSpecies.json");
        } catch (Exception ex) {
            return;
        }
        fail("SpeciesLoader should throw an exception when loading a malformed file.");

    }

    @Test
    public void testLoadCorrectSpecies() throws Exception {
        System.out.println("testLoadCorrectSpecies");
        try {
            SpeciesLoader.loadSpeciesFromLocalJSON("testSpecies.json");
        } catch (Exception e) {
            fail("SpeciesLoader should not throw an exception when loading a correct file.");
        }
    }
}

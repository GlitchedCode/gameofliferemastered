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
public class SpeciesLoaderIT {

    @Test
    public void testLoadIncorrectSpecies() {
        System.out.println("tesLoadIncorrectSpecies");
        try {
            SpeciesLoader loader = new SpeciesLoader();
            loader.loadSpeciesFromLocalJSON("badSpecies.json");
        } catch (Exception ex) {
            return;
        }
        fail("SpeciesLoader should throw an exception when loading a malformed file.");

    }

    @Test
    public void testLoadCorrectSpecies() throws Exception {
        System.out.println("testLoadCorrectSpecies");
        try {
            SpeciesLoader loader = new SpeciesLoader();
            loader.loadSpeciesFromLocalJSON("testSpecies.json");
        } catch (Exception e) {
            fail("SpeciesLoader should not throw an exception when loading a correct file.");
        }
    }
    
    @Test
    public void testCorrectSpeciesTexture() throws Exception {
        System.out.println("testCorrectSpeciesTexture");
        try {
            SpeciesLoader loader = new SpeciesLoader();            
            loader.loadSpeciesFromLocalJSON("testSpecies.json");
        } catch (Exception e) {
            fail("Exception thrown during test.");
        }
    }
}

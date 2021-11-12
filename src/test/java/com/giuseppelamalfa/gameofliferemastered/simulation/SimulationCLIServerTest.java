/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.simulation;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.PlayerData;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.grid.GameMode;
import com.giuseppelamalfa.gameofliferemastered.simulation.request.Request;
import java.util.HashSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author glitchedcode
 */
public class SimulationCLIServerTest {

    public SimulationCLIServerTest() {
    }

    /**
     * Test of reloadSpeciesConf method, of class SimulationCLIServer.
     */
    @Test
    public void testReloadSpeciesConf() throws Exception {
        System.out.println("reloadSpeciesConf");
        SimulationCLIServer instance = new SimulationCLIServer(10, 10);
        instance.reloadSpeciesConf("species/testSpecies.json");
        assertEquals("Mimic", instance.getSpeciesLoader().getSpeciesData(2).name);
    }

    /**
     * Test of isRemoteInstance method, of class SimulationCLIServer.
     */
    @Test
    public void testIsRemoteInstance() throws Exception {
        System.out.println("isRemoteInstance");
        assertEquals(false, new SimulationCLIServer(10, 10).isRemoteInstance());
        SimulationCLIServer remote = new SimulationCLIServer(7777, 4, 10, 10, GameMode.SANDBOX);
        assertEquals(true, remote.isRemoteInstance());
        remote.close();
    }

    /**
     * Test of getNextClientID method, of class SimulationCLIServer.
     */
    @Test
    public void testGetNextClientID() throws Exception {
        System.out.println("getNextClientID");
        SimulationCLIServer instance = new SimulationCLIServer(10, 10);
        assertEquals(0, instance.getNextClientID());
        assertEquals(1, instance.getNextClientID());
        assertEquals(2, instance.getNextClientID());
    }

    /**
     * Test of extractRandomColor method, of class SimulationCLIServer.
     */
    @Test
    public void testExtractRandomColor() throws Exception {
        System.out.println("extractRandomColor");
        SimulationCLIServer instance = new SimulationCLIServer(10,10);
        PlayerData.TeamColor color = instance.extractRandomColor();
        HashSet<PlayerData.TeamColor> extracted = new HashSet<>();
        while (color != PlayerData.TeamColor.NONE){
            assertEquals(false, extracted.contains(color));
            extracted.add(color);
            color = instance.extractRandomColor();
        }
        assertEquals(true, extracted.size() > 0);
    }

    /**
     * Test of getLocalPlayerID method, of class SimulationCLIServer.
     */
    @Test
    public void testGetLocalPlayerID() throws Exception {
        System.out.println("getLocalPlayerID");
        SimulationCLIServer instance = new SimulationCLIServer(10, 10);
        assertEquals(-1, instance.getLocalPlayerID());
    }
    
    /**
     * Test of getUnit method, of class SimulationCLIServer.
     */
    @Test
    public void testSetGetRemoveUnit() throws Exception {
        System.out.println("setGetRemoveUnit");
        SimulationCLIServer instance = new SimulationCLIServer(10,10);
        instance.setUnit(0, 0, instance.getSpeciesLoader().getNewUnit(0));
        assertEquals(false, instance.getUnit(0, 0).isAlive());
        
        // set unit via request
        
        
        // remove unit
    }

    /**
     * Test of computeNextTurn method, of class SimulationCLIServer.
     */
    @Test
    public void testComputeNextTurn() throws Exception {
        System.out.println("computeNextTurn");
        SimulationCLIServer instance = new SimulationCLIServer(10,10);
        assertEquals(0, instance.getCurrentTurn());
        instance.computeNextTurn();
        assertEquals(1, instance.getCurrentTurn());
    }
}

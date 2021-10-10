/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.grid;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.PlayerData;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.SpeciesLoader;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.Unit;
import java.util.ArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author glitchedcode
 */
public class GridTest {

    SpeciesLoader loader = new SpeciesLoader();
    
    public GridTest() throws Exception {
        loader.loadSpeciesFromLocalJSON("species/testSpecies.json");
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getCurrentTurn method, of class Grid.
     */
    @Test
    public void testGetCurrentTurn() throws Exception {
        System.out.println("getCurrentTurn");
        Grid instance = new Grid(64, 64);
        assertEquals(true, instance.getCurrentTurn() == 0);
        instance.computeNextTurn(loader);
        assertEquals(true, instance.getCurrentTurn() == 1);
    }

    @Test
    public void testGetPlayerCount() throws Exception {
        System.out.println("getPlayerCount");
        Grid instance = new Grid(64, 64);
        assertEquals(true, instance.getPlayerCount() == 0);
        for (int i = 1; i < 9; i++) {
            PlayerData player = new PlayerData("lul");
            player.ID = i;
            instance.addPlayer(player);
            assertEquals(true, instance.getPlayerCount() == i);
        }
    }

    /**
     * Test of getPlayerRankings method, of class Grid.
     */
    @Test
    public void testGetPlayerRankings() throws Exception {
        System.out.println("getPlayerRankings");
        Grid instance = new Grid(64, 64);
        PlayerData[] players = {
            new PlayerData(),
            new PlayerData(),
            new PlayerData(),
            new PlayerData()
        };
        for (int i = 0; i < 4; i++) {
            players[i].ID = i;
            instance.addPlayer(players[i]);
            for (int j = 0; j <= i; j++) {
                instance.setUnit(i, j, loader.getNewUnit(0, i));
            }
        }
        ArrayList<PlayerData> result = instance.getPlayerRankings();
        for (int i = 0; i < 3; i++) {
            assertEquals(true, result.get(i).score >= result.get(i + 1).score);
        }
    }

    /**
     * Test of resize method, of class Grid.
     */
    @Test
    public void testResize() throws Exception {
        System.out.println("resize");
        Grid instance = new Grid(128, 128);
        PlayerData player = new PlayerData();
        player.ID = 0;
        instance.addPlayer(player);
        Unit unit = loader.getNewUnit(0, 0);
        instance.setUnit(64, 64, unit);
        assertEquals(unit, instance.getUnit(64, 64));
        instance.resize(64, 64);
        assertEquals(false, instance.getUnit(64, 64).isAlive());
    }
}

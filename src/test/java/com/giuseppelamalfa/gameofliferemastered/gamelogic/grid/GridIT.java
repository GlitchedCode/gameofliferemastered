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
public class GridIT {

    SpeciesLoader loader = new SpeciesLoader();
    Grid grid;
    PlayerData player1;

    public GridIT() throws Exception {
        loader.loadSpeciesFromLocalJSON("testSpecies.json");
    }

    @Before
    public void setUp() throws Exception {
        grid = new Grid(10, 10);
        player1 = new PlayerData("Player1");
        player1.ID = 0;
        grid.addPlayer(player1);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of setUnit, getUnit and removeUnit methods, of class Grid.
     */
    @Test
    public void testSetRemoveUnit() {
        System.out.println("removeUnit");
        int row = 0;
        int col = 0;
        ArrayList<PlayerData> rankings = grid.getPlayerRankings();

        assertEquals(true, rankings.get(0) == player1 & player1.score == 0);

        Unit unit = loader.getNewUnit(0, 0);
        grid.setUnit(row, col, unit);

        assertEquals(true, grid.getUnit(row, col) == unit && unit.isAlive());
        assertEquals(true, rankings.get(0) == player1 & player1.score == 1);

        grid.removeUnit(row, col);

        assertEquals(false, grid.getUnit(row, col).isAlive());
        assertEquals(true, rankings.get(0) == player1 & player1.score == 0);
    }

    /**
     * Test of clearBoard method, of class Grid.
     */
    @Test
    public void testClearBoard() {
        System.out.println("clearBoard");

        for (int i = 1; i < 4; i++) {
            PlayerData player = new PlayerData();
            player.ID = i;

            grid.addPlayer(player);
            for (int j = 0; j < 10; j++) {
                grid.setUnit(i, j, loader.getNewUnit(0,i));
            }
            assertEquals(false, player.score == 0);
        }
        
        grid.clearBoard();
    
        for(PlayerData player : grid.getPlayerRankings()) {
            assertEquals(true, player.score == 0);
        }
    }

    @Test
    public void testRemovePlayer() {
        System.out.println("removePlayer");
        PlayerData player2 = new PlayerData();
        int ID = 1;
        player2.ID = ID;
        grid.addPlayer(player2);
        
        boolean found = false;
        for (PlayerData player : grid.getPlayerRankings()){
            found |= player.ID == 1;
        }
        assertEquals(true, found);
        
        for(int i = 0; i < 10; i++){
            grid.setUnit(i, i, loader.getNewUnit(0,ID));
            assertEquals(true, grid.getUnit(i, i).isAlive());
        }
        
        grid.removePlayer(ID);
        for(int i = 0; i < 10; i++){
            assertEquals(false, grid.getUnit(i, i).isAlive());
        }
        found = false;
        for (PlayerData player : grid.getPlayerRankings()){
            found |= player.ID == 1;
        }
        assertEquals(false, found);
    }

    @Test
    public void testGetUnitScoreIncrement() {
        System.out.println("getUnitScoreIncrement");
        Unit unit = loader.getNewUnit(0);
        assertEquals(0, grid.getUnitScoreIncrement(unit));
        grid.setUnit(0, 0, unit);
        assertEquals(1, grid.getUnitScoreIncrement(unit));
        unit.kill();
        assertEquals(0, grid.getUnitScoreIncrement(unit));
    }

}

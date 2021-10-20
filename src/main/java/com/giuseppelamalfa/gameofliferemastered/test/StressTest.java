/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.test;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.PlayerData;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.grid.Grid;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.SpeciesLoader;
import com.giuseppelamalfa.gameofliferemastered.utils.ConcurrentGrid2DContainer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author glitchedcode
 */
public class StressTest {

    static void loadBigGrid(Grid grid, SpeciesLoader loader, int sideLen) throws Exception {
        grid.resize(sideLen, sideLen);
        grid.clearBoard();

        for (int r = 0; r < sideLen; r++) {
            for (int c = 0; c < sideLen; c++) {
                int val = (int) (Math.random() * 3);
                if (val < 2) {
                    grid.setUnit(r, c, loader.getNewUnit(val, 0));
                }
            }
        }
    }

    public static void main(String args[]) throws Exception {
        int starting = 128;
        int max = 256;
        int rounds = 4;
        
        System.out.println("Measuring turn computation time with grid side length from "
                + starting + " to " + max);
        System.out.println("Core count: " + Grid.PROCESSOR_COUNT + ".\n");

        int diff = max - starting + 1;
        List<Double> totals = new ArrayList<>(diff);
        for(int i = 0; i < diff; i++){
            totals.add(0d);
        }
        
        SpeciesLoader loader = new SpeciesLoader();
        loader.loadSpeciesFromLocalJSON();
        Grid grid = new Grid(1, 1);
        PlayerData data = new PlayerData();
        data.ID = 0;
        grid.addPlayer(data);
        
        
        for (int round = 0; round < rounds; round++) {
            System.out.println("Round " + (round+1) + "\n");
            for (int i = 0; i < diff; i++) {
                int sideLen = starting + i;
                loadBigGrid(grid, loader, sideLen);

                long startTime = System.nanoTime();
                grid.computeNextTurn(loader);
                long endTime = System.nanoTime();
                double millisDuration = (endTime - startTime) / 1_000_000.0;

                totals.set(i, totals.get(i) + millisDuration);
                
                System.out.print(sideLen);
                System.out.print(" ");
                System.out.println(millisDuration);
            }
            System.out.println("\n");
        }
        
        System.out.println("Averages\n");
        for(int i = 0; i < diff; i++){
            System.out.print(starting + i);
            System.out.print(" ");
            System.out.println(totals.get(i) / (double)rounds);
        }


        System.exit(0);
    }
}

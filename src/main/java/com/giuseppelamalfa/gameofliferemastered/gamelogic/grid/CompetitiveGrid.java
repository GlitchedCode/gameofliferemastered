/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.grid;

/**
 *
 * @author glitchedcode
 */
public class CompetitiveGrid extends Grid {

    public final static Integer SIMULATION_PHASE_LENGTH = 80;

    /**
     * Constructor
     *
     * @param rows Number of rows
     * @param cols Number of columns
     * @throws Exception
     */
    public CompetitiveGrid(Integer rows, Integer cols) throws Exception {
        super(rows, cols);
        isLocked = true;
        gameStatus = "Waiting for players...";
    }

    @Override
    public void setRunning(boolean val) {
    }

    @Override
    public synchronized void computeNextTurn() throws Exception {
        advance();
    }
}

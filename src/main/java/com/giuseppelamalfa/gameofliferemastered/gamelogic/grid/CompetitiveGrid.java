/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.grid;

import com.giuseppelamalfa.gameofliferemastered.utils.TimerWrapper;

/**
 *
 * @author glitchedcode
 */
public class CompetitiveGrid extends Grid {

    public final static int SIMULATION_PHASE_LENGTH = 80;
    public final static int PLACEMENT_PHASE_TIME = 60;

    final TimerWrapper timer = new TimerWrapper();
    int currentPhaseNumber = 0;
    int secondsPassed = 0;

    /**
     * Constructor
     *
     * @param rows Number of rows
     * @param cols Number of columns
     * @throws Exception
     */
    public CompetitiveGrid(Integer rows, Integer cols) throws Exception {
        super(rows, cols);
        gameModeName = "Competitive";
        isLocked = true;
        isRunning = false;
        gameStatus = "Waiting for players...";
    }

    @Override
    public void setRunning(boolean val) {
    }

    @Override
    public synchronized void computeNextTurn() throws Exception {
        advance();
        if (getCurrentTurn() % SIMULATION_PHASE_LENGTH == 0) {
            endPhase();
        }
    }

    private void startPhase() {
        currentPhaseNumber++;
        gameStatus = "Running phase " + currentPhaseNumber;

        isRunning = true;
    }

    private void endPhase() {
        isRunning = false;
        isLocked = false;
        timer.scheduleAtFixedRate(() -> {
            int remaining = PLACEMENT_PHASE_TIME - secondsPassed;
            if (remaining == 0) {
                timer.cancel();
                startPhase();
            } else {
                secondsPassed++;
                gameStatus = "Placement: " + remaining + " seconds left.";
            }
        }, 0, 1000);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.grid;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.PlayerData;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.simulation.SimulationInterface;
import com.giuseppelamalfa.gameofliferemastered.utils.TimerWrapper;
import java.io.Serializable;

/**
 *
 * @author glitchedcode
 */
public class CompetitiveGrid extends Grid {

    enum State implements Serializable {

        WAITING {
            @Override
            public void enter(CompetitiveGrid g) {
                g.resetGame();
            }
        },
        GAME_STARTED {
            @Override
            public void enter(CompetitiveGrid g) {
                g.startGame();
            }
        },
        PLACEMENT_PHASE {
            @Override
            public void enter(CompetitiveGrid g) {
                g.endPhase();
            }
        },
        SIMULATION_PHASE {
            @Override
            public void enter(CompetitiveGrid g) {
                g.startPhase();
            }
        };

        public void enter(CompetitiveGrid g) {
        }

    }

    public final static int SIMULATION_PHASE_LENGTH = 80;
    public final static int PLACEMENT_PHASE_TIME = 20;
    public final static int GAME_START_WAIT_TIME = 10;

    int currentPhaseNumber = 0;
    int secondsPassed = 0;
    State currentState;
    boolean showWinner = false;
    boolean started = false;

    private transient TimerWrapper timer = new TimerWrapper();

    /**
     * Constructor
     *
     * @param rows Number of rows
     * @param cols Number of columns
     * @throws Exception
     */
    public CompetitiveGrid(Integer rows, Integer cols) throws Exception {
        super(rows, cols);
        competitive = true;
        gameModeName = "Competitive";
        setState(State.WAITING);
    }

    @Override
    public void setRunning(boolean val) {
    }

    @Override
    public synchronized void computeNextTurn() throws Exception {
        super.computeNextTurn();
        if ( getCurrentTurn() % SIMULATION_PHASE_LENGTH == 0 ) {
            setState(State.PLACEMENT_PHASE);
        }
    }

    @Override
    public boolean showWinner() {
        return showWinner;
    }

    /**
     * Adds a player to the game.
     *
     * @param player
     */
    @Override
    public void addPlayer(PlayerData player) {
        super.addPlayer(player);
        if ( getPlayerCount() == 2 & currentState == State.WAITING ) {
            setState(State.GAME_STARTED);
        }
    }

    /**
     * Removes a player and it's units from the game.
     *
     * @param id player's ID
     */
    @Override
    public void removePlayer(int id) {
        super.removePlayer(id);
        if ( getPlayerCount() <= 2 ) {
            setState(State.WAITING);
        }
    }

    @Override
    public void afterSync() {
        super.afterSync();
        timer = new TimerWrapper();
        setState(currentState);
    }

    private void setState(State state) {
        timer.cancel();
        currentState = state;

        SimulationInterface sim = getSimulation();
        if ( sim != null ) {
            sim.synchronize();
        }

        state.enter(this);
    }

    private void resetGame() {
        isLocked = true;
        isRunning = false;
        started = false;
        gameStatus = "Waiting for players...";
        clearBoard();
    }

    private void startGame() {
        isRunning = false;

        timer.scheduleAtFixedRate(() -> {
            int remaining = GAME_START_WAIT_TIME - secondsPassed;
            if ( remaining <= 0 ) {
                secondsPassed = 0;
                setState(State.PLACEMENT_PHASE);
            }
            else {
                secondsPassed++;
                gameStatus = "Starting in " + remaining + " seconds.";
            }
        }, 0, 1000);
    }

    private void endPhase() {
        isRunning = false;
        isLocked = false;

        if ( !started ) {
            started = true;
        }
        else {
            showWinner = true;
        }
        clearBoard(false);

        timer.scheduleAtFixedRate(() -> {
            int remaining = PLACEMENT_PHASE_TIME - secondsPassed;
            if ( remaining <= 0 ) {
                secondsPassed = 0;
                setState(State.SIMULATION_PHASE);
            }
            else {
                secondsPassed++;
                gameStatus = "Placement: " + remaining + " seconds left.";
            }
        }, 0, 1000);
    }

    private void startPhase() {
        isRunning = true;
        isLocked = true;
        showWinner = false;

        currentPhaseNumber++;
        gameStatus = "Running phase " + currentPhaseNumber;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Object clone() throws CloneNotSupportedException {
        try {
            CompetitiveGrid ret = (CompetitiveGrid) super.clone();
            return ret;
        }
        catch (CloneNotSupportedException e) {
            System.out.println("com.giuseppelamalfa.gameofliferemastered.gamelogic.Grid.clone() failed idk");
            return this;
        }
    }
}

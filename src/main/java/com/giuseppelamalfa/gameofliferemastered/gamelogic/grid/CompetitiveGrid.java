/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.grid;

import com.giuseppelamalfa.gameofliferemastered.ApplicationFrame;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.PlayerData;
import com.giuseppelamalfa.gameofliferemastered.utils.TimerWrapper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.function.Predicate;

/**
 *
 * @author glitchedcode
 */
public class CompetitiveGrid extends Grid {

    enum State implements Serializable {

        WAITING,
        GAME_STARTED,
        PLACEMENT_PHASE,
        SIMULATION_PHASE;

        private static final ArrayList<Predicate<CompetitiveGrid>> PREDICATES
                = new ArrayList<Predicate<CompetitiveGrid>>() {
            {
                add((g) -> {
                    g.resetGame();
                    return true;
                });
                add((g) -> {
                    g.startGame();
                    return true;
                });
                add((g) -> {
                    g.endPhase();
                    return true;
                });
                add((g) -> {
                    g.startPhase();
                    return true;
                });
            }
        };

        public boolean predicate(CompetitiveGrid g) {
            return PREDICATES.get(ordinal()).test(g);
        }

    }

    static public final TimerWrapper globalTimer = new TimerWrapper();

    public final static int SIMULATION_PHASE_LENGTH = 80;
    public final static int PLACEMENT_PHASE_TIME = 60;
    public final static int GAME_START_WAIT_TIME = 20;

    int currentPhaseNumber = 0;
    int secondsPassed = 0;
    State currentState;
    boolean showWinner = false;
    boolean started = false;

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
        setState(State.WAITING);
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
        if (getPlayerCount() == 2 & currentState == State.WAITING) {
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
        if (getPlayerCount() == 1) {
            setState(State.WAITING);
        }
    }

    @Override
    public void afterSync() {
        setState(currentState);
    }

    private void setState(State state) {
        globalTimer.cancel();
        currentState = state;
        ApplicationFrame.forceSynchronize();
        state.predicate(this);
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

        globalTimer.scheduleAtFixedRate(() -> {
            int remaining = GAME_START_WAIT_TIME - secondsPassed;
            if (remaining <= 0) {
                secondsPassed = 0;
                setState(State.PLACEMENT_PHASE);
            } else {
                secondsPassed++;
                gameStatus = "Starting in " + remaining + " seconds.";
            }
        }, 0, 1000);
    }

    private void startPhase() {
        isRunning = true;
        isLocked = true;
        ApplicationFrame.setShowWinner(false);

        currentPhaseNumber++;
        gameStatus = "Running phase " + currentPhaseNumber;
    }

    private void endPhase() {
        isRunning = false;
        isLocked = false;
        
        if (!started) {
            started = true;
        } else {
            ApplicationFrame.setShowWinner(true);
        }
        clearBoard(false);

        globalTimer.scheduleAtFixedRate(() -> {
            int remaining = PLACEMENT_PHASE_TIME - secondsPassed;
            if (remaining <= 0) {
                secondsPassed = 0;
                setState(State.SIMULATION_PHASE);
            } else {
                secondsPassed++;
                gameStatus = "Placement: " + remaining + " seconds left.";
            }
        }, 0, 1000);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Object clone() throws CloneNotSupportedException {
        try {
            CompetitiveGrid ret = (CompetitiveGrid) super.clone();
            return ret;
        } catch (CloneNotSupportedException e) {
            System.out.println("com.giuseppelamalfa.gameofliferemastered.gamelogic.Grid.clone() failed idk");
            return this;
        }
    }
}

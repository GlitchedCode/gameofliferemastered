/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.grid;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.State;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.GameLogicException;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.PlayerData;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.simulation.SimulationInterface;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.*;
import com.giuseppelamalfa.gameofliferemastered.utils.*;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for handling all of game logic and rendering all units to the grid
 *
 * @author glitchedcode
 */
public class Grid implements Serializable, Cloneable {

    public final Integer SECTOR_SIDE_LENGTH = 32;
    protected String gameModeName = "Sandbox";

    private ConcurrentGrid2DContainer<Unit> board;
    private ConcurrentGrid2DContainer<Boolean> sectorFlags;

    protected String gameStatus = "Paused";
    protected boolean isRunning = false;
    protected boolean isLocked = false;
    protected boolean competitive = false;
    protected int syncTurnCount = 40;
    private int turn = 0;

    private Integer rowCount;
    private Integer columnCount;
    private Integer sectorRowCount;
    private Integer sectorColumnCount;

    private boolean unitFoundThisTurn = false;
    private Point topLeftActive;
    private Point bottomRightActive;
    private Point topLeftProcessed;
    private Point bottomRightProcessed;

    private final HashMap<Integer, PlayerData> players = new HashMap<>();
    private ArrayList<PlayerData> orderedPlayers = new ArrayList<>(); // players ordered by their ranking
    private boolean runPlayerIDCheck = false;

    private transient SimulationInterface simulation;
    private transient Lock lock = new ReentrantLock();
    private transient Condition gridStateAccessible = lock.newCondition();
    private transient Boolean modifyingGridState = false;

    private static final DeadUnit deadUnit = new DeadUnit();
    private static final int processorCount = Runtime.getRuntime().availableProcessors();

    /**
     * Constructor
     *
     * @param rows Number of rows
     * @param cols Number of columns
     * @throws Exception
     */
    public Grid(Integer rows, Integer cols) throws Exception {
        rowCount = rows;
        columnCount = cols;
        sectorRowCount = (rows / SECTOR_SIDE_LENGTH) + 1;
        sectorColumnCount = (cols / SECTOR_SIDE_LENGTH) + 1;

        board = new ConcurrentGrid2DContainer<>(rows, cols, deadUnit);
        sectorFlags = new ConcurrentGrid2DContainer<>(sectorRowCount, sectorColumnCount, false);

        topLeftActive = new Point(0, 0);
        bottomRightActive = new Point(cols, rows);

        topLeftProcessed = new Point(topLeftActive);
        bottomRightProcessed = new Point(bottomRightActive);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Object clone() throws CloneNotSupportedException {
        try {
            Grid ret = (Grid) super.clone();
            ret.board = (ConcurrentGrid2DContainer<Unit>) board.clone();
            ret.sectorFlags = (ConcurrentGrid2DContainer<Boolean>) sectorFlags.clone();

            ret.topLeftActive = new Point(0, 0);
            ret.bottomRightActive = new Point(ret.columnCount, ret.rowCount);

            ret.topLeftProcessed = new Point(ret.topLeftActive);
            ret.bottomRightProcessed = new Point(ret.bottomRightActive);
            return ret;
        } catch (CloneNotSupportedException e) {
            System.out.println("com.giuseppelamalfa.gameofliferemastered.gamelogic.Grid.clone() failed idk");
            return this;
        }
    }

    private void awaitGridStateAccessible() {
        lock.lock();
        while (modifyingGridState) {
            try {
                gridStateAccessible.await();
            } catch (InterruptedException ex) {
                Logger.getLogger(Grid.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        lock.unlock();
    }

    private void setModifyingGridState() {
        awaitGridStateAccessible();
        modifyingGridState = true;
    }

    private void unsetModifyingGridState() {
        modifyingGridState = false;
        lock.lock();
        gridStateAccessible.signalAll();
        lock.unlock();
    }

    /*
     * GETTERS AND SETTERS
     */
    public final boolean isRunning() {
        return isRunning;
    }

    public final boolean isLocked() {
        return isLocked;
    }

    public final boolean isCompetitive() {
        return competitive;
    }

    public final String getGameModeName() {
        return gameModeName;
    }

    public final String getStatusString() {
        return gameStatus;
    }

    public final int getCurrentTurn() {
        return turn;
    }

    public final int getRowCount() {
        return rowCount;
    }

    public final int getColumnCount() {
        return columnCount;
    }

    public final int getPlayerCount() {
        return players.size();
    }

    public boolean showWinner() {
        return false;
    }

    /**
     * @return PlayerData objects ordered by score.
     */
    public final ArrayList<PlayerData> getPlayerRankings() {
        return orderedPlayers;
    }

    /**
     * Resizes the playing field, removing any units that would be out of bounds
     * after resizing.
     *
     * @param rows New row count.
     * @param cols New column count.
     * @throws Exception Thrown on failed sanity checks.
     */
    public final void resize(int rows, int cols) throws Exception {
        setModifyingGridState();

        int _sectorRowCount = (rows / SECTOR_SIDE_LENGTH) + 1;
        int _sectorColumnCount = (cols / SECTOR_SIDE_LENGTH) + 1;

        board.resize(rows, cols);
        sectorFlags.resize(_sectorRowCount, _sectorColumnCount);

        rowCount = rows;
        columnCount = cols;
        sectorRowCount = _sectorRowCount;
        sectorColumnCount = _sectorColumnCount;

        unsetModifyingGridState();
        calculateScore();
    }

    /**
     * @param row
     * @param col
     * @return Returns unit at provided location, or null if none is present or
     * if location is out of bounds.
     */
    public final Unit getUnit(int row, int col) {
        return board.get(row, col);
    }

    /**
     * Removes the unit at the given location from the game
     *
     * @param row
     * @param col
     */
    public final void removeUnit(int row, int col) {
        Unit found = board.get(row, col);
        if (found.isAlive()) {
            players.get(found.getPlayerID()).score -= getUnitScoreIncrement(found);
            board.remove(row, col);
            orderPlayersByScore();
        }
    }

    /**
     * Sets an unit to a given location.
     *
     * @param row Row location.
     * @param col Column location.
     * @param unit Unit to be set.
     */
    public final void setUnit(int row, int col, Unit unit) {
        if (unit == null) {
            return;
        }
        if (!players.containsKey(unit.getPlayerID())) {
            return;
        }
        unit.update();
        setToPosition(row, col, unit);
        correctProcessRegion();
    }

    /**
     * Removes all units from the board.
     */
    public final void clearBoard() {
        clearBoard(true);
    }

    public final void clearBoard(boolean orderPlayers) {
        setModifyingGridState();
        if (orderPlayers) {
            players.values().forEach(data -> {
                data.score = 0;
            });

            orderPlayersByScore();
        }
        unsetModifyingGridState();
        board.clear();
    }

    public void setRunning(boolean val) {
        isRunning = val;
        if (val) {
            gameStatus = "Running";
        } else {
            gameStatus = "Paused";
        }
    }

    public void afterSync() {
        //board.setDefaultValue(deadUnit);
        //sectorFlags.setDefaultValue(false);
        lock = new ReentrantLock();
        gridStateAccessible = lock.newCondition();
        modifyingGridState = false;
    }

    /*
     * GAME LOGIC CODE
     */
    private class SectorCoords {

        public final int row;
        public final int col;

        public SectorCoords(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    private void sectorSurviveReproduce(int sectorRow, int sectorColumn) throws GameLogicException {
        awaitGridStateAccessible();
        Point topLeftBoundary = getSectorTopLeftBoundary(sectorRow, sectorColumn);
        Point bottomRightBoundary = getSectorBottomRightBoundary(sectorRow, sectorColumn);

        topLeftBoundary.x = Integer.max(topLeftBoundary.x, topLeftActive.x);
        topLeftBoundary.y = Integer.max(topLeftBoundary.y, topLeftActive.y);
        bottomRightBoundary.x = Integer.min(bottomRightBoundary.x, bottomRightActive.x);
        bottomRightBoundary.y = Integer.min(bottomRightBoundary.y, bottomRightActive.y);

        boolean active = survivalStep(topLeftBoundary, bottomRightBoundary);
        active = active | reproductionStep(topLeftBoundary, bottomRightBoundary);
        sectorFlags.put(sectorRow, sectorColumn, active);
    }

    /**
     * Advances the board's state to the next turn.
     *
     * @throws Exception
     */
    protected final void advance() throws Exception {
        unitFoundThisTurn = false;
        ArrayList<SectorCoords> processedSectors = new ArrayList<>();
        ArrayList<Thread> threads = new ArrayList<>();

        // Loop through every active sector and any adjacent inactive sectors
        // to add them to processedSectors.
        for (int sectorRow = 0; sectorRow < sectorRowCount; sectorRow++) {
            for (int sectorColumn = 0; sectorColumn < sectorColumnCount; sectorColumn++) {
                if (surroundingSectorsActive(sectorRow, sectorColumn)) {
                    processedSectors.add(new SectorCoords(sectorRow, sectorColumn));
                }
            }
        }

        // Divide the sectors we need to process into batches
        // that can be passed to each thread, then start each of them.
        // Additional threads are not started if additional processors are
        // not available or if there are less processed sectors than processor cores
        int sectorsPerThread = processedSectors.size() / processorCount;
        for (int i = 1; i < processorCount; i++) {
            int start = (i + 1) * sectorsPerThread;
            int end = start + sectorsPerThread;
            Thread thread = new Thread(() -> {
                for (int j = start; j < end; j++) {
                    SectorCoords coords = processedSectors.get(j);
                    try {
                        sectorSurviveReproduce(coords.row, coords.col);
                    } catch (GameLogicException ex) {
                        Logger.getLogger(Grid.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            thread.start();

            threads.add(thread);
        }

        // One batch may be executed in the current thread
        for (int i = 0; i < sectorsPerThread; i++) {
            SectorCoords coords = processedSectors.get(i);
            sectorSurviveReproduce(coords.row, coords.col);
        }

        // Join them all because survival and reproduction phases
        // need to be completed on the entire board before moving on.
        for (int i = 0; i < threads.size(); i++) {
            Thread thread = threads.get(i);
            if (thread.isAlive()) {
                thread.join();
            }
        }

        cleanupStep();
        correctProcessRegion();
        orderPlayersByScore();
        runPlayerIDCheck = false;
        turn += 1;

        if (syncTurnCount != 0 & simulation != null) {
            if (turn % syncTurnCount == 0) {
                simulation.synchronize();
            }
        }
    }

    public synchronized void computeNextTurn() throws Exception {
        advance();
    }

    /**
     * Loops through the entire board and recalculates player scores.
     */
    public final void calculateScore() {
        awaitGridStateAccessible();
        players.values().forEach(data -> {
            data.score = 0;
        });

        for (int row = topLeftActive.y; row <= bottomRightActive.y; row++) {
            for (int col = topLeftActive.x; col <= bottomRightActive.x; col++) {
                Unit current = board.get(row, col);
                if (current != null) {
                    if (current.isAlive()) {
                        players.get(current.getPlayerID()).score += getUnitScoreIncrement(current);
                    }
                }
            }
        }
        orderPlayersByScore();
    }

    /**
     * Adds a player to the game.
     *
     * @param player
     */
    public void addPlayer(PlayerData player) {
        setModifyingGridState();
        if (!players.containsKey(player.ID)) {
            players.put(player.ID, player);
        } else {
            PlayerData data = players.get(player.ID);
            if (player.name != null) {
                data.name = player.name;
            }
        }
        unsetModifyingGridState();
        orderPlayersByScore();
    }

    /**
     * Removes a player and it's units from the game.
     *
     * @param id player's ID
     */
    public void removePlayer(int id) {
        setModifyingGridState();
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                Unit unit = getUnit(r, c);
                if (unit != null) {
                    if (unit.getPlayerID() == id) {
                        setToPosition(r, c, null);
                    }
                }
            }
        }
        players.remove(id);
        unsetModifyingGridState();
        orderPlayersByScore();
    }

    /**
     * After this function is called, each unit's player ID is checked, so that
     * units with invalid player IDs will be removed.
     */
    public final void setPlayerIDCheckNextTurn() {
        runPlayerIDCheck = true;
    }

    /**
     * @param ID player's ID
     * @return Player's color, or PlayerData.TeamColor.NONE if not found.
     */
    public final PlayerData.TeamColor getPlayerColor(int ID) {
        try {
            return players.get(ID).color;
        } catch (Exception e) {
            return PlayerData.TeamColor.NONE;
        }
    }

    public final void setSimulation(SimulationInterface iface) {
        simulation = iface;
    }

    public final SimulationInterface getSimulation() {
        return simulation;
    }

    /**
     * Sets a given unit to the given coordinates on the game board
     *
     * @param row column
     * @param col row
     * @param unit unit to be set
     */
    protected final void setToPosition(Integer row, Integer col, Unit unit) {
        Unit previous = board.get(row, col);
        if (previous.getPlayerID() != -1) {
            players.get(previous.getPlayerID()).score -= getUnitScoreIncrement(previous);
            board.remove(row, col);
        }

        if (unit != null) {
            unit.setCompetitive(competitive);

            awaitGridStateAccessible();
            players.get(unit.getPlayerID()).score += getUnitScoreIncrement(unit);
            board.put(row, col, unit);
            moveProcessBoundaryToInclude(row, col);
            sectorFlags.put(row / SECTOR_SIDE_LENGTH, col / SECTOR_SIDE_LENGTH, true);
            orderPlayersByScore();
        }
    }

    /**
     * Checks a given unit and calculates the score to be added to its player.
     *
     * @param unit
     * @return Unit's score increment.
     */
    protected int getUnitScoreIncrement(Unit unit) {
        if (unit.isAlive()) {
            return 1;
        }
        return 0;
    }

    // Helper functions.
    private boolean surroundingSectorsActive(int sectorRow, int sectorColumn) {
        boolean ret = false;
        for (int r = sectorRow - 1; r <= sectorRow + 1; r++) {
            if (r < 0 | r >= sectorRowCount) {
                continue;
            }
            for (int c = sectorColumn - 1; c <= sectorColumn + 1; c++) {
                if (c < 0 | c >= sectorColumnCount) {
                    continue;
                }

                ret = ret | sectorFlags.get(r, c);
            }
        }
        return ret;
    }

    private void moveProcessBoundaryToInclude(Integer row, Integer col) {
        setModifyingGridState();
        if (!unitFoundThisTurn) {
            topLeftProcessed.move(col, row);
            bottomRightProcessed.move(col, row);
            unitFoundThisTurn = true;
            return;
        }

        if (row < topLeftProcessed.y) {
            topLeftProcessed.y = row;
        } else if (row > bottomRightProcessed.y) {
            bottomRightProcessed.y = row;
        }

        if (col < topLeftProcessed.x) {
            topLeftProcessed.x = col;
        } else if (col > bottomRightProcessed.x) {
            bottomRightProcessed.x = col;
        }
        unsetModifyingGridState();
    }

    private void correctProcessRegion() {
        setModifyingGridState();
        topLeftActive.move(Integer.max(topLeftProcessed.x - 1, 0),
                Integer.max(topLeftProcessed.y - 1, 0));

        bottomRightActive.move(Integer.min(bottomRightProcessed.x + 1, columnCount),
                Integer.min(bottomRightProcessed.y + 1, rowCount));
        unsetModifyingGridState();
    }

    private Point getSectorTopLeftBoundary(int sectorRow, int sectorColumn) {
        return new Point(sectorColumn * SECTOR_SIDE_LENGTH, sectorRow * SECTOR_SIDE_LENGTH);
    }

    private Point getSectorBottomRightBoundary(int sectorRow, int sectorColumn) {
        int row = Integer.min(rowCount, (sectorRow + 1) * SECTOR_SIDE_LENGTH - 1);
        int col = Integer.min(columnCount, (sectorColumn + 1) * SECTOR_SIDE_LENGTH - 1);

        return new Point(col, row);
    }

    private Unit[] getUnitsAdjacentToPosition(Integer row, Integer col) {
        Unit[] ret = new Unit[8];

        //top row
        for (int i = 0; i < 3; i++) {
            Unit unit = board.get(row - 1, col + i - 1);
            ret[i] = unit;
        }

        //middle row
        ret[7] = board.get(row, col - 1);
        ret[3] = board.get(row, col + 1);

        //bottom row
        for (int i = 0; i < 3; i++) {
            Unit unit = board.get(row + 1, col - i + 1);
            ret[i + 4] = unit;
        }

        for (int i = 0; i < 8; i++) {
            if (ret[i] == null) {
                ret[i] = deadUnit;
            }
        }

        return ret;
    }

    private void orderPlayersByScore() {
        setModifyingGridState();
        orderedPlayers = new ArrayList<>(players.values());
        orderedPlayers.sort((PlayerData one, PlayerData two) -> {
            int val = two.score - one.score;
            if (val == 0) {
                val = one.ID - two.ID;
            }
            return val;
        });
        unsetModifyingGridState();
    }

    // Next turn computation helper functions.
    private boolean survivalStep(Point topLeftBoundary, Point bottomRightBoundary) throws GameLogicException {
        boolean aliveNextTurn = false;

        for (int row = topLeftBoundary.y; row <= bottomRightBoundary.y; row++) {
            for (int col = topLeftBoundary.x; col <= bottomRightBoundary.x; col++) {
                Unit current = board.get(row, col);
                if (!current.isAlive()) {
                    continue;
                }
                if (runPlayerIDCheck) {
                    if (current.getPlayerID() != -1 & !players.containsKey(current.getPlayerID())) {
                        setToPosition(row, col, null);
                        continue;
                    }
                }

                Unit[] adjacentUnits = getUnitsAdjacentToPosition(row, col);
                //System.out.println("" + col + " " + row + " " + Arrays.toString(adjacentUnits));
                current.computeNextTurn(adjacentUnits);

                // Expand the board's processing area accordingly as we
                // process more units
                if (current.getNextTurnState() != State.DEAD) {
                    moveProcessBoundaryToInclude(row, col);
                    aliveNextTurn = true;
                }
            }
        }

        return aliveNextTurn;
    }

    private boolean reproductionStep(Point topLeftBoundary, Point bottomRightBoundary) {
        boolean aliveNextTurn = false;
        for (int row = topLeftBoundary.y; row <= bottomRightBoundary.y; row++) {
            for (int col = topLeftBoundary.x; col <= bottomRightBoundary.x; col++) {
                Unit unit = board.get(row, col);
                if (unit.isAlive()) {
                    continue;
                }

                Unit[] adjacentUnits = getUnitsAdjacentToPosition(row, col);

                // Run reproduction checks only if there are adjacent alive units
                for (int i = 0; i < 8; i++) {
                    if (adjacentUnits[i].isAlive()) {
                        deadUnit.computeNextTurn(adjacentUnits);
                        break;
                    }
                }

                Unit bornUnit = deadUnit.getBornUnit();

                if (bornUnit != null) {
                    aliveNextTurn = true;
                    setToPosition(row, col, bornUnit);
                    moveProcessBoundaryToInclude(row, col);
                }
            }
        }
        return aliveNextTurn;
    }

    private void cleanupStep() {
        players.values().forEach(data -> {
            data.score = 0;
        });

        ArrayList<Thread> threads = new ArrayList<>();
        int rowsPerBatch = (bottomRightActive.y - topLeftActive.y + 1) / processorCount;
        rowsPerBatch++;

        for (int i = 1; i < processorCount; i++) {
            int offset = topLeftActive.y + i * rowsPerBatch;
            int lastRow = offset + rowsPerBatch;
            Thread thread = new Thread(() -> {
                for (int row = offset; row <= lastRow; row++) {
                    for (int col = topLeftActive.x; col <= bottomRightActive.x; col++) {
                        Unit current = board.get(row, col);
                        if (current.getPlayerID() != -1) {
                            current.update();
                            players.get(current.getPlayerID()).score += getUnitScoreIncrement(current);
                            if (!current.isAlive()) {
                                board.remove(row, col);
                            }
                        }
                    }
                }
            });
            thread.start();
            threads.add(thread);
        }

        int lastRow = topLeftActive.y + rowsPerBatch;
        for (int row = topLeftActive.y; row <= lastRow; row++) {
            for (int col = topLeftActive.x; col <= bottomRightActive.x; col++) {
                Unit current = board.get(row, col);
                if (current.getPlayerID() != -1) {
                    current.update();
                    players.get(current.getPlayerID()).score += getUnitScoreIncrement(current);
                    if (!current.isAlive()) {
                        board.remove(row, col);
                    }
                }
            }
        }

        threads.forEach(thread -> {
            try {
                if (thread.isAlive()) {
                    thread.join();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Grid.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
}

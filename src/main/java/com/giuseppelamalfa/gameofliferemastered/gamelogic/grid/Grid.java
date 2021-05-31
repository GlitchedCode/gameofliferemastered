/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.grid;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.state.State;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.GameLogicException;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.PlayerData;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.*;
import com.giuseppelamalfa.gameofliferemastered.utils.*;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class for handling all of game logic and rendering all units to the grid
 *
 * @author glitchedcode
 */
public class Grid implements Serializable, Cloneable {

    public final Integer SECTOR_SIDE_LENGTH = 32;
    public final String GAMEMODE_NAME = "Sandbox";

    private TwoDimensionalContainer<UnitInterface> board;
    private TwoDimensionalContainer<Boolean> sectorFlags;

    protected String gameStatus = "Paused";
    protected boolean isRunning = false;
    protected boolean isLocked = false;
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

    private final DeadUnit deadUnit;

    private final HashMap<Integer, PlayerData> players = new HashMap<>();
    private ArrayList<PlayerData> orderedPlayers = new ArrayList<>(); // players ordered by their ranking
    private boolean runPlayerIDCheck = false;

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

        board = new TwoDimensionalContainer<>(rows, cols);
        sectorFlags = new TwoDimensionalContainer<>(sectorRowCount, sectorColumnCount, false);
        deadUnit = new DeadUnit();

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
            ret.board = (TwoDimensionalContainer<UnitInterface>) board.clone();
            ret.sectorFlags = (TwoDimensionalContainer<Boolean>) sectorFlags.clone();

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

    /*
     * GETTERS AND SETTERS
     */
    public final boolean isRunning() {
        return isRunning;
    }
    
    public final boolean isLocked() {
        return isLocked;
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
        int _sectorRowCount = (rows / SECTOR_SIDE_LENGTH) + 1;
        int _sectorColumnCount = (cols / SECTOR_SIDE_LENGTH) + 1;

        board.resize(rows, cols);
        sectorFlags.resize(_sectorRowCount, _sectorColumnCount);

        rowCount = rows;
        columnCount = cols;
        sectorRowCount = _sectorRowCount;
        sectorColumnCount = _sectorColumnCount;
        calculateScore();
    }

    /**
     * @param row
     * @param col
     * @return Returns unit at provided location, or null if none is present or
     * if location is out of bounds.
     */
    public final UnitInterface getUnit(int row, int col) {
        return board.get(row, col);
    }

    /**
     * Removes the unit at the given location from the game
     *
     * @param row
     * @param col
     */
    public final void removeUnit(int row, int col) {
        UnitInterface found = board.get(row, col);
        if (found != null) {
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
    public final void setUnit(int row, int col, UnitInterface unit) {
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
        players.values().forEach(data -> {
            data.score = 0;
        });
        orderPlayersByScore();
        board.clear();
    }

    public void setRunning(boolean val) {
        isRunning = val;
        if(val)
            gameStatus = "Running";
        else
            gameStatus = "Paused";
    }


    /*
     * GAME LOGIC CODE
     */
    /**
     * Advances the board's state to the next turn.
     *
     * @throws Exception
     */
    protected synchronized final void advance() throws Exception {
        unitFoundThisTurn = false;
        // Loop through every active sector, and any adjacent inactive sectors
        for (int sectorRow = 0; sectorRow < sectorRowCount; sectorRow++) {
            for (int sectorColumn = 0; sectorColumn < sectorColumnCount; sectorColumn++) {
                if (!surroundingSectorsActive(sectorRow, sectorColumn)) {
                    continue;
                }

                Point topLeftBoundary = getSectorTopLeftBoundary(sectorRow, sectorColumn);
                Point bottomRightBoundary = getSectorBottomRightBoundary(sectorRow, sectorColumn);

                topLeftBoundary.x = Integer.max(topLeftBoundary.x, topLeftActive.x);
                topLeftBoundary.y = Integer.max(topLeftBoundary.y, topLeftActive.y);
                bottomRightBoundary.x = Integer.min(bottomRightBoundary.x, bottomRightActive.x);
                bottomRightBoundary.y = Integer.min(bottomRightBoundary.y, bottomRightActive.y);

                boolean active = nextTurnStateComputationStep(topLeftBoundary, bottomRightBoundary);
                active = active | reproductionStep(topLeftBoundary, bottomRightBoundary);
                sectorFlags.put(sectorRow, sectorColumn, active);
            }
        }

        cleanupStep();
        correctProcessRegion();
        orderPlayersByScore();
        runPlayerIDCheck = false;
        turn += 1;
    }

    public synchronized void computeNextTurn() throws Exception {
        advance();
    }

    /**
     * Loops through the entire board and recalculates player scores.
     */
    public final void calculateScore() {
        players.values().forEach(data -> {
            data.score = 0;
        });

        for (int row = topLeftActive.y; row <= bottomRightActive.y; row++) {
            for (int col = topLeftActive.x; col <= bottomRightActive.x; col++) {
                UnitInterface current = board.get(row, col);
                if (current != null) {
                    players.get(current.getPlayerID()).score += getUnitScoreIncrement(current);
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
    public final void addPlayer(PlayerData player) {
        players.put(player.ID, player);
        orderPlayersByScore();
    }

    /**
     * Removes a player and it's units from the game.
     *
     * @param id player's ID
     */
    public final void removePlayer(int id) {
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                UnitInterface unit = getUnit(r, c);
                if (unit != null) {
                    if (unit.getPlayerID() == id) {
                        setToPosition(r, c, null);
                    }
                }
            }
        }
        players.remove(id);
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

    /**
     * Sets a given unit to the given coordinates on the game board
     *
     * @param row column
     * @param col row
     * @param unit unit to be set
     */
    protected final void setToPosition(Integer row, Integer col, UnitInterface unit) {
        UnitInterface previous = board.get(row, col);
        if (previous != null) {
            players.get(previous.getPlayerID()).score -= getUnitScoreIncrement(previous);
            board.remove(row, col);
        }

        players.get(unit.getPlayerID()).score += getUnitScoreIncrement(unit);
        board.put(row, col, unit);
        moveProcessBoundaryToInclude(row, col);
        sectorFlags.put(row / SECTOR_SIDE_LENGTH, col / SECTOR_SIDE_LENGTH, true);
        orderPlayersByScore();
    }

    /**
     * Checks a given unit and calculates the score to be added to its player.
     *
     * @param unit
     * @return Unit's score increment.
     */
    protected int getUnitScoreIncrement(UnitInterface unit) {
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
    }

    private void correctProcessRegion() {
        topLeftActive.move(Integer.max(topLeftProcessed.x - 1, 0),
                Integer.max(topLeftProcessed.y - 1, 0));

        bottomRightActive.move(Integer.min(bottomRightProcessed.x + 1, columnCount),
                Integer.min(bottomRightProcessed.y + 1, rowCount));
    }

    private Point getSectorTopLeftBoundary(int sectorRow, int sectorColumn) {
        return new Point(sectorColumn * SECTOR_SIDE_LENGTH, sectorRow * SECTOR_SIDE_LENGTH);
    }

    private Point getSectorBottomRightBoundary(int sectorRow, int sectorColumn) {
        int row = Integer.min(rowCount, (sectorRow + 1) * SECTOR_SIDE_LENGTH - 1);
        int col = Integer.min(columnCount, (sectorColumn + 1) * SECTOR_SIDE_LENGTH - 1);

        return new Point(col, row);
    }

    private UnitInterface[] getUnitsAdjacentToPosition(Integer row, Integer col) {
        UnitInterface[] ret = new UnitInterface[8];

        //top row
        for (int i = 0; i < 3; i++) {
            UnitInterface unit = board.get(row - 1, col + i - 1);
            ret[i] = unit;
        }

        //middle row
        ret[7] = board.get(row, col - 1);
        ret[3] = board.get(row, col + 1);

        //bottom row
        for (int i = 0; i < 3; i++) {
            UnitInterface unit = board.get(row + 1, col - i + 1);
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
        orderedPlayers = new ArrayList<>(players.values());
        orderedPlayers.sort((PlayerData one, PlayerData two) -> {
            int val = two.score - one.score;
            if (val == 0) {
                val = one.ID - two.ID;
            }
            return val;
        });
    }

    // Next turn computation helper functions.
    private boolean nextTurnStateComputationStep(Point topLeftBoundary, Point bottomRightBoundary) throws GameLogicException {
        boolean aliveNextTurn = false;

        for (int row = topLeftBoundary.y; row <= bottomRightBoundary.y; row++) {
            for (int col = topLeftBoundary.x; col <= bottomRightBoundary.x; col++) {
                UnitInterface current = board.get(row, col);
                if (current == null) {
                    continue;
                }
                if (runPlayerIDCheck) {
                    if (current.getPlayerID() != -1 & !players.containsKey(current.getPlayerID())) {
                        setToPosition(row, col, null);
                        continue;
                    }
                }

                UnitInterface[] adjacentUnits = getUnitsAdjacentToPosition(row, col);
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
                UnitInterface unit = board.get(row, col);
                if (unit != null) {
                    if (unit.isAlive()) {
                        continue;
                    }
                }

                UnitInterface[] adjacentUnits = getUnitsAdjacentToPosition(row, col);

                deadUnit.computeNextTurn(adjacentUnits);
                UnitInterface bornUnit = deadUnit.getBornUnit();
                deadUnit.update();

                if (bornUnit != null) {
                    //System.out.println("" + col + " " + row + " " + Arrays.toString(adjacentUnits));
                    aliveNextTurn = true;
                    setToPosition(row, col, bornUnit);
                    moveProcessBoundaryToInclude(row, col);
                }
            }
        }
        return aliveNextTurn;
    }

    private void cleanupStep() {
        for (PlayerData data : players.values()) {
            data.score = 0;
        }

        for (int row = topLeftActive.y; row <= bottomRightActive.y; row++) {
            for (int col = topLeftActive.x; col <= bottomRightActive.x; col++) {
                UnitInterface current = board.get(row, col);
                if (current != null) {
                    current.update();
                    players.get(current.getPlayerID()).score += getUnitScoreIncrement(current);
                    if (!current.isAlive()) {
                        board.remove(row, col);
                    }
                }
            }
        }
    }
}

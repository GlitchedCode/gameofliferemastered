/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.grid;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.GameLogicException;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.PlayerData;
import com.giuseppelamalfa.gameofliferemastered.simulation.SimulationInterface;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.*;
import com.giuseppelamalfa.gameofliferemastered.utils.*;
import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    public final static int PROCESSOR_COUNT = Runtime.getRuntime().availableProcessors();
    public final Integer SECTOR_SIDE_LENGTH = 64;

    private ConcurrentGrid2DContainer<Unit> board;
    private ConcurrentGrid2DContainer<Boolean> sectorFlags;

    private int rowCount;
    private int columnCount;
    private int sectorRowCount;
    private int sectorColumnCount;

    private Point topLeftActive;
    private Point bottomRightActive;
    private Point topLeftProcessed;
    private Point bottomRightProcessed;

    int turn = 0; // default accessor needed for testing

    private final ConcurrentHashMap<Integer, PlayerData> players = new ConcurrentHashMap<>();
    private ArrayList<PlayerData> orderedPlayers = new ArrayList<>(); // players ordered by their ranking

    private transient SimulationInterface simulation;
    private transient Lock gridLock = new ReentrantLock();
    private transient Lock turnLock = new ReentrantLock();

    private String gameModeName = "Sandbox";
    private String gameStatus = "Paused";
    private int syncTurnCount = 40;

    protected static final DeadUnit deadUnit = new DeadUnit();
    private transient ExecutorService executor = Executors.newWorkStealingPool(PROCESSOR_COUNT);

    protected boolean isRunning = false;
    protected boolean isLocked = false;
    protected boolean competitive = false;

    /**
     * Constructor
     *
     * @param rows Number of rows
     * @param cols Number of columns
     * @param loader
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

    protected void setGameModeName(String arg) {
        if (arg != null) {
            gameModeName = arg;
        } else {
            gameModeName = "";
        }
    }

    protected void setGameStatus(String arg) {
        if (arg != null) {
            gameStatus = arg;
        } else {
            gameStatus = "";
        }
    }

    protected void setSyncTurnCount(int arg) {
        syncTurnCount = Math.max(0, arg);
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

    public final boolean areGridContentsEqual(Grid other) {
        int rows = getRowCount();
        int cols = getColumnCount();

        if (rows != other.getRowCount() | cols != other.getColumnCount()) {
            return false;
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Unit unit = getUnit(r, c);
                Unit otherUnit = getUnit(r, c);

                if (!unit.isAlive() | !otherUnit.isAlive()) {
                    if (otherUnit.isAlive() != otherUnit.isAlive()) {
                        return false;
                    } else {
                        continue;
                    }
                }

                if (unit != otherUnit) {
                    return false;
                }
            }
        }

        return true;
    }

    public synchronized final void writeBoardToFile(String fileNameStem) throws Exception {
        PrintStream stream = new PrintStream(new File(fileNameStem + ".grid"));
        int rows = getRowCount();
        int cols = getColumnCount();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Unit unit = getUnit(r, c);
                stream.printf("%d ", unit.getActualSpeciesID());
            }
            stream.println("");
        }
    }

    public synchronized final void readBoardFromFile(File file, SpeciesLoader loader, boolean resize) throws Exception {

        Scanner scanner = new Scanner(file);
        List<String> lines = new ArrayList<>();

        int rows = getRowCount();
        int cols = getColumnCount();

        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine());
        }
        if (resize) {
            resize(lines.size(), cols);
            rows = getRowCount();
        }

        clearBoard();
        rows = Math.min(rows, lines.size());
        for (int r = 0; r < rows; r++) {
            Scanner strScanner = new Scanner(new ByteArrayInputStream(lines.get(r).getBytes()));
            int cellsInRow = 0;
            while (strScanner.hasNext()) {
                try {
                    String next = strScanner.next();
                    strScanner.skip(" ");
                    int speciesID = Integer.decode(next);

                    cellsInRow++;
                    if (cellsInRow > cols) {
                        resize(rows, cellsInRow);
                        cols = cellsInRow;
                    }
                    
                    if (speciesID != -1) {
                        Unit newUnit = loader.getNewUnit(speciesID);
                        setUnit(r, cellsInRow - 1, newUnit);
                    }
                } catch (Exception e) {
                    break;
                }
            }
        }
    }

    public synchronized final void readBoardFromFile(File file, SpeciesLoader loader) throws Exception {
        readBoardFromFile(file, loader, false);
    }

    void setAllSectorFlags(boolean arg) {
        for (int r = 0; r < sectorFlags.getRowCount(); r++) {
            for (int c = 0; c < sectorFlags.getColumnCount(); c++) {
                sectorFlags.put(r, c, arg);
            }
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
        gridLock.lock();
        try {
            int _sectorRowCount = (rows / SECTOR_SIDE_LENGTH) + 1;
            int _sectorColumnCount = (cols / SECTOR_SIDE_LENGTH) + 1;

            board.resize(rows, cols);
            sectorFlags.resize(_sectorRowCount, _sectorColumnCount);

            rowCount = rows;
            columnCount = cols;
            sectorRowCount = _sectorRowCount;
            sectorColumnCount = _sectorColumnCount;

        } finally {
            gridLock.unlock();
        }
        calculateScore();
    }

    /**
     * @param row
     * @param col
     * @return Returns unit at provided location, or null if none is present or
     * if location is out of bounds.
     */
    public final Unit getUnit(int row, int col) {
        Unit ret;
        ret = board.get(row, col);
        return ret;
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
            incrementPlayerScore(found.getPlayerID(), -getUnitScoreIncrement(found));
            board.remove(row, col);
            moveProcessBoundaryToInclude(row, col);
            touchSector(row / SECTOR_SIDE_LENGTH, col / SECTOR_SIDE_LENGTH);
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
    public final void setUnit(int row, int col, Unit unit) throws GameLogicException {
        turnLock.lock();
        try {
            if (unit == null) {
                return;
            }
            if (!players.containsKey(unit.getPlayerID())) {
                return;
            }
            //unit.update();
            setToPosition(row, col, unit);
        } finally {
            turnLock.unlock();
        }
    }

    /**
     * Removes all units from the board.
     */
    public final void clearBoard() {
        clearBoard(true);
    }

    public final void clearBoard(boolean orderPlayers) {
        turnLock.lock();
        try {
            if (orderPlayers) {
                players.values().forEach(data -> {
                    data.score = 0;
                });

                orderPlayersByScore();
            }
            board.clear();
        } finally {
            turnLock.unlock();
        }
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
        executor = Executors.newFixedThreadPool(PROCESSOR_COUNT);
        gridLock = new ReentrantLock();
        turnLock = new ReentrantLock();
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

    private void sectorSurviveReproduce(int sectorRow, int sectorColumn, SpeciesLoader speciesLoader) throws GameLogicException {
        gridLock.lock();
        Point topLeftBoundary = getSectorTopLeftBoundary(sectorRow, sectorColumn);
        Point bottomRightBoundary = getSectorBottomRightBoundary(sectorRow, sectorColumn);
        try {
            topLeftBoundary.x = Integer.max(topLeftBoundary.x, topLeftActive.x);
            topLeftBoundary.y = Integer.max(topLeftBoundary.y, topLeftActive.y);
            bottomRightBoundary.x = Integer.min(bottomRightBoundary.x, bottomRightActive.x);
            bottomRightBoundary.y = Integer.min(bottomRightBoundary.y, bottomRightActive.y);
        } finally {
            gridLock.unlock();
        }
        boolean active = survivalStep(topLeftBoundary, bottomRightBoundary);
        active = active | reproductionStep(topLeftBoundary, bottomRightBoundary, speciesLoader);
        sectorFlags.put(sectorRow, sectorColumn, active);
    }

    private void startBatchSurviveReproduce(ConcurrentLinkedQueue<SectorCoords> processedSectors, SpeciesLoader speciesLoader) throws GameLogicException, InterruptedException {
        // Divide the sectors we need to process into batches
        // that can be passed to each thread, then start each of them.
        // Additional threads are not started if additional processors are
        // not available.
        int spawnedThreads = Integer.min(PROCESSOR_COUNT, processedSectors.size());
        List<Callable<Object>> tasks = new ArrayList<>();
        for (int i = 0; i < spawnedThreads; i++) {
            tasks.add(() -> {
                while (!processedSectors.isEmpty()) {
                    SectorCoords coords = processedSectors.poll();
                    try {
                        if (coords != null) {
                            sectorSurviveReproduce(coords.row, coords.col, speciesLoader);
                        }
                    } catch (GameLogicException ex) {
                        Logger.getLogger(Grid.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                return null;
            });
        }

        executor.invokeAll(tasks);
    }

    /**
     * Advances the board's state to the next turn.
     *
     * @throws Exception
     */
    private void advance(SpeciesLoader speciesLoader) throws Exception {
        turnLock.lock();
        try {
            ConcurrentLinkedQueue<SectorCoords> processedSectors = new ConcurrentLinkedQueue<>();

            // Loop through every active sector and any adjacent inactive sectors
            // to add them to processedSectors.
            for (int sectorRow = 0; sectorRow < sectorRowCount; sectorRow++) {
                for (int sectorColumn = 0; sectorColumn < sectorColumnCount; sectorColumn++) {
                    if (surroundingSectorsActive(sectorRow, sectorColumn)) {
                        processedSectors.add(new SectorCoords(sectorRow, sectorColumn));
                    }
                }
            }

            if (processedSectors.size() > 0) {
                startBatchSurviveReproduce(processedSectors, speciesLoader);
            }

            cleanupStep();
            orderPlayersByScore();
            turn += 1;

            if (syncTurnCount != 0 & simulation != null) {
                if (turn % syncTurnCount == 0) {
                    simulation.synchronize();
                }
            }
        } finally {
            turnLock.unlock();
        }
    }

    public void computeNextTurn(SpeciesLoader speciesLoader) throws Exception {
        advance(speciesLoader);
    }

    /**
     * Loops through the entire board and recalculates player scores.
     */
    public final void calculateScore() {
        gridLock.lock();
        try {
            players.values().forEach(data -> {
                data.score = 0;
            });

            for (int row = topLeftActive.y; row <= bottomRightActive.y; row++) {
                for (int col = topLeftActive.x; col <= bottomRightActive.x; col++) {
                    Unit current = board.get(row, col);
                    if (current != null) {
                        if (current.isAlive()) {
                            incrementPlayerScore(current.getPlayerID(), getUnitScoreIncrement(current));
                        }
                    }
                }
            }
        } finally {
            gridLock.unlock();
        }
        orderPlayersByScore();
    }

    /**
     * Adds a player to the game.
     *
     * @param player
     */
    public void addPlayer(PlayerData player) {
        gridLock.lock();
        try {
            if (!players.containsKey(player.ID)) {
                players.put(player.ID, player);
            } else {
                PlayerData data = players.get(player.ID);
                if (player.name != null) {
                    data.name = player.name;
                }
            }
        } finally {
            gridLock.unlock();
        }
        orderPlayersByScore();
    }

    /**
     * Removes a player and it's units from the game.
     *
     * @param id player's ID
     * @return true if player ID is found, false otherwise
     */
    public boolean removePlayer(int id) {
        gridLock.lock();
        boolean ret = false;
        try {
            if (players.containsKey(id)) {
                /* not needed
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
                 */
                players.remove(id);
                ret = true;
            }
        } finally {
            gridLock.unlock();
        }
        orderPlayersByScore();
        return ret;
    }

    /**
     * After this function is called, each unit's player ID is checked, so that
     * units with invalid player IDs will be removed.
     */
    /* unneeded
    public final void setPlayerIDCheckNextTurn() {
        runPlayerIDCheck = true;
    }
     */
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

    void incrementPlayerScore(int playerID, int increment) {
        PlayerData player = players.get(playerID);
        if (player != null) {
            player.score += increment;
        }
    }

    protected final void touchSector(int row, int col) {
        sectorFlags.put(row, col, true);
        sectorFlags.put(row, col - 1, true);
        sectorFlags.put(row - 1, col, true);
        sectorFlags.put(row - 1, col - 1, true);
        sectorFlags.put(row, col + 1, true);
        sectorFlags.put(row + 1, col, true);
        sectorFlags.put(row + 1, col + 1, true);
        sectorFlags.put(row - 1, col + 1, true);
        sectorFlags.put(row + 1, col - 1, true);
    }

    /**
     * Sets a given unit to the given coordinates on the game board
     *
     * @param row column
     * @param col row
     * @param unit unit to be set
     */
    protected final void setToPosition(Integer row, Integer col, Unit unit) throws GameLogicException {
        Unit previous = board.get(row, col);
        if (previous.isAlive() | previous.getPlayerID() != -1) {
            throw new GameLogicException(unit, "setToPosition should only be called on non-empty cells");
            //incrementPlayerScore(previous.getPlayerID(), -getUnitScoreIncrement(previous));
            //board.remove(row, col);
        }

        if (unit != null) {
            unit.setCompetitive(competitive);

            incrementPlayerScore(unit.getPlayerID(), getUnitScoreIncrement(unit));
            board.put(row, col, unit);

            moveProcessBoundaryToInclude(row, col);
            touchSector(row / SECTOR_SIDE_LENGTH, col / SECTOR_SIDE_LENGTH);
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
        gridLock.lock();
        try {

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
        } finally {
            gridLock.unlock();
        }

        correctProcessRegion();
    }

    private void correctProcessRegion() {
        gridLock.lock();
        try {
            topLeftActive.move(Integer.max(topLeftProcessed.x - 1, 0),
                    Integer.max(topLeftProcessed.y - 1, 0));

            bottomRightActive.move(Integer.min(bottomRightProcessed.x + 1, columnCount),
                    Integer.min(bottomRightProcessed.y + 1, rowCount));
        } finally {
            gridLock.unlock();
        }
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
        gridLock.lock();
        try {
            orderedPlayers = new ArrayList<>(players.values());
            orderedPlayers.sort((PlayerData one, PlayerData two) -> {
                int val = two.score - one.score;
                if (val == 0) {
                    val = one.ID - two.ID;
                }
                return val;
            });
        } finally {
            gridLock.unlock();
        }
    }

    // Next turn computation helper functions.
    private boolean survivalStep(Point topLeftBoundary, Point bottomRightBoundary) throws GameLogicException {
        boolean activeNextTurn = false;

        for (int row = topLeftBoundary.y; row <= bottomRightBoundary.y; row++) {
            for (int col = topLeftBoundary.x; col <= bottomRightBoundary.x; col++) {
                Unit current = board.get(row, col);
                if (!current.isAlive()) {
                    continue;
                }

                Unit[] adjacentUnits = getUnitsAdjacentToPosition(row, col);
                //System.out.println("" + col + " " + row + " " + Arrays.toString(adjacentUnits));
                current.computeNextTurn(adjacentUnits);

                // Expand the board's processing area accordingly as we
                // process more units
                if (current.isStateChanged()) {
                    moveProcessBoundaryToInclude(row, col);
                    activeNextTurn = true;
                }
            }
        }

        return activeNextTurn;
    }

    private boolean reproductionStep(Point topLeftBoundary, Point bottomRightBoundary, SpeciesLoader speciesLoader) throws GameLogicException {
        boolean activeNextTurn = false;
        for (int row = topLeftBoundary.y; row <= bottomRightBoundary.y; row++) {
            for (int col = topLeftBoundary.x; col <= bottomRightBoundary.x; col++) {
                Unit unit = board.get(row, col);
                if (unit.isAlive()) {
                    continue;
                }

                Unit[] adjacentUnits = getUnitsAdjacentToPosition(row, col);
                Unit bornUnit = null;

                // Run reproduction checks only if there are adjacent alive units
                for (int i = 0; i < 8; i++) {
                    if (adjacentUnits[i].isAlive()) {
                        bornUnit = deadUnit.getBornUnit(adjacentUnits, speciesLoader);
                        break;
                    }
                }

                if (bornUnit != null) {
                    activeNextTurn = true;
                    setToPosition(row, col, bornUnit);
                    moveProcessBoundaryToInclude(row, col);
                }
            }
        }
        return activeNextTurn;
    }

    private void cleanupRows(int firstRow, int lastRow) {
        for (int row = firstRow; row < lastRow; row++) {
            for (int col = topLeftActive.x; col <= bottomRightActive.x; col++) {
                Unit current = board.get(row, col);
                if (current.getPlayerID() != -1) {
                    current.update();
                    incrementPlayerScore(current.getPlayerID(), getUnitScoreIncrement(current));
                    if (!current.isAlive()) {
                        board.remove(row, col);
                    }
                }
            }
        }
    }

    private void cleanupStep() {
        gridLock.lock();
        try {
            players.values().forEach(data -> {
                data.score = 0;
            });

            int rows = bottomRightActive.y - topLeftActive.y;
            int threadCount = Integer.min(rows, PROCESSOR_COUNT);
            int rowsPerBatch = rows / threadCount;
            rowsPerBatch++;

            List<Callable<Object>> tasks = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                int offset = topLeftActive.y + i * rowsPerBatch;
                int lastRow = offset + rowsPerBatch;
                tasks.add(() -> {
                    cleanupRows(offset, lastRow);
                    return null;
                });
            }

            try {
                executor.invokeAll(tasks);
            } catch (InterruptedException ex) {
                Logger.getLogger(Grid.class.getName()).log(Level.SEVERE, null, ex);
            }

            for (int r = 0; r < sectorFlags.getRowCount(); r++) {
                for (int c = 0; c < sectorFlags.getColumnCount(); c++) {
                    if (sectorFlags.get(r, c)) {
                        touchSector(r, c);
                    }
                }
            }
        } finally {
            gridLock.unlock();
        }
    }

    static void fillRandomGrid(Random rng, Grid grid, SpeciesLoader loader, int sideLen) throws Exception {
        grid.resize(sideLen, sideLen);
        grid.clearBoard();

        for (int r = 0; r < sideLen; r++) {
            for (int c = 0; c < sideLen; c++) {
                int val = (int) (rng.nextFloat() * 3);
                if (val < 2) {
                    grid.setUnit(r, c, loader.getNewUnit(val, 0));
                }
            }
        }
    }

    static void copyToGrid(Grid src, Grid dst) throws GameLogicException {
        int rows = Math.min(src.getRowCount(), dst.getRowCount());
        int cols = Math.min(src.getColumnCount(), dst.getColumnCount());

        dst.clearBoard();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Unit srcUnit = src.getUnit(r, c);
                if (srcUnit.isAlive()) {
                    Unit dstUnit = (Unit) srcUnit.clone();
                    dst.setUnit(r, c, dstUnit);
                }
            }
        }
    }

    public static void main(String args[]) throws Exception {
        int starting = 128;
        int max = 256;
        int rounds = 10;
        Random rng = new Random(0xdeadbeef);

        System.out.println("Measuring turn computation time with grid side length from "
                + starting + " to " + max);
        System.out.println("Core count: " + Grid.PROCESSOR_COUNT + ".\n");

        int diff = max - starting + 1;
        List<Double> totals = new ArrayList<>(diff);
        for (int i = 0; i < diff; i++) {
            totals.add(0d);
        }

        SpeciesLoader loader = new SpeciesLoader();
        loader.loadSpeciesFromLocalJSON();
        Grid randGrid = new Grid(1, 1);
        Grid grid = new Grid(1, 1);
        PlayerData data = new PlayerData();
        data.ID = 0;
        randGrid.addPlayer(data);
        grid.addPlayer(data);

        for (int round = 0; round < rounds; round++) {
            System.out.println("Round " + (round + 1) + "\n");
            fillRandomGrid(rng, randGrid, loader, max);

            for (int i = 0; i < diff; i++) {
                int sideLen = starting + i;
                grid.resize(sideLen, sideLen);
                copyToGrid(randGrid, grid);

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
        for (int i = 0; i < diff; i++) {
            System.out.print(starting + i);
            System.out.print(" ");
            System.out.println(totals.get(i) / (double) rounds);
        }

        System.exit(0);
    }
}

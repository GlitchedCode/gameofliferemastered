/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.*;
import com.giuseppelamalfa.gameofliferemastered.utils.*;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Class for handling all of game logic and rendering all units to the grid
 *
 * @author glitchedcode
 */
public class Grid implements Serializable, Cloneable
{
    private TwoDimensionalContainer<UnitInterface> board;
    private TwoDimensionalContainer<Boolean> sectorFlags;

    private int turn = 0;

    public final Integer SECTOR_SIDE_LENGTH = 32;
    private Integer rowCount;
    private Integer columnCount;
    private Integer sectorRowCount;
    private Integer sectorColumnCount;

    private boolean unitFoundThisTurn = false;
    private final Point topLeftActive;
    private final Point bottomRightActive;
    private final Point topLeftProcessed;
    private final Point bottomRightProcessed;

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
    public Grid(Integer rows, Integer cols) throws Exception    {
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
    @SuppressWarnings({"unchecked", "unchecked"})
    public Object clone() {
        try {
            Grid ret = (Grid) super.clone();
            ret.board = (TwoDimensionalContainer<UnitInterface>)board.clone();
            ret.sectorFlags = (TwoDimensionalContainer<Boolean>)sectorFlags.clone();
            return ret;
        } catch (CloneNotSupportedException e) {
            System.out.println("com.giuseppelamalfa.gameofliferemastered.gamelogic.Grid.clone() failed idk");
            return this;
        }
    }
    
    /*
    * GETTERS AND SETTERS
     */
    public int          getCurrentTurn() {
        return turn;
    }

    /**
     * @return the number of the board's columns
     */
    public int getRowCount()    {
        return rowCount;
    }

    /**
     * @return the number of the board's columns
     */
    public int getColumnCount()    {
        return columnCount;
    }
    
    public void resize(int rows, int cols) throws Exception {
        rowCount = rows;
        columnCount = cols;
        sectorRowCount = (rows / SECTOR_SIDE_LENGTH) + 1;
        sectorColumnCount = (cols / SECTOR_SIDE_LENGTH) + 1;

        board.resize(rows, cols);
        sectorFlags.resize(sectorRowCount, sectorColumnCount);
        
    }
    
    public UnitInterface getUnit(int row, int col)    {
        return board.get(row, col);
    }

    public void setUnit(int row, int col, UnitInterface unit)
    {
        if(!players.containsKey(unit.getPlayerID())) return;
        unit.update();
        setToPosition(row, col, unit);
        correctProcessRegion();
    }
    
    public final int getSectorSideLength()    {
        return SECTOR_SIDE_LENGTH;
    }

    public void clearBoard()
    {
        board.clear();
    }

    /*
    * GAME LOGIC CODE
     */
    private boolean surroundingSectorsActive(int sectorRow, int sectorColumn)
    {
        boolean ret = false;

        for (int r = sectorRow - 1; r <= sectorRow + 1; r++)
        {
            if ( r < 0 | r >= sectorRowCount )
            {
                continue;
            }

            for (int c = sectorColumn - 1; c <= sectorColumn + 1; c++)
            {
                if ( c < 0 | c >= sectorColumnCount )
                {
                    continue;
                }

                ret = ret | sectorFlags.get(r, c);
            }
        }

        return ret;
    }

    /**
     * Advances the game state to the next turn
     *
     * @throws java.lang.Exception
     */
    public synchronized void computeNextTurn() throws Exception
    {
        unitFoundThisTurn = false;

        for(PlayerData data : players.values())
            data.score = 0;
        
        for (int sectorRow = 0; sectorRow < sectorRowCount; sectorRow++)
        {
            for (int sectorColumn = 0; sectorColumn < sectorColumnCount; sectorColumn++)
            {
                if ( !surroundingSectorsActive(sectorRow, sectorColumn) )
                    continue;

                Point topLeftBoundary = getSectorTopLeftBoundary(sectorRow, sectorColumn);
                Point bottomRightBoundary = getSectorBottomRightBoundary(sectorRow, sectorColumn);

                topLeftBoundary.x = Integer.max(topLeftBoundary.x, topLeftActive.x);
                topLeftBoundary.y = Integer.max(topLeftBoundary.y, topLeftActive.y);
                bottomRightBoundary.x = Integer.min(bottomRightBoundary.x, bottomRightActive.x);
                bottomRightBoundary.y = Integer.min(bottomRightBoundary.y, bottomRightActive.y);
                /*
                System.out.println("sector " + sectorColumn + " " + sectorRow);

                System.out.println(topLeftBoundary);
                System.out.println(bottomRightBoundary);
                 */
                
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

        //System.out.println("Turn " + turn);
    }

    private void moveProcessBoundaryToInclude(Integer row, Integer col)
    {
        if ( !unitFoundThisTurn )
        {
            topLeftProcessed.move(col, row);
            bottomRightProcessed.move(col, row);
            unitFoundThisTurn = true;
            return;
        }

        if ( row < topLeftProcessed.y )
        {
            topLeftProcessed.y = row;
        }
        else if ( row > bottomRightProcessed.y )
        {
            bottomRightProcessed.y = row;
        }

        if ( col < topLeftProcessed.x )
        {
            topLeftProcessed.x = col;
        }
        else if ( col > bottomRightProcessed.x )
        {
            bottomRightProcessed.x = col;
        }

    }

    private void correctProcessRegion()
    {
        topLeftActive.move(Integer.max(topLeftProcessed.x - 1, 0),
                Integer.max(topLeftProcessed.y - 1, 0));

        bottomRightActive.move(Integer.min(bottomRightProcessed.x + 1, columnCount),
                Integer.min(bottomRightProcessed.y + 1, rowCount));
    }

    /**
     * Sets a given unit to the given coordinates on the game board
     *
     * @param row column
     * @param col row
     * @param unit unit to be set
     */
    protected final void setToPosition(Integer row, Integer col, UnitInterface unit)
    {
        if ( board.get(row, col) == null )
        {
            board.put(row, col, unit);
            moveProcessBoundaryToInclude(row, col);
            sectorFlags.put(row / SECTOR_SIDE_LENGTH, col / SECTOR_SIDE_LENGTH, true);
        }
        else
        {
            board.remove(row, col);
        }
    }

    private Point getSectorTopLeftBoundary(int sectorRow, int sectorColumn)
    {
        return new Point(sectorColumn * SECTOR_SIDE_LENGTH, sectorRow * SECTOR_SIDE_LENGTH);
    }

    private Point getSectorBottomRightBoundary(int sectorRow, int sectorColumn)
    {
        int row = Integer.min(rowCount, (sectorRow + 1) * SECTOR_SIDE_LENGTH - 1);
        int col = Integer.min(columnCount, (sectorColumn + 1) * SECTOR_SIDE_LENGTH - 1);

        return new Point(col, row);
    }

    private UnitInterface[] getUnitsAdjacentToPosition(Integer row, Integer col)
    {
        UnitInterface[] ret = new UnitInterface[8];

        //top row
        for (int i = 0; i < 3; i++)
        {
            UnitInterface unit = board.get(row - 1, col + i - 1);
            ret[i] = unit;
        }

        //middle row
        ret[7] = board.get(row, col - 1);
        ret[3] = board.get(row, col + 1);

        //bottom row
        for (int i = 0; i < 3; i++)
        {
            UnitInterface unit = board.get(row + 1, col - i + 1);
            ret[i + 4] = unit;
        }
        // if there is no unit, just leave null
        return ret;
    }

    private boolean nextTurnStateComputationStep(Point topLeftBoundary, Point bottomRightBoundary) throws GameLogicException
    {
        boolean aliveNextTurn = false;

        for (int row = topLeftBoundary.y; row <= bottomRightBoundary.y; row++)
        {
            for (int col = topLeftBoundary.x; col <= bottomRightBoundary.x; col++)
            {
                UnitInterface current = board.get(row, col);
                if ( current == null )
                    continue;
                if (runPlayerIDCheck)
                    if(current.getPlayerID() != -1 & players.containsKey(current.getPlayerID()))
                        setToPosition(row, col, null);
                
                UnitInterface[] adjacentUnits = getUnitsAdjacentToPosition(row, col);
                //System.out.println("" + col + " " + row + " " + Arrays.toString(adjacentUnits));
                current.computeNextTurn(adjacentUnits);

                // Expand the board's processing area accordingly as we
                // process more units
                if ( current.getNextTurnState() != UnitInterface.State.DEAD )
                {
                    players.get(current.getPlayerID()).score++;
                    moveProcessBoundaryToInclude(row, col);
                    aliveNextTurn = true;
                }
            }
        }

        return aliveNextTurn;
    }

    private boolean reproductionStep(Point topLeftBoundary, Point bottomRightBoundary)
    {
        boolean aliveNextTurn = false;
        for (int row = topLeftBoundary.y; row <= bottomRightBoundary.y; row++)
        {
            for (int col = topLeftBoundary.x; col <= bottomRightBoundary.x; col++)
            {
                UnitInterface unit = board.get(row, col);
                if ( unit != null )
                {
                    if ( unit.getCurrentState() == UnitInterface.State.ALIVE )
                    {
                        continue;
                    }
                }

                UnitInterface[] adjacentUnits = getUnitsAdjacentToPosition(row, col);

                deadUnit.computeNextTurn(adjacentUnits);
                UnitInterface bornUnit = deadUnit.getBornUnit();
                deadUnit.update();

                if ( bornUnit != null )
                {
                    //System.out.println("" + col + " " + row + " " + Arrays.toString(adjacentUnits));
                    aliveNextTurn = true;
                    setToPosition(row, col, bornUnit);
                    moveProcessBoundaryToInclude(row, col);
                }
            }
        }
        return aliveNextTurn;
    }

    private void cleanupStep()
    {
        for (int row = topLeftActive.y; row <= bottomRightActive.y; row++)
        {
            for (int col = topLeftActive.x; col <= bottomRightActive.x; col++)
            {
                UnitInterface current = board.get(row, col);
                if ( current != null )
                {
                    current.update();
                    if ( current.getCurrentState() == UnitInterface.State.DEAD )
                    {
                        board.remove(row, col);
                    }
                }
            }
        }
    }
   
    public ArrayList<PlayerData> getPlayerRankings(){ return new ArrayList<>(orderedPlayers); }
    
    private void orderPlayersByScore(){
        orderedPlayers =  new ArrayList<>(players.values());
        orderedPlayers.sort(new Comparator<PlayerData> () {
            public int compare(PlayerData one, PlayerData two){
                int val = two.score - one.score;
                if(val == 0)
                    val = one.ID - two.ID;
                return val;
            }
        });
    }
    
    public void addPlayer(PlayerData player)
    {
        players.put(player.ID, player);
        orderPlayersByScore();
    }
    
    public void removePlayer(int id){
        for(int r = 0; r < rowCount; r++)
            for(int c = 0; c < columnCount; c++)
            {
                UnitInterface unit = getUnit(r, c);
                if(unit != null)
                    if(unit.getPlayerID() == id)
                        setToPosition(r, c, null);
            }
        players.remove(id);
        orderPlayersByScore();
    }

    public void setPlayerIDCheckNextTurn(){
        runPlayerIDCheck = true;
    }
    
    public PlayerData.TeamColor getPlayerColor(int ID){
        try{
            return players.get(ID).color;
        }catch(Exception e){
            return PlayerData.TeamColor.NONE;
        }
    }
}

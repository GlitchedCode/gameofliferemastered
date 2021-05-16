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

    private Integer rowCount;
    private Integer columnCount;
    private Integer sectorRowCount;
    private Integer sectorColumnCount;
    private final Integer sectorSideLength = 32;

    private boolean unitFoundThisTurn = false;
    private final Point topLeftActive;
    private final Point bottomRightActive;
    private final Point topLeftProcessed;
    private final Point bottomRightProcessed;

    private final DeadUnit deadUnit;

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
        sectorRowCount = (rows / sectorSideLength) + 1;
        sectorColumnCount = (cols / sectorSideLength) + 1;

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
        sectorRowCount = (rows / sectorSideLength) + 1;
        sectorColumnCount = (cols / sectorSideLength) + 1;

        board.resize(rows, cols);
        sectorFlags.resize(sectorRowCount, sectorColumnCount);
        
    }
    
    public UnitInterface getUnit(int row, int col)    {
        return board.get(row, col);
    }

    public final int getSectorSideLength()    {
        return sectorSideLength;
    }

    /*
    * RENDERING AND UI CODE
     */
    public void setUnit(int row, int col, UnitInterface unit)
    {
        unit.update();
        setToPosition(row, col, unit);
        correctProcessRegion();
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

        for (int sectorRow = 0; sectorRow < sectorRowCount; sectorRow++)
        {
            for (int sectorColumn = 0; sectorColumn < sectorColumnCount; sectorColumn++)
            {

                if ( !surroundingSectorsActive(sectorRow, sectorColumn) )
                {
                    continue;
                }

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
    public final void setToPosition(Integer row, Integer col, UnitInterface unit)
    {
        if ( board.get(row, col) == null )
        {
            board.put(row, col, unit);
            moveProcessBoundaryToInclude(row, col);
            sectorFlags.put(row / sectorSideLength, col / sectorSideLength, true);
        }
        else
        {
            board.remove(row, col);
        }
    }

    private Point getSectorTopLeftBoundary(int sectorRow, int sectorColumn)
    {
        return new Point(sectorColumn * sectorSideLength, sectorRow * sectorSideLength);
    }

    private Point getSectorBottomRightBoundary(int sectorRow, int sectorColumn)
    {
        int row = Integer.min(rowCount, (sectorRow + 1) * sectorSideLength - 1);
        int col = Integer.min(columnCount, (sectorColumn + 1) * sectorSideLength - 1);

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
                {
                    continue;
                }

                
                UnitInterface[] adjacentUnits = getUnitsAdjacentToPosition(row, col);
                //System.out.println("" + col + " " + row + " " + Arrays.toString(adjacentUnits));
                current.computeNextTurn(adjacentUnits);

                // Expand the board's processing area accordingly as we
                // process more units
                if ( current.getNextTurnState() != UnitInterface.State.DEAD )
                {
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
   
    public void addPlayer()
    {
        
    }
}

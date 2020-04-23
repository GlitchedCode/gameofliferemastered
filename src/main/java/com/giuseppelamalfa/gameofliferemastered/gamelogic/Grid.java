/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.DeadUnit;
import com.giuseppelamalfa.gameofliferemastered.ImageManager;
import com.giuseppelamalfa.gameofliferemastered.utils.TwoDimensionalContainer;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;

/**
 * Class for handling all of game logic and rendering all units to the grid
 *
 * @author glitchedcode
 */
public class Grid
{

    private final TwoDimensionalContainer<UnitInterface> board;
    private final ImageManager tileManager;

    private Integer             turn;
    private Integer             sideLength;
    private Integer             lineSpacing;

    private final Dimension     size = new Dimension();
    private Integer             xoffset;
    private Integer             yoffset;
    private Integer             startRow;
    private Integer             startColumn;
    
    private final Integer       rowCount;
    private final Integer       columnCount;

    private final Point         topLeftActive;
    private final Point         bottomRightActive;

    private final Point         topLeftProcessed;
    private final Point         bottomRightProcessed;

    private final DeadUnit      deadUnit;

    /**
     * Constructor
     *
     * @param rows Number of rows
     * @param cols Number of columns
     * @param tileManager ImageManager object that stores game tiles
     * @throws Exception
     */
    public Grid(Integer rows, Integer cols, ImageManager tileManager) throws Exception
    {
        rowCount = rows;
        columnCount = cols;

        this.tileManager = tileManager;
        board = new TwoDimensionalContainer<>(rows, cols);
        deadUnit = new DeadUnit();

        topLeftActive = new Point(0, 0);
        bottomRightActive = new Point(cols, rows);

        topLeftProcessed = new Point(topLeftActive);
        bottomRightProcessed = new Point(bottomRightActive);

        setSideLength(32);

        turn = 0;
    }

    /*
    * GETTERS AND SETTERS
    */
    
    /**
     * @return the number of the board's columns
     */
    public Integer getRowCount()
    {
        return rowCount;
    }

    /**
     * @return the number of the board's columns
     */
    public Integer getColumnCount()
    {
        return columnCount;
    }

    /**
     * @return the current in game turn
     */
    public Integer getTurn()
    {
        return turn;
    }
    
    public UnitInterface getUnit(int row, int col)
    {
        return board.get(row, col);
    }

    /**
     * Sets side length for all units and sets grid width and height
     * accordingly, taking into account that the grid's lines are 1 pixel wide
     *
     * @param value
     */
    public final void setSideLength(Integer value)
    {
        size.width = (value + 1) * columnCount + 1;
        size.height = (value + 1) * rowCount + 1;
    }

    public final Integer getSideLength()
    {
        return sideLength;
    }

    public final Dimension getSize()
    {
        return size;
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

    public void drawUnit(Graphics2D g, AffineTransform xform, ImageObserver obs, UnitInterface unit)
    {
        Image img = tileManager.getImage(unit.getSpecies().getTextureCode());
        g.drawImage(img, xform, obs);
    }

    /*
    * GAME LOGIC CODE
    */
    /**
     * Advances the game state to the next turn
     */
    public void computeNextTurn()
    {
        nextTurnStateComputationStep();
        reproductionStep();
        cleanupStep();
        correctProcessRegion();

        turn = turn + 1;
    }

    private void moveProcessBoundaryToInclude(Integer row, Integer col)
    {
        if (row < topLeftProcessed.y)
        {
            topLeftProcessed.y = row;
        }
        else if (row > bottomRightProcessed.y)
        {
            bottomRightProcessed.y = row;
        }

        if (col < topLeftProcessed.x)
        {
            topLeftProcessed.x = col;
        }
        else if (col > bottomRightProcessed.x)
        {
            bottomRightProcessed.x = col;
        }

    }
    
    private void correctProcessRegion()
    {
        topLeftActive.move(Integer.max(topLeftProcessed.x - 1, 0),
                Integer.max(topLeftProcessed.y - 1, 0));

        bottomRightActive.move(Integer.min(bottomRightProcessed.x + 2, columnCount),
                Integer.min(bottomRightProcessed.y + 2, rowCount));
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
        if (board.get(row, col) == null)
        {
            board.put(row, col, unit);
            moveProcessBoundaryToInclude(row, col);
        }
        else
        {
            board.remove(row, col);
        }
    }

    private UnitInterface[] getUnitsAdjacentToPosition(Integer row, Integer col)
    {
        UnitInterface[] ret = new UnitInterface[8];

        //top row
        for (int i = 0; i < 3; i++)
        {
            UnitInterface unit = null;
            if (col + i >= topLeftActive.x)
            {
                unit = board.get(row - 1, col + i - 1);
            }
            ret[i] = unit;
        }

        //middle row
        ret[7] = board.get(row, col - 1);
        ret[3] = board.get(row, col + 1);

        //bottom row
        for (int i = 0; i < 3; i++)
        {
            UnitInterface unit = null;
            if (col + i >= topLeftActive.x)
            {
                unit = board.get(row + 1, col - i + 1);
            }
            ret[i + 4] = unit;
        }
        // if there is no unit, just leave null
        return ret;
    }

    private void nextTurnStateComputationStep()
    {
        boolean noUnitFound = true;
        for (int row = topLeftActive.y; row <= bottomRightActive.y; row++)
        {
            for (int col = topLeftActive.x; col <= bottomRightActive.x; col++)
            {
                UnitInterface current = board.get(row, col);
                if (current == null)
                {
                    continue;
                }

                // Set the board's processing rectangle to only contain the first
                // live unit found
                if (noUnitFound)
                {
                    topLeftProcessed.move(col, row);
                    bottomRightProcessed.move(col, row);
                    noUnitFound = false;
                }
                // Expand the board's processing area accordingly as we
                // process more units
                else
                {
                    moveProcessBoundaryToInclude(col, row);
                }

                UnitInterface[] adjacentUnits = getUnitsAdjacentToPosition(row, col);
                //System.out.println(Arrays.toString(adjacentUnits));

                current.computeNextTurn(adjacentUnits);

            }
        }
    }

    private void reproductionStep()
    {
        for (int row = topLeftActive.y; row <= bottomRightActive.y; row++)
        {
            for (int col = topLeftActive.x; col <= bottomRightActive.x; col++)
            {
                if (board.get(row, col) != null)
                {
                    continue;
                }

                UnitInterface[] adjacentUnits = getUnitsAdjacentToPosition(row, col);

                deadUnit.computeNextTurn(adjacentUnits);
                UnitInterface bornUnit = deadUnit.getBornUnit();
                deadUnit.update();

                if (bornUnit != null)
                {
                    setToPosition(row, col, bornUnit);
                    moveProcessBoundaryToInclude(row, col);
                }
            }
        }
    }

    private void cleanupStep()
    {
        for (int row = topLeftActive.y; row <= bottomRightActive.y; row++)
        {
            for (int col = topLeftActive.x; col <= bottomRightActive.x; col++)
            {
                UnitInterface current = board.get(row, col);
                if (current != null)
                {
                    current.update();
                    if (current.getCurrentState() == UnitInterface.State.DEAD)
                    {
                        board.remove(row, col);
                    }
                }
            }
        }
    }
}

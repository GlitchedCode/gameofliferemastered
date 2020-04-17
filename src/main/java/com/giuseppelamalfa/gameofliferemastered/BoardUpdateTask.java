/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.Grid;
import java.util.TimerTask;
import javax.swing.JPanel;

/**
 *
 * @author glitchedcode
 */
public class BoardUpdateTask extends TimerTask
{

    private final Grid grid;
    private final JPanel canvas;

    private final long msInterval;

    public BoardUpdateTask(Grid grid, JPanel canvas)
    {
        super();
        this.grid = grid;
        this.canvas = canvas;
        msInterval = 250;
    }

    @Override
    public void run()
    {
        synchronized (grid)
        {
            grid.computeNextTurn();
        }
    }

    public long getMsInterval()
    {
        return msInterval;
    }

}

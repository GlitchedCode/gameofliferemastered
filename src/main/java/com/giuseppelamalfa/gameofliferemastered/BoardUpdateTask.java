/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.GridPanelInterface;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

/**
 *
 * @author glitchedcode
 */
public class BoardUpdateTask extends TimerTask
{

    private final GridPanelInterface grid;
    private final JPanel canvas;

    private final long msInterval;

    public BoardUpdateTask(GridPanelInterface grid, JPanel canvas)
    {
        super();
        this.grid = grid;
        this.canvas = canvas;
        msInterval = 150;
    }

    @Override
    public void run()
    {
        synchronized (grid)
        {
            try
            {
                grid.computeNextTurn();
            }
            catch (Exception ex)
            {
                Logger.getLogger(BoardUpdateTask.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public long getMsInterval()
    {
        return msInterval;
    }

}

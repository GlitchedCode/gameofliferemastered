/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered;

import java.util.TimerTask;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.SimulationInterface;

/**
 *
 * @author glitchedcode
 */
public class BoardUpdateTask extends TimerTask
{

    public SimulationInterface grid;

    private final long msInterval;

    public BoardUpdateTask()
    {
        super();
        msInterval = 150;
    }
    
    @Override
    public void run()
    {
        if(grid == null) return;
        if(!grid.isSimulationRunning()) return;
        try
        {
            grid.computeNextTurn();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public long getMsInterval()
    {
        return msInterval;
    }

}

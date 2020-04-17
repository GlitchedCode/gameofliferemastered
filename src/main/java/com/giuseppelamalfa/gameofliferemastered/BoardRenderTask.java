/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered;

import java.util.TimerTask;
import javax.swing.JPanel;

/**
 *
 * @author glitchedcode
 */
public class BoardRenderTask extends TimerTask
{
    private final JPanel canvas;
    
    public BoardRenderTask(JPanel canvas)
    {
        this.canvas = canvas;
    }
    
    @Override
    public void run()
    {
        synchronized (canvas)
        {
            canvas.repaint();
        }
    }
}

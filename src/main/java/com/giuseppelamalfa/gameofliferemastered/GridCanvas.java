/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.Grid;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Timer;
import javax.swing.JPanel;

/**
 *
 * @author glitchedcode
 */
public class GridCanvas extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener
{

    private Integer sideLength = 32;
    private Grid grid;

    private Point screenOrigin;

    private Point lastDragLocation = new Point();

    private final Timer timer = new Timer();
    private boolean autoplay = false;
    private BoardUpdateTask updateTask;

    /*
    * MOUSE INPUT HANDLING
     */
    @Override
    public void mouseClicked(MouseEvent me)
    {
        synchronized (grid)
        {
            int button = me.getButton();
            if (button == MouseEvent.BUTTON1)
            {
                grid.setUnit(me.getPoint());
            }
            if (button == MouseEvent.BUTTON2)
            {
                grid.clearBoard();
            }
            if (button == MouseEvent.BUTTON3)
            {
                grid.computeNextTurn();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent me)
    {
        synchronized (this)
        {
            int button = me.getButton();
            if (button == MouseEvent.BUTTON1)
            {
                lastDragLocation = new Point(me.getPoint());
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent me)
    {
        synchronized (this)
        {
            int mask = MouseEvent.BUTTON1_DOWN_MASK;
            if (me.getModifiersEx() != mask)
            {
                return;
            }
            Point dragLocation = me.getPoint();
            Point offset = new Point(lastDragLocation.x - dragLocation.x,
                    lastDragLocation.y - dragLocation.y);
            screenOrigin.translate(offset.x, offset.y);
            setScreenOrigin(screenOrigin);
            lastDragLocation = new Point(dragLocation);
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent me)
    {
        synchronized (this)
        {
            int rotation = me.getWheelRotation();
            synchronized (grid)
            {
                setSideLength(grid.getSideLength() - (rotation * 4));
            }
        }
    }

    // don't need these
    @Override
    public void mouseReleased(MouseEvent me)
    {
    }

    @Override
    public void mouseEntered(MouseEvent me)
    {
    }

    @Override
    public void mouseExited(MouseEvent me)
    {
    }

    @Override
    public void mouseMoved(MouseEvent me)
    {
    }

    /*
    * KEYBOARD EVENT LOGIC
     */
    @Override
    public void keyPressed(KeyEvent e)
    {
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_SPACE)
        {
            System.out.println("kek");
            if (autoplay)
            {
                if (updateTask != null)
                {
                   updateTask.cancel();
                   updateTask = null;
                }
                autoplay = false;
            }
            else
            {
                updateTask = new BoardUpdateTask(grid, this);
                timer.schedule(updateTask, 0, updateTask.getMsInterval());
                autoplay = true;
            }
        }
    }

    public void setGrid(Grid grid)
    {
        this.grid = grid;
        grid.setSideLength(sideLength);
        Dimension gridSize = grid.getSize();
        Dimension size = getSize();
        setScreenOrigin(new Point((gridSize.width - size.width) / 2,
                (gridSize.height - size.height) / 2));
    }

    public void setScreenOrigin(Point newOrigin)
    {
        screenOrigin = newOrigin;
        Dimension gridSize = grid.getSize();
        Dimension size = getSize();
        int maxX = Integer.max(gridSize.width - size.width, 0);
        int maxY = Integer.max(gridSize.height - size.height, 0);
        screenOrigin.x = Integer.min(maxX, Integer.max(screenOrigin.x, 0));
        screenOrigin.y = Integer.min(maxY, Integer.max(screenOrigin.y, 0));
        grid.setScreenOrigin(screenOrigin);

    }

    public void resetScreenOrigin()
    {
        setScreenOrigin(screenOrigin);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        Dimension size = getSize();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, size.width, size.height);
        if (grid != null)
        {
            grid.draw((Graphics2D) g, this);
        }
    }

    /**
     * @return side length for units
     */
    public Integer getSideLength()
    {
        return sideLength;
    }

    /**
     * Set side length for units
     *
     * @param value
     */
    public void setSideLength(Integer value)
    {
        sideLength = value;
        grid.setSideLength(value);
        setScreenOrigin(screenOrigin);
    }

    @Override
    public void setSize(Dimension size)
    {
        super.setSize(size);
        grid.setCanvasSize(size);
    }

    public void init()
    {
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
        timer.schedule(new BoardRenderTask(this), 0, 18);
    }
}

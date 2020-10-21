/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.Grid;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.UnitInterface;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.Cell;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.Snake;
import java.awt.BasicStroke;
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
import java.awt.geom.AffineTransform;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;

/**
 *
 * @author glitchedcode
 */
public final class GridCanvas extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener
{

    private int sideLength;
    private Grid grid;

    private Point screenOrigin;

    private Point lastDragLocation = new Point();

    private final Timer timer = new Timer();
    private boolean autoplay = false;
    private BoardUpdateTask updateTask;
    private int lineSpacing;
    private float tileScale;
    private int yoffset;
    private int xoffset;
    private int startRow;
    private int startColumn;
    
    public GridCanvas()
    {
        setSideLength(32);
    }

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
                setUnit(me.getPoint());
            }
            if (button == MouseEvent.BUTTON2)
            {
                grid.clearBoard();
            }
            if (button == MouseEvent.BUTTON3)
            {
                try
                {
                    grid.computeNextTurn();
                }
                catch (Exception ex)
                {
                    Logger.getLogger(GridCanvas.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        }

    @Override
    public void mouseMoved(MouseEvent me)
    {
        synchronized (grid)
        {
            Point point = me.getPoint();
            point.x += xoffset;
            point.y += yoffset;

            int row = point.y / lineSpacing + startRow;
            int col = point.x / lineSpacing + startColumn;
            
            int sectorRow = row / grid.getSectorSideLength();
            int sectorCol = col / grid.getSectorSideLength();
            
            String text = "<html>Position: (" + col + ", " + row + 
                    ")<br>Sector: (" + sectorCol + ", " + sectorRow + ")";
            
            UnitInterface unit = grid.getUnit(row, col);
            
            if (unit != null)
            {
                text += "<br>" + unit.toString();
            }
            
            text += "</html>";
            
            setToolTipText(text);
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
                setSideLength(sideLength - (rotation * 4));
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

    private void setUnit(Point point)
    {
        point.x += xoffset;
        point.y += yoffset;

        int row = point.y / lineSpacing + startRow;
        int col = point.x / lineSpacing + startColumn;
        
        if (grid != null)
            grid.setUnit(row, col, new Snake());
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
        
        yoffset = screenOrigin.y % (lineSpacing);
        xoffset = screenOrigin.x % (lineSpacing);
        startRow = screenOrigin.y / lineSpacing;
        startColumn = screenOrigin.x / lineSpacing;
    }

    public void resetScreenOrigin()
    {
        setScreenOrigin(screenOrigin);
    }

    @Override
    public void repaint()
    {
        synchronized (this)
        {
            super.repaint();
        }
    }
    
    @Override
    public void paintComponent(Graphics g)
    {
        if (grid == null)
            return;
        
        Graphics2D g2 = (Graphics2D) g;
        Dimension size = getSize();
        g.setColor(getBackground());
        g.fillRect(0, 0, size.width, size.height);
        
        int rows, cols;
        int gridRows = grid.getRowCount(), gridCols = grid.getColumnCount();
        Dimension gridSize = grid.getSize();
        int height = Integer.min(size.height, gridSize.height);
        int width = Integer.min(size.width, gridSize.width);

        rows = Integer.min(height / sideLength + 1, gridRows);
        cols = Integer.min(width / sideLength + 1, gridCols);

        // Draw the grid
        g2.setStroke(new BasicStroke(1));
        g2.setColor(getForeground());
        // rows
        for (int c = 0; c <= rows; c++)
        {
            int ypos = c * (lineSpacing) - yoffset;
            g.drawLine(0, ypos, width - 1, ypos);
        }

        // columns
        for (int c = 0; c <= cols; c++)
        {
            int xpos = c * (lineSpacing) - xoffset;
            g.drawLine(xpos, 0, xpos, height - 1);
        }
                
        // Draw the units
        int endRow = startRow + rows;
        int endColumn = startColumn + cols;
        int drawnRows = endRow - startRow;
        int drawnColumns = endColumn - startColumn;
        
        g2.setStroke(new BasicStroke(2));
        
        
        Point topLeft = grid.getTopLeftActive();
        Point bottomRight = grid.getBottomRightActive();
        
        for (int r = 0; r < drawnRows; r++) // rows
        {
            for (int c = 0; c < drawnColumns; c++) // columns
            {

                int row = r + startRow;
                int col = c + startColumn;

                int xpos = c * (lineSpacing) - xoffset + 1;
                int ypos = r * (lineSpacing) - yoffset + 1;
                
                if (row == topLeft.y && col == topLeft.x)
                {
                    g2.setColor(Color.RED);
                    g.drawLine(0, ypos, width - 1, ypos);
                    g.drawLine(xpos, 0, xpos, height - 1);
                    g.drawOval(xpos - 5, ypos - 5, 10, 10);
                }
                else if (row == bottomRight.y && col == bottomRight.x)
                {
                    g2.setColor(Color.BLUE);
                    g.drawLine(0, ypos + lineSpacing, width - 1, ypos + lineSpacing);                    
                    g.drawLine(xpos + lineSpacing, 0, xpos + lineSpacing, height - 1);
                    g.drawOval(xpos + lineSpacing - 5, ypos + lineSpacing - 5, 10, 10);
                }
                
                UnitInterface unit = grid.getUnit(row, col);
                if (unit == null)
                {
                    continue;
                }

                
                AffineTransform xform = new AffineTransform();
                xform.translate(xpos, ypos);
                xform.scale(tileScale, tileScale);

                grid.drawUnit(g2, xform, this, unit);
            }
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
        sideLength = Integer.min(64, Integer.max(8, value));
        lineSpacing = sideLength + 1;
        if (grid != null)
        {
            grid.setSideLength(sideLength);
            setScreenOrigin(screenOrigin);
        }
        tileScale = sideLength / 8.0f;
    }

    @Override
    public void setSize(Dimension size)
    {
        super.setSize(size);
    }

    public void init()
    {
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
        timer.schedule(new BoardRenderTask(this), 0, 18);
        ToolTipManager.sharedInstance().setInitialDelay(150);
    }
}

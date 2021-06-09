/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.ui;

import com.giuseppelamalfa.gameofliferemastered.ApplicationFrame;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.UnitInterface;
import com.giuseppelamalfa.gameofliferemastered.utils.ImageManager;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.simulation.SimulationInterface;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.SpeciesLoader;
import com.giuseppelamalfa.gameofliferemastered.utils.TimerWrapper;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author glitchedcode
 */
public final class GridPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

    final int TOOLTIP_INITIAL_DELAY_MS = 150;

    protected int sideLength;
    protected SimulationInterface simulation;
    protected ImageManager tileManager;

    protected Point screenOrigin = new Point();

    protected Point lastDragLocation = new Point();

    protected int lineSpacing;
    protected float tileScale;
    protected int yoffset;
    protected int xoffset;
    protected int startRow;
    protected int startColumn;

    protected final TimerWrapper timer = new TimerWrapper();
    private boolean initialized = false;

    protected GameStatusPanel gameStatusPanel;
    protected UnitPalette palette;

    public GridPanel() {
        setSideLength(32);
    }

    public GridPanel(ImageManager tileManager) {
        this.tileManager = tileManager;
        setSideLength(32);
    }

    /*
    * MOUSE INPUT HANDLING
     */
    @Override
    public void mouseClicked(MouseEvent me) {
        synchronized (simulation) {
            int button = me.getButton();
            if (button == MouseEvent.BUTTON1 & !simulation.isLocked()) {
                setUnit(me.getPoint());
            } else if (button == MouseEvent.BUTTON3) {
                try {
                    if (simulation.isLocallyControlled() & !simulation.isLocked()) {
                        simulation.computeNextTurn();
                        gameStatusPanel.setPlayerPanels(simulation.getPlayerRankings());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent me) {
        synchronized (simulation) {
            Point point = me.getPoint();
            point.x += xoffset;
            point.y += yoffset;

            int row = point.y / lineSpacing + startRow;
            int col = point.x / lineSpacing + startColumn;

            int sectorRow = row / simulation.getSectorSideLength();
            int sectorCol = col / simulation.getSectorSideLength();

            String text = "<html>Position: (" + col + ", " + row
                    + ")<br>Sector: (" + sectorCol + ", " + sectorRow + ")";

            UnitInterface unit = simulation.getUnit(row, col);

            if (unit != null) {
                text += "<br>" + unit.toString();
            }

            text += "</html>";

            setToolTipText(text);
        }
    }

    @Override
    public void mousePressed(MouseEvent me) {
        int button = me.getButton();
        if (button == MouseEvent.BUTTON1) {
            lastDragLocation = new Point(me.getPoint());
        }
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        int mask = MouseEvent.BUTTON1_DOWN_MASK;
        if (me.getModifiersEx() != mask) {
            return;
        }
        Point dragLocation = me.getPoint();
        Point offset = new Point(lastDragLocation.x - dragLocation.x,
                lastDragLocation.y - dragLocation.y);
        screenOrigin.translate(offset.x, offset.y);
        setScreenOrigin(screenOrigin);
        lastDragLocation = new Point(dragLocation);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent me) {
        int rotation = me.getWheelRotation();
        setSideLength(sideLength - (rotation * 4));
    }

    // don't need these
    @Override
    public void mouseReleased(MouseEvent me) {
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

    /*
    * KEYBOARD EVENT LOGIC
     */
    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            simulation.setRunning(!simulation.isRunning());
        }
    }

    private void setUnit(Point point) {
        point.x += xoffset;
        point.y += yoffset;

        int row = point.y / lineSpacing + startRow;
        int col = point.x / lineSpacing + startColumn;

        try {
            if (simulation.getUnit(row, col) != null) {
                simulation.removeUnit(row, col);
            } else {
                simulation.setUnit(row, col, palette.getNewUnit(simulation.getLocalPlayerID()));
            }
            gameStatusPanel.setPlayerPanels(simulation.getPlayerRankings());
        } catch (Exception ex) {
            Logger.getLogger(GridPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void setSimulation(SimulationInterface simulation, boolean resetOrigin) {
        this.simulation = simulation;
        setSideLength(sideLength);

        gameStatusPanel.setTurnCount(simulation.getCurrentTurn());
        gameStatusPanel.setPlayerPanels(simulation.getPlayerRankings());
        gameStatusPanel.setGameModeName(simulation.getGameModeName());

        Dimension size = getSize();
        Dimension gridSize = getGridSize();
        if (resetOrigin) {
            setScreenOrigin(new Point((gridSize.width - size.width) / 2,
                    (gridSize.height - size.height) / 2));
        }
    }

    public void setSimulation(SimulationInterface grid) {
        GridPanel.this.setSimulation(grid, false);
    }

    public void setScreenOrigin(Point newOrigin) {
        screenOrigin = newOrigin;
        Dimension size = getSize();
        Dimension gridSize = getGridSize();
        int maxX = Integer.max(gridSize.width - size.width, 0);
        int maxY = Integer.max(gridSize.height - size.height, 0);
        screenOrigin.x = Integer.min(maxX, Integer.max(screenOrigin.x, 0));
        screenOrigin.y = Integer.min(maxY, Integer.max(screenOrigin.y, 0));

        yoffset = screenOrigin.y % (lineSpacing);
        xoffset = screenOrigin.x % (lineSpacing);
        startRow = screenOrigin.y / lineSpacing;
        startColumn = screenOrigin.x / lineSpacing;
    }

    public void resetScreenOrigin() {
        setScreenOrigin(screenOrigin);
    }

    @Override
    public void repaint() {
        super.repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        if (simulation == null) {
            return;
        }

        gameStatusPanel.setTurnCount(simulation.getCurrentTurn());
        gameStatusPanel.setStatus(simulation.getStatusString());

        Graphics2D g2 = (Graphics2D) g;
        Dimension size = getSize();
        g2.setColor(getBackground());
        g2.fillRect(0, 0, size.width, size.height);

        int rows, cols;
        int gridRows = simulation.getRowCount(), gridCols = simulation.getColumnCount();
        Dimension gridSize = size;
        int height = Integer.min(size.height, gridSize.height);
        int width = Integer.min(size.width, gridSize.width);

        rows = Integer.min(height / sideLength + 1, gridRows);
        cols = Integer.min(width / sideLength + 1, gridCols);

        // Draw the simulation
        g2.setStroke(new BasicStroke(1));
        g2.setColor(getForeground());
        // rows
        for (int c = 0; c <= rows; c++) {
            int ypos = c * (lineSpacing) - yoffset;
            g2.drawLine(0, ypos, width - 1, ypos);
        }

        // columns
        for (int c = 0; c <= cols; c++) {
            int xpos = c * (lineSpacing) - xoffset;
            g2.drawLine(xpos, 0, xpos, height - 1);
        }

        // Draw the units
        int endRow = startRow + rows;
        int endColumn = startColumn + cols;
        int drawnRows = endRow - startRow;
        int drawnColumns = endColumn - startColumn;

        g2.setStroke(new BasicStroke(2));

        for (int r = 0; r < drawnRows; r++) // rows
        {
            for (int c = 0; c < drawnColumns; c++) // columns
            {
                int row = r + startRow;
                int col = c + startColumn;

                int xpos = c * (lineSpacing) - xoffset + 1;
                int ypos = r * (lineSpacing) - yoffset + 1;

                UnitInterface unit = simulation.getUnit(row, col);
                if (unit == null) {
                    continue;
                }

                AffineTransform xform = new AffineTransform();
                xform.translate(xpos, ypos);
                xform.scale(tileScale, tileScale);

                drawUnit(g2, xform, this, unit);
            }
        }
    }

    public void drawUnit(Graphics2D g, AffineTransform xform, ImageObserver obs, UnitInterface unit) {
        Image img = tileManager.getImage(SpeciesLoader.getSpeciesData(unit.getSpeciesID()).textureCode);
        g.setColor(simulation.getPlayerColor(unit.getPlayerID()).getMainAWTColor());
        g.fillRect((int) xform.getTranslateX(), (int) xform.getTranslateY(),
                (int) (xform.getScaleX() * 8), (int) (xform.getScaleY() * 8));
        g.drawImage(img, xform, obs);
    }

    /**
     * @return side length for units
     */
    public Integer getSideLength() {
        return sideLength;
    }

    /**
     * Set side length for units
     *
     * @param value
     */
    public void setSideLength(Integer value) {
        sideLength = Integer.min(64, Integer.max(8, value));
        lineSpacing = sideLength + 1;
        if (simulation != null) {
            setScreenOrigin(screenOrigin);
        }
        tileScale = sideLength / 8.0f;
    }

    public Dimension getGridSize() {
        Dimension gridSize = new Dimension();
        gridSize.width = (sideLength + 1) * simulation.getColumnCount() + 1;
        gridSize.height = (sideLength + 1) * simulation.getRowCount() + 1;
        return gridSize;
    }

    public GameStatusPanel getGameStatusPanel() {
        return gameStatusPanel;
    }

    public void init(UnitPalette palette) {
        if (initialized) {
            return;
        }

        gameStatusPanel = (GameStatusPanel) getComponent(0);
        this.palette = palette;
        initialized = true;
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
        
        // repaint 60 times a secound
        timer.scheduleAtFixedRate(() -> {
            repaint();
        }, 0, 18);
        // board update task
        timer.scheduleAtFixedRate(() -> {
            if (simulation == null) {
                return;
            }
            if (!simulation.isRunning()) {
                return;
            }
            try {
                simulation.computeNextTurn();
                gameStatusPanel.setPlayerPanels(simulation.getPlayerRankings());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, ApplicationFrame.BOARD_UPDATE_MS);

        ToolTipManager.sharedInstance().setInitialDelay(TOOLTIP_INITIAL_DELAY_MS);
    }
}

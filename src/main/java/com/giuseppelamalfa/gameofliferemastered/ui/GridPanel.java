/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.ui;

import com.giuseppelamalfa.gameofliferemastered.ui.renderers.TextureGridRenderer;
import com.giuseppelamalfa.gameofliferemastered.ApplicationFrame;
import com.giuseppelamalfa.gameofliferemastered.utils.ImageManager;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import com.giuseppelamalfa.gameofliferemastered.simulation.SimulationInterface;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.SpeciesLoader;
import com.giuseppelamalfa.gameofliferemastered.utils.TimerWrapper;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.Unit;
import com.giuseppelamalfa.gameofliferemastered.ui.renderers.GridRenderer;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author glitchedcode
 */
public class GridPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

    final int TOOLTIP_INITIAL_DELAY_MS = 150;

    private SimulationInterface simulation;

    private Point screenOrigin = new Point();
    private Point lastDragLocation = new Point();

    private final TimerWrapper timer = new TimerWrapper();
    private boolean initialized = false;
    private GameStatusPanel gameStatusPanel = new GameStatusPanel();
    private UnitPalette palette = new UnitPalette();

    GridRenderer renderer;

    public GridPanel() {
    }

    public GridPanel(ImageManager tileManager) {
        renderer = new TextureGridRenderer(tileManager);
        setSideLength(32);
    }

    /*
    * MOUSE INPUT HANDLING
     */
    @Override
    public void mouseClicked(MouseEvent me) {
        if (simulation == null) {
            return;
        }
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
        if (simulation == null) {
            return;
        }
        synchronized (simulation) {
            Point point = me.getPoint();

            int lineSpacing = renderer.getLineSpacing();
            int yoffset = screenOrigin.y % (lineSpacing);
            int xoffset = screenOrigin.x % (lineSpacing);
            int startRow = screenOrigin.y / lineSpacing;
            int startColumn = screenOrigin.x / lineSpacing;

            point.x += xoffset;
            point.y += yoffset;

            int row = point.y / lineSpacing + startRow;
            int col = point.x / lineSpacing + startColumn;

            String text = "<html>Position: (" + col + ", " + row + ")";

            Unit unit = simulation.getUnit(row, col);

            if (unit != null) {
                text += "<br>" + unit.toString();
            }

            text += "</html>";

            //setToolTipText(text);
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
        setSideLength(renderer.getSideLength() - (rotation * 4));
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
        if (simulation == null) {
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            simulation.setRunning(!simulation.isRunning());
        } else if (e.getKeyCode() == KeyEvent.VK_HOME) {
            try {
                simulation.saveGrid();
            } catch (Exception ex) {
                Logger.getLogger(GridPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    protected void setUnit(Point point) {
        int lineSpacing = renderer.getLineSpacing();
        int yoffset = screenOrigin.y % (lineSpacing);
        int xoffset = screenOrigin.x % (lineSpacing);
        int startRow = screenOrigin.y / lineSpacing;
        int startColumn = screenOrigin.x / lineSpacing;

        point.x += xoffset;
        point.y += yoffset;

        int row = point.y / lineSpacing + startRow;
        int col = point.x / lineSpacing + startColumn;

        if (simulation == null) {
            return;
        }

        try {
            if (simulation.getUnit(row, col).isAlive()) {
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
        if (simulation == null) {
            return;
        }

        setScreenOrigin(screenOrigin);

        palette.setSimulation(simulation);

        gameStatusPanel.setVisible(true);
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
        setSimulation(grid, false);
    }

    public SimulationInterface getSimulation() {
        return simulation;
    }

    public void setScreenOrigin(Point newOrigin) {
        screenOrigin = newOrigin;
        Dimension size = getSize();
        Dimension gridSize = getGridSize();
        int maxX = Integer.max(gridSize.width - size.width, 0);
        int maxY = Integer.max(gridSize.height - size.height, 0);
        screenOrigin.x = Integer.min(maxX, Integer.max(screenOrigin.x, 0));
        screenOrigin.y = Integer.min(maxY, Integer.max(screenOrigin.y, 0));
    }

    public void resetScreenOrigin() {
        setScreenOrigin(screenOrigin);
    }

    @Override
    public void repaint() {
        super.repaint();
    }

    public void saveScreenshot(String path) throws IOException {
        BufferedImage ret = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        paint(ret.getGraphics());
        ImageIO.write(ret, "png", new File(path));
    }

    @Override
    public void paintComponent(Graphics g) {
        renderer.render(g, this, screenOrigin);
    }

    Color getUnitColor(Unit unit) {
        return unit.getColor();
    }

    /**
     * Set side length for units
     *
     * @param value
     */
    public final void setSideLength(Integer value) {
        renderer.setSideLength(value);
    }

    public Dimension getGridSize() {
        Dimension gridSize = new Dimension();
        int sideLength = renderer.getSideLength();
        gridSize.width = (sideLength + 1) * simulation.getColumnCount() + 1;
        gridSize.height = (sideLength + 1) * simulation.getRowCount() + 1;
        return gridSize;
    }

    public UnitPalette getPalette() {
        return palette;
    }

    public GameStatusPanel getGameStatusPanel() {
        return gameStatusPanel;
    }

    public void init(UnitPalette palette) {
        if (initialized) {
            return;
        }
        try {
            gameStatusPanel = (GameStatusPanel) getComponent(0);
        } catch (Exception e) {
            gameStatusPanel = new GameStatusPanel();
        }
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

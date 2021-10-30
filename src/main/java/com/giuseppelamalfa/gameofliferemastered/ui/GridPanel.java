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
import com.giuseppelamalfa.gameofliferemastered.utils.TimerWrapper;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.Unit;
import com.giuseppelamalfa.gameofliferemastered.ui.renderers.GridRenderer;
import com.giuseppelamalfa.gameofliferemastered.ui.renderers.PixelGridRenderer;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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

    private boolean isPixelRenderer;
    private final TextureGridRenderer textureRenderer;
    private final PixelGridRenderer pixelRenderer = new PixelGridRenderer();
    private Path screenshotSaveDir = null;

    private GridRenderer currentRenderer;

    public GridPanel() {
        textureRenderer = null;
        currentRenderer = pixelRenderer;
        isPixelRenderer = true;
        setSideLengthPower(5);
    }

    public GridPanel(ImageManager tileManager) {
        textureRenderer = new TextureGridRenderer(tileManager);
        currentRenderer = textureRenderer;
        isPixelRenderer = false;
        setSideLengthPower(5);
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
        /*
        if (simulation == null) {
            return;
        }

        Point point = me.getPoint();

        int lineSpacing = currentRenderer.getLineSpacing();
        int yoffset = screenOrigin.y % (lineSpacing);
        int xoffset = screenOrigin.x % (lineSpacing);
        int startRow = screenOrigin.y / lineSpacing;
        int startColumn = screenOrigin.x / lineSpacing;

        point.x += xoffset;
        point.y += yoffset;

        int row = point.y / lineSpacing + startRow;
        int col = point.x / lineSpacing + startColumn;

        Unit unit = simulation.getUnit(row, col);
        String text = "<html>Position: (" + col + ", " + row + ")";
        if (unit != null) {
            text += "<br>" + unit.toString();
        }
        text += "</html>";
        setToolTipText(text);
         */
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
        if (rotation < 0) {
            incrementSideLengthPower();
        } else {
            decrementSideLengthPower();
        }
        setSideLengthPower(currentRenderer.getSideLength() - (rotation * 4));
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
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SPACE:
                simulation.setRunning(!simulation.isRunning());
                break;
            case KeyEvent.VK_HOME: {
                try {
                    simulation.saveGrid();
                } catch (Exception ex) {
                    Logger.getLogger(GridPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
            case KeyEvent.VK_INSERT:
                swapRenderers();
                break;
            case KeyEvent.VK_F12: {
                try {
                    toggleSaveScreenshot();
                } catch (IOException ex) {
                    Logger.getLogger(GridPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
            default:
        }
    }

    private void swapRenderers() {
        if (isPixelRenderer) {
            currentRenderer = textureRenderer;
            gameStatusPanel.setVisible(true);
        } else {
            currentRenderer = pixelRenderer;
            gameStatusPanel.setVisible(false);
        }
        isPixelRenderer = !isPixelRenderer;
    }

    protected void toggleSaveScreenshot() throws IOException {
        if (simulation == null) {
            return;
        }

        if (screenshotSaveDir == null) {
            LocalDateTime now = LocalDateTime.now();
            screenshotSaveDir = Path.of("grid-" + DateTimeFormatter.ISO_INSTANT.format(now.toInstant(ZoneOffset.UTC)) + "\\");
            File file = screenshotSaveDir.toFile();
            if (!file.mkdir()) {
                System.out.println("Failed to create screenshot directory.");
                screenshotSaveDir = null;
                return;
            }
            System.out.println("Saving screenshots to " + screenshotSaveDir);
            Path palettePath = Path.of(screenshotSaveDir.toString(), "palette.txt");
            File paletteFile = Files.createFile(palettePath).toFile();
            simulation.getSpeciesLoader().writePalette(new FileOutputStream(paletteFile));

            Path screenshotPath = Path.of(screenshotSaveDir.toString(), (Integer.toString(simulation.getCurrentTurn()) + ".gif"));
            saveScreenshot(screenshotPath.toFile());
        } else {
            screenshotSaveDir = null;
        }
    }

    protected void setUnit(Point point) {
        int lineSpacing = currentRenderer.getLineSpacing();
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

    public void saveScreenshot(File file) {
        BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        paint(img.getGraphics());
        new Thread(() -> {
            try {
                ImageIO.write(img, "gif", file);
            } catch (IOException ex) {
                Logger.getLogger(GridPanel.class.getName()).log(Level.SEVERE, null, ex);
            }

        }).start();
    }

    @Override
    public void paintComponent(Graphics g) {
        currentRenderer.render(g, this, screenOrigin);
    }

    Color getUnitColor(Unit unit) {
        return unit.getColor();
    }

    private int sideLengthPower = 5;

    public final void incrementSideLengthPower() {
        setSideLengthPower(sideLengthPower + 1);
    }

    public final void decrementSideLengthPower() {
        setSideLengthPower(sideLengthPower - 1);
    }

    /**
     * Set side length for units
     *
     * @param power
     */
    public final void setSideLengthPower(int power) {
        sideLengthPower = Math.max(power, 0);
        int len = 1 << sideLengthPower;
        System.out.println(len);
        pixelRenderer.setSideLength(len);
        if (textureRenderer != null) {
            textureRenderer.setSideLength(len);
        }
    }

    public Dimension getGridSize() {
        Dimension gridSize = new Dimension();
        int sideLength = currentRenderer.getSideLength();
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
                if (screenshotSaveDir != null) {
                    Path screenshotPath = Path.of(screenshotSaveDir.toString(), (Integer.toString(simulation.getCurrentTurn()) + ".gif"));
                    saveScreenshot(screenshotPath.toFile());
                }
                gameStatusPanel.setPlayerPanels(simulation.getPlayerRankings());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, ApplicationFrame.BOARD_UPDATE_MS);

        ToolTipManager.sharedInstance().setInitialDelay(TOOLTIP_INITIAL_DELAY_MS);
    }
}

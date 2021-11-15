/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.ui.renderers;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.Unit;
import com.giuseppelamalfa.gameofliferemastered.simulation.SimulationInterface;
import com.giuseppelamalfa.gameofliferemastered.ui.GameStatusPanel;
import com.giuseppelamalfa.gameofliferemastered.ui.GridPanel;
import com.giuseppelamalfa.gameofliferemastered.utils.ImageManager;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;

/**
 *
 * @author glitchedcode
 */
public class TextureGridRenderer implements GridRenderer{

    private int lineSpacing;
    private int sideLength;
    private float tileScale = 1f;

    private ImageManager tileManager = new ImageManager();

    public TextureGridRenderer() {
    }

    public TextureGridRenderer(ImageManager tileManager) {
        this.tileManager = tileManager;
    }

    @Override
    public int getLineSpacing() {
        return lineSpacing;
    }

    @Override
    public int getSideLength() {
        return sideLength;
    }

    BufferedImageOp getUnitFilter(Unit unit, SimulationInterface simulation) {
        return simulation.getSpeciesLoader().getSpeciesFilter(unit.getActualSpeciesID());
    }

    String getUnitTextureCode(Unit unit, SimulationInterface simulation) {
        return simulation.getSpeciesLoader().getSpeciesTextureCode(unit.getSpeciesID());
    }

    @Override
    public void setSideLength(int value) {
        sideLength = Integer.min(64, Integer.max(8, value));
        lineSpacing = sideLength + 1;
        tileScale = sideLength / 8.0f;
    }

    public void drawUnit(Graphics2D g, AffineTransform xform, ImageObserver obs, Unit unit, SimulationInterface simulation) {
        g.setColor(simulation.getPlayerColor(unit.getPlayerID()).getMainAWTColor());
        g.fillRect((int) xform.getTranslateX(), (int) xform.getTranslateY(),
                (int) (xform.getScaleX() * 8), (int) (xform.getScaleY() * 8));

        AffineTransform oldXForm = g.getTransform();

        g.setTransform(xform);
        g.drawImage(tileManager.getImage(getUnitTextureCode(unit, simulation)),
                getUnitFilter(unit, simulation), 0, 0);

        g.setTransform(oldXForm);
    }

    @Override
    public void render(Graphics g, GridPanel panel, Point screenOrigin) {
        Graphics2D g2 = (Graphics2D) g;
        Dimension size = panel.getSize();
        g2.setColor(panel.getBackground());
        g2.fillRect(0, 0, size.width, size.height);

        SimulationInterface simulation = panel.getSimulation();
        if (simulation == null) {
            return;
        }

        GameStatusPanel gameStatusPanel = panel.getGameStatusPanel();
        gameStatusPanel.setTurnCount(simulation.getCurrentTurn());
        gameStatusPanel.setStatus(simulation.getStatusString());

        int yoffset = screenOrigin.y % (lineSpacing);
        int xoffset = screenOrigin.x % (lineSpacing);
        int startRow = screenOrigin.y / lineSpacing;
        int startColumn = screenOrigin.x / lineSpacing;

        int rows, cols;
        int gridRows = simulation.getRowCount(), gridCols = simulation.getColumnCount();
        Dimension gridSize = size;
        int height = Integer.min(size.height, gridSize.height);
        int width = Integer.min(size.width, gridSize.width);

        rows = Integer.min(height / sideLength + 1, gridRows);
        cols = Integer.min(width / sideLength + 1, gridCols);

        // Draw the simulation
        g2.setStroke(new BasicStroke(1));
        g2.setColor(panel.getForeground());
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

                Unit unit = simulation.getUnit(row, col);
                if (unit == null) {
                    continue;
                }

                AffineTransform xform = new AffineTransform();
                xform.translate(xpos, ypos);
                xform.scale(tileScale, tileScale);

                if (unit.isAlive()) {
                    drawUnit(g2, xform, panel, unit, simulation);
                }
            }
        }
    }
}

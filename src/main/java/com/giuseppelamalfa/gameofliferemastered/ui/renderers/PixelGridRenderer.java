/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.ui.renderers;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.Unit;
import com.giuseppelamalfa.gameofliferemastered.simulation.SimulationInterface;
import com.giuseppelamalfa.gameofliferemastered.ui.GridPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

/**
 *
 * @author glitchedcode
 */
public class PixelGridRenderer implements GridRenderer {

    private int lineSpacing;
    private int sideLength;
    private Color bg = new Color(0.0f, 0.0f, 0.0f, 0.1f);

    @Override
    public int getLineSpacing() {
        return lineSpacing;
    }

    @Override
    public int getSideLength() {
        return sideLength;
    }

    @Override
    public void setSideLength(int value) {
        sideLength = Integer.min(64, Integer.max(1, value));
        lineSpacing = sideLength;
    }

    @Override
    public void render(Graphics g, GridPanel panel, Point screenOrigin) {
        Graphics2D g2 = (Graphics2D) g;
        Dimension size = panel.getSize();
        g2.setColor(bg);
        g2.fillRect(0, 0, size.width, size.height);

        SimulationInterface simulation = panel.getSimulation();
        if (simulation == null) {
            return;
        }

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

                g.setColor(unit.getColor());
                g.fillRect(xpos, ypos, lineSpacing, lineSpacing);
            }
        }
    }
}

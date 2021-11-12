/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.ui.renderers;

import com.giuseppelamalfa.gameofliferemastered.ui.GridPanel;
import java.awt.Graphics;
import java.awt.Point;

/**
 *
 * @author glitchedcode
 */
public interface GridRenderer {
    int getLineSpacing();
    int getSideLength();
    void setSideLength(int val);
    void render(Graphics g, GridPanel panel, Point screenOrigin, double delta);
}

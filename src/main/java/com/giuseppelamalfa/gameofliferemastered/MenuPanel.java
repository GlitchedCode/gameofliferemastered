/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;

import java.awt.event.MouseListener;

/**
 *
 * @author glitchedcode
 */
public class MenuPanel extends JPanel implements MouseListener, KeyListener {

    /**
     * Invoked when the mouse button has been clicked (pressed and released) on
     * a component.
     *
     * @param e the event to be processed
     */
    public void mouseClicked(MouseEvent e) {
        //System.out.println("Menu panel clicked.");
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param e the event to be processed
     */
    public void mousePressed(MouseEvent e) {
    }

    /**
     * Invoked when a mouse button has been released on a component.
     *
     * @param e the event to be processed
     */
    public void mouseReleased(MouseEvent e) {
    }

    /**
     * Invoked when the mouse enters a component.
     *
     * @param e the event to be processed
     */
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * Invoked when the mouse exits a component.
     *
     * @param e the event to be processed
     */
    public void mouseExited(MouseEvent e) {
    }

    /**
     * Invoked when a key has been typed. See the class description for
     * {@link KeyEvent} for a definition of a key typed event.
     *
     * @param e the event to be processed
     */
    public void keyTyped(KeyEvent e) {
    }

    /**
     * Invoked when a key has been pressed. See the class description for
     * {@link KeyEvent} for a definition of a key pressed event.
     *
     * @param e the event to be processed
     */
    public void keyPressed(KeyEvent e) {
    }

    /**
     * Invoked when a key has been released. See the class description for
     * {@link KeyEvent} for a definition of a key released event.
     *
     * @param e the event to be processed
     */
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        Dimension size = getSize();
        g.setColor(getBackground());
        g.fillRect(0, 0, size.width, size.height);
    }

    public MenuPanel() {
        addMouseListener(this);
        addKeyListener(this);
    }
}

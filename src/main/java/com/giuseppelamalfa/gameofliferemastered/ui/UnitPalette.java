/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.ui;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.SpeciesLoader;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.UnitInterface;
import com.giuseppelamalfa.gameofliferemastered.utils.ImageManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.io.Serializable;
import java.util.ArrayList;
import javax.swing.JPanel;

class PaletteItem implements Serializable {

    int speciesID;
    public boolean active;
    public int count;

    public PaletteItem(int speciesID, boolean active, int count) throws IllegalArgumentException {
        if (count < 0) {
            throw new IllegalArgumentException("Invalid unit count.");
        }
        this.speciesID = speciesID;
        this.active = active;
        this.count = count;
    }

    public PaletteItem(int speciesID, boolean active) {
        this.speciesID = speciesID;
        this.active = active;
        this.count = -1;
    }

    public int getSpeciesID() {
        return speciesID;
    }
}

/**
 *
 * @author glitchedcode
 */
public class UnitPalette extends JPanel implements MouseListener {

    static public final int ICON_SIDE_LENGTH = 32;
    static public final float ICON_SCALE = ICON_SIDE_LENGTH / 8.0f;
    static public final Color SELECTED_BGCOLOR = new Color(127, 127, 127);
    static public final Color UNSELECTED_BGCOLOR = new Color(0, 0, 0, 255);
    static public final Color INACTIVE_OVERLAY_COLOR = new Color(0, 0, 0, 127);

    protected int selectedIndex = 0;
    protected ArrayList<PaletteItem> items = new ArrayList<>();
    protected ImageManager tileManager;

    /*
    * MOUSE INPUT HANDLING
     */
    @Override
    public void mouseClicked(MouseEvent me) {
        int button = me.getButton();
        if (button == MouseEvent.BUTTON1) {
            int xoff = me.getPoint().x - getLocation().x;
            int newIndex = xoff / (ICON_SIDE_LENGTH + items.size());
            try {
                if (items.get(newIndex).active) {
                    selectedIndex = newIndex;
                }
            } catch (Exception e) {
            }
            repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent me) {
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

    @Override
    public void mousePressed(MouseEvent me) {
    }

    public ArrayList<PaletteItem> getPaletteItems() {
        return items;
    }

    public void addPaletteItem(int speciesID, boolean active, int count) throws Exception {
        boolean canAdd = true;

        for (PaletteItem item : items) {
            if (item.getSpeciesID() == speciesID) {
                canAdd = false;
            }
        }

        if (canAdd) {
            items.add(new PaletteItem(speciesID, active, count));
        }
        resetSize();
        repaint();
    }

    public void addPaletteItem(int speciesID, boolean active) throws Exception {
        boolean canAdd = true;

        for (PaletteItem item : items) {
            if (item.getSpeciesID() == speciesID) {
                canAdd = false;
            }
        }

        if (canAdd) {
            items.add(new PaletteItem(speciesID, active));
        }
        resetSize();
        repaint();
    }

    public void removePaletteItem(int speciesID) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getSpeciesID() == speciesID) {
                selectedIndex--;
                for (; selectedIndex >= 0; selectedIndex--) {
                    if (items.get(selectedIndex).active) {
                        break;
                    }
                }

                items.remove(i);
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public UnitInterface getNewUnit(int playerID) throws Exception {
        PaletteItem item = items.get(selectedIndex);
        if (!item.active | item.count == 0) {
            return null;
        }
        UnitInterface ret = (UnitInterface) SpeciesLoader.getNewUnit(item.getSpeciesID(), playerID);
        if (item.count < 0) {
            item.count--;
        }
        return ret;

    }

    public int getSpeciesAtIndex(int index) {
        PaletteItem item = items.get(index);
        if (item.active) {
            return item.speciesID;
        } else {
            return -1;
        }
    }

    public int getSpeciesCount() {
        if (items.isEmpty()) {
            return 0;
        }
        PaletteItem item = items.get(selectedIndex);
        if (!item.active) {
            return 0;
        }
        if (item.count < 0) {
            return 99;
        }
        return items.get(selectedIndex).count;
    }

    public int getItemCount() {
        return items.size();
    }

    public Object getPaletteObject() {
        return items;
    }

    @SuppressWarnings("unchecked")
    public void setPaletteObject(Object arg) {
        items = (ArrayList<PaletteItem>) arg;
        resetSize();
    }

    public void resetSize() {
        setSize(items.size() * (ICON_SIDE_LENGTH + 1) + 1, ICON_SIDE_LENGTH + 2);
    }

    @Override
    public void paintComponent(Graphics g) {

        if (items.isEmpty()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        Dimension size = getSize();
        Point pos = getLocation();
        g2.setColor(UNSELECTED_BGCOLOR);
        g2.fillRect(0, 0, size.width, size.height);

        int offset = selectedIndex * (1 + ICON_SIDE_LENGTH);
        g2.setColor(SELECTED_BGCOLOR);
        g2.fillRect(offset + 1, 1, ICON_SIDE_LENGTH, ICON_SIDE_LENGTH);

        g2.setStroke(new BasicStroke(1));

        for (int i = 0; i < items.size(); i++) {
            PaletteItem item = items.get(i);
            offset = i * (1 + ICON_SIDE_LENGTH);
            // icon
            AffineTransform xform = new AffineTransform();
            xform.translate(offset, 1);
            xform.scale(ICON_SCALE, ICON_SCALE);
            Image img = tileManager.getImage(SpeciesLoader.getSpecies(item.speciesID).textureCode);
            g2.drawImage(img, xform, this);

            g2.setColor(getForeground());
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            offset = i * (1 + ICON_SIDE_LENGTH);

            // count
            if (item.count >= 0) {
                g2.drawString(Integer.toString(item.count), offset, 1);
            }

            if (!item.active | item.count == 0) // inactive icon
            {
                g2.setColor(INACTIVE_OVERLAY_COLOR);
                g2.fillRect(offset, 1, offset, size.height - 1);
            }
        }
    }

    public void init(ImageManager tileManager) throws Exception {
        addMouseListener(this);
        this.tileManager = tileManager;
        for (int c = 0; c < SpeciesLoader.getSpeciesCount(); c++) {
            addPaletteItem(c, true);
        }
    }
}

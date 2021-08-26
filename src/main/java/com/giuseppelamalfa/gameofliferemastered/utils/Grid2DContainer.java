/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author glitchedcode
 * @param <T> Stored type
 */
public class Grid2DContainer<T> implements Cloneable, Serializable {

    private HashMap<Integer, T> map = new HashMap<>();
    private int rows;
    private int cols;

    private boolean hasDefault = false;
    private T defaultValue = null;

    // Initializes the ArrayList objects 
    public Grid2DContainer(int rows, int cols, T sparseDefault) throws IllegalArgumentException {
        super();
        if (rows < 0 | cols < 0) // sanity check
        {
            throw new IllegalArgumentException("Invalid size values for TwoDimensionalArrayList");
        }
        this.rows = rows;
        this.cols = cols;
        resize(rows, cols);
        this.defaultValue = sparseDefault;
        hasDefault = sparseDefault != null;
    }

    // Initializes the ArrayList objects 
    public Grid2DContainer(int rows, int cols) throws IllegalArgumentException {
        this(rows, cols, null);
    }

    public synchronized boolean hasDefaultValue() {
        return hasDefault;
    }

    public synchronized void setDefaultValue(T val) {
        defaultValue = val;
        hasDefault = val != null;
    }

    // Resets the minimum capacity for all the ArrayList objects
    public synchronized final void resize(int rows, int cols) throws IllegalArgumentException {
        if (rows < 0 | cols < 0) // sanity check
        {
            throw new IllegalArgumentException("Invalid size values for TwoDimensionalArrayList");
        }
        ArrayList<Integer> keys = new ArrayList<>();
        for (Integer key : map.keySet()) {
            int row = getRow(key);
            int col = getColumn(key);
            if (row >= rows | col >= cols) {
                keys.add(key);
            }
        }

        for (Integer key : keys) {
            map.remove(key);
        }

        this.rows = rows;
        this.cols = cols;
    }

    // Gets element at (row, col) coordinates
    public synchronized final T get(int row, int col) {
        int key = getKeyFromCoords(row, col);
        if (map.containsKey(key)) {
            return map.get(key);
        }

        if (hasDefault) {
            return defaultValue;
        }

        return null;
    }

    // Puts element at (row, col) coordinates
    public synchronized void put(Integer row, Integer col, T element) {
        if (row >= rows | col >= cols | row < 0 | col < 0) {
            return;
        }

        if (element == null || element == defaultValue) {
            remove(row, col);
        } else {
            map.put(getKeyFromCoords(row, col), element);
        }
    }

    public synchronized void remove(Integer row, Integer col) {
        map.remove(getKeyFromCoords(row, col));
    }

    public synchronized void clear() {
        map.clear();
        try {
            resize(rows, cols);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized Object clone() {
        ConcurrentGrid2DContainer<T> ret;
        try {
            ret = new ConcurrentGrid2DContainer<>(rows, cols, defaultValue);
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    ret.put(r, c, get(r, c));
                }
            }

            return ret;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static int getRow(int key) {
        return key >> 16;
    }

    private static int getColumn(int key) {
        return key & 0x000000FF;
    }

    private static int getKeyFromCoords(int row, int col) {
        return row << 16 | col;
    }

}

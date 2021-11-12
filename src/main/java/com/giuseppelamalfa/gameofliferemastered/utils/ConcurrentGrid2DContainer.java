/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.utils;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.grid.Grid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author glitchedcode
 * @param <T> Stored type
 */
public class ConcurrentGrid2DContainer<T> implements Cloneable, Serializable {

    private final ConcurrentHashMap<Integer, T> map;
    private int rows;
    private int cols;

    private final T defaultValue;

    // Initializes the ArrayList objects 
    public ConcurrentGrid2DContainer(int rows, int cols, T sparseDefault) throws IllegalArgumentException {
        super();
        if (rows < 0 | cols < 0) // sanity check
        {
            throw new IllegalArgumentException("Invalid size values for TwoDimensionalArrayList");
        }
        this.rows = rows;
        this.cols = cols;
        this.defaultValue = sparseDefault;
        
        map = new ConcurrentHashMap<>(rows*cols, 0.9f, Grid.PROCESSOR_COUNT);

        resize(rows, cols);
    }

    // Initializes the ArrayList objects 
    public ConcurrentGrid2DContainer(int rows, int cols) throws IllegalArgumentException {
        this(rows, cols, null);
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    // Resets the minimum capacity for all the ArrayList objects
    public final void resize(int rows, int cols) throws IllegalArgumentException {
        if (rows < 0 | cols < 0) // sanity check
        {
            throw new IllegalArgumentException("Invalid size values for TwoDimensionalArrayList");
        }
        ArrayList<Integer> keys = new ArrayList<>();
        map.keySet().forEach(key -> {
            int row = getRow(key);
            int col = getColumn(key);
            if (row >= rows | col >= cols) {
                keys.add(key);
            }
        });

        keys.forEach(key -> {
            map.remove(key);
        });

        this.rows = rows;
        this.cols = cols;
    }

    // Gets element at (row, col) coordinates
    public T get(int row, int col) {
        int key = row << 16 | col;
        T ret;
        ret = map.get(key);
        if (ret == null) {
            return defaultValue;
        }
        return ret;
    }

    // Puts element at (row, col) coordinates
    public void put(Integer row, Integer col, T element) {
        if (row >= rows | col >= cols | row < 0 | col < 0) {
            return;
        }

        map.put(row << 16 | col, element);
    }

    public void remove(Integer row, Integer col) {
        map.remove(row << 16 | col);
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

    public final int getRowCount() {
        return rows;
    }

    public final int getColumnCount() {
        return cols;
    }

    private static int getRow(int key) {
        return key >> 16;
    }

    private static int getColumn(int key) {
        return key & 0x0000FFFF;
    }

    private static int getKeyFromCoords(int row, int col) {
        return row << 16 | col;
    }

}

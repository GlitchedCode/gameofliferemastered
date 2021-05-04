/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.utils;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.Grid;
import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author glitchedcode
 * @param <T> Stored type
 */
public class TwoDimensionalContainer<T> extends HashMap<Integer, HashMap<Integer, T>> implements Cloneable, Serializable{

    private int rows;
    private int cols;
    
    // Initializes the ArrayList objects 
    public TwoDimensionalContainer(int rows, int cols) throws Exception
    {
        super();        
        if (rows < 0 | cols < 0) // sanity check
        {
            throw new Exception("Invalid size values for TwoDimensionalArrayList");
        }
        this.rows = rows;
        this.cols = cols;
        resize(rows, cols);
    }
    
    // Resets the minimum capacity for all the ArrayList objects
    public final void resize(int rows, int cols) throws Exception
    {
        if (rows < 0 | cols < 0) // sanity check
        {
            throw new Exception("Invalid size values for TwoDimensionalArrayList");
        }
        
        if (cols < this.cols) // remove extra columns
        {
            for (int i = cols; i < this.cols; i++)
            {
                super.remove(i);
            }
        }
        
        for (int i = 0; i < cols; i++) // column cycle
        {
            HashMap<Integer, T> column = null;
            
            if (super.containsKey(i))
            {
                column = super.get(i);
            }
            
            if (column != null)
            {
                if (rows < this.rows) // remove extra rows
                {
                    for (int j = rows; j < this.rows; j++)
                    {
                        column.remove(j);
                    }
                }
            }
            else
            {
                super.put(i, new HashMap<>());
            }
        }
    }
    
    // Gets element at (row, col) coordinates
    public final T get(int row, int col)
    {
        if (super.containsKey(col))
        {
            if (super.get(col).containsKey(row))
            {
                return super.get(col).get(row);
            }
        }
        
        return null;
    }
    
    // Puts element at (row, col) coordinates
    public void put(Integer row, Integer col, T element)
    {
        if (row >= rows | col >= cols | row < 0 | col < 0)
            return;
        
        super.get(col).put(row, element);            
    }
    
    public void remove(Integer row, Integer col)
    {

        if (super.containsKey(col))
        {
            if (super.get(col).containsKey(row))
            {
                super.get(col).remove(row);
            }
        }
    }
    
    @Override
    public void clear()
    {
        super.clear();
        try {
            resize(rows, cols);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Object clone() {
        TwoDimensionalContainer<T> ret;
        try {
            ret = new TwoDimensionalContainer<>(rows, cols);
            for(int r = 0; r < rows; r++)
            for(int c = 0; c < cols; c++)
                ret.put(r, c, get(r, c));
        
            return ret;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}

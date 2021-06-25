/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.grid;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.simulation.SimulationInterface;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author glitchedcode
 */
public enum GameMode {
    SANDBOX(Grid.class, true),
    COMPETITIVE(CompetitiveGrid.class, false);

    public final Constructor<?> constructor;
    public final boolean controlledByHost;

    GameMode(Class<?> clazz, boolean locallyControlled) {
        Constructor<?> tmp;
        try {
            tmp = clazz.getConstructor(Integer.class, Integer.class);
        } catch (Exception ex) {
            tmp = null;
            Logger.getLogger(GameMode.class.getName()).log(Level.SEVERE, null, ex);
        }
        constructor = tmp;
        this.controlledByHost = locallyControlled;
    }

    public Grid getNewGrid(int rows, int cols) {
        try {
            return (Grid) constructor.newInstance(rows, cols);
        } catch (InstantiationException ex) {
            Logger.getLogger(GameMode.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(GameMode.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(GameMode.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(GameMode.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}

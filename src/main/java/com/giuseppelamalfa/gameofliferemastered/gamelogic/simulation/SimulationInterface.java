/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.simulation;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.PlayerData;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.request.InvalidRequestException;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.request.Request;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.SpeciesLoader;
import java.io.IOException;
import java.util.ArrayList;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.Unit;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author glitchedcode
 */
public abstract class SimulationInterface {
    
    public abstract boolean isLocked();

    public abstract boolean isRunning();

    public abstract boolean isLocallyControlled();

    public abstract String getGameModeName();

    public abstract int getLocalPlayerID();

    public abstract PlayerData.TeamColor getPlayerColor(int ID);

    public abstract ArrayList<PlayerData> getPlayerRankings();
    
    public abstract SpeciesLoader getSpeciesLoader();

    public abstract int getRowCount();

    public abstract int getColumnCount();

    public abstract void resize(int rows, int cols);

    public abstract int getSectorSideLength();

    public abstract String getStatusString();

    public abstract int getCurrentTurn();

    public abstract Unit getUnit(int row, int col);

    public abstract void removeUnit(int row, int col);

    public abstract void setUnit(int row, int col, Unit unit);

    public abstract void computeNextTurn() throws Exception;

    public abstract void setRunning(boolean val);

    public void handleRequest(Request request, int clientID) throws IOException, InvalidRequestException {
        try {
            Class<?> clazz = getClass();
            Method method = clazz.getDeclaredMethod(request.type.procedureName,
                    Request.class, Integer.class);
            method.invoke(this, request, clientID);
        } catch (NoSuchMethodException e) {
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    public abstract void synchronize();

    public abstract void saveGrid() throws Exception;
    
    public abstract void close();
}

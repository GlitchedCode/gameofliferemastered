/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.simulation;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.PlayerData;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.request.InvalidRequestException;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.request.Request;
import java.io.IOException;
import java.util.ArrayList;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.Unit;

/**
 *
 * @author glitchedcode
 */
public interface SimulationInterface {

    boolean isLocked();

    boolean isRunning();

    boolean isLocallyControlled();

    String getGameModeName();

    int getLocalPlayerID();

    PlayerData.TeamColor getPlayerColor(int ID);

    ArrayList<PlayerData> getPlayerRankings();

    int getRowCount();

    int getColumnCount();

    void resize(int rows, int cols);

    int getSectorSideLength();

    String getStatusString();

    int getCurrentTurn();

    Unit getUnit(int row, int col);

    void removeUnit(int row, int col);

    void setUnit(int row, int col, Unit unit);

    void computeNextTurn() throws Exception;

    void setRunning(boolean val);

    void handleRequest(Request requestObject, int ID) throws IOException, InvalidRequestException;

    void synchronize();

    void close();
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.simulation;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.request.UpdatePlayerDataRequest;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.request.SetUnitRequest;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.request.SyncGridRequest;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.request.InvalidRequestException;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.request.Request;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.request.LogMessageRequest;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.request.DisconnectRequest;
import com.giuseppelamalfa.gameofliferemastered.ApplicationFrame;
import com.giuseppelamalfa.gameofliferemastered.ui.GridPanel;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.PlayerData;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.grid.GameMode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.Unit;

/**
 *
 * @author glitchedcode
 */
public class SimulationGUIServer extends SimulationCLIServer {

    static protected final ArrayList<PlayerData> offlineRanking = new ArrayList<>();
    
    PlayerData localPlayerData;
    GridPanel panel;

    public SimulationGUIServer(String playerName, int portNumber, int playerCount,
            int rowCount, int columnCount, GameMode mode) throws Exception {
        super(portNumber, playerCount, rowCount, columnCount, mode);
        playerCount--;
        localPlayerData = new PlayerData(playerName, extractRandomColor());
        localPlayerData.ID = getNextClientID();
        currentGrid.addPlayer(localPlayerData);
    }

    public SimulationGUIServer(int rowCount, int columnCount) throws Exception {
        super(rowCount, columnCount);
        localPlayerData = new PlayerData();
        localPlayerData.ID = getNextClientID();
        currentGrid.addPlayer(localPlayerData);
    }

    @Override
    protected void writeToStatusLog(String msg) {
        ApplicationFrame.writeToStatusLog(msg);
    }

    @Override
    public ArrayList<PlayerData> getPlayerRankings() {
        if (isRemoteInstance()) {
            return currentGrid.getPlayerRankings();
        } else {
            return offlineRanking;
        }
    }

    @Override
    public void removeUnit(int row, int col) {
        SetUnitRequest req = new SetUnitRequest(row, col, null);
        try {
            handleRequest(req, -1);
        } catch (IOException ex) {
            Logger.getLogger(SimulationGUIServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidRequestException ex) {
            Logger.getLogger(SimulationGUIServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void setUnit(int row, int col, Unit unit) {
        SetUnitRequest req = new SetUnitRequest(row, col, unit);
        try {
            handleRequest(req, 0);
        } catch (IOException ex) {
            Logger.getLogger(SimulationGUIServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidRequestException ex) {
            Logger.getLogger(SimulationGUIServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void computeNextTurn() throws Exception {
        super.computeNextTurn();
        panel.getGameStatusPanel().setShowWinner(currentGrid.showWinner());
    }

    @Override
    public void synchronize() {
        panel.getGameStatusPanel().setShowWinner(currentGrid.showWinner());
        super.synchronize();
    }

    public void initializeGridPanel(GridPanel panel) {
        this.panel = panel;
    }

    @Override
    protected void handleUpdatePlayerDataRequest(Request r, Integer clientID) {
        ClientData data = connectedClients.get(clientID);
        if (data == null) {
            return;
        }
        UpdatePlayerDataRequest updateRequest = (UpdatePlayerDataRequest) r;
        PlayerData newPlayerData = updateRequest.playerData;

        if (!updateRequest.connected) {
            currentGrid.removePlayer(newPlayerData.ID);
            UpdatePlayerDataRequest disconnectRequest = new UpdatePlayerDataRequest(newPlayerData, false);
            sendToAll(disconnectRequest, newPlayerData.ID);
        } else if (updateRequest.updateLocal) {
            if (newPlayerData.name != null) {
                if (data.playerData.name == null) {
                    sendLogMessage(newPlayerData.name + " joined the game.");
                }
                data.playerData.name = newPlayerData.name;
            }
            if (newPlayerData.color != PlayerData.TeamColor.NONE
                    & availableColors.contains(newPlayerData.color)) {
                if (data.playerData.color != PlayerData.TeamColor.NONE) {
                    availableColors.add(data.playerData.color);
                }
                data.playerData.color = newPlayerData.color;
                availableColors.remove(newPlayerData.color);
            }

            currentGrid.addPlayer(data.playerData);
            UpdatePlayerDataRequest clientUpdateRequest
                    = new UpdatePlayerDataRequest(data.playerData, true);
            sendToAll(clientUpdateRequest, newPlayerData.ID);
        }
        panel.getGameStatusPanel().setPlayerPanels(getPlayerRankings());
    }

    @Override
    protected void handleDisconnectRequest(Request r, Integer clientID) {
        ClientData data = connectedClients.get(clientID);
        if (data == null) {
            return;
        }
        DisconnectRequest disconnect = (DisconnectRequest) r;

        ApplicationFrame.writeToStatusLog(data.playerData.name + " disconnected: " + disconnect.message);
        connectedClients.remove(clientID);
        try {
            data.socket.close();
        } catch (IOException ex) {
            Logger.getLogger(SimulationGUIServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void handleSetUnitRequest(Request r, Integer clientID) {
        ClientData data = connectedClients.get(clientID);
        PlayerData playerData;
        if (data == null) {
            playerData = localPlayerData;
        } else {
            playerData = data.playerData;
        }

        SetUnitRequest setUnit = (SetUnitRequest) r;
        sendToAll(r, clientID);

        if (setUnit.unit != null) { // set unit
            if (currentGrid.getUnit(setUnit.row, setUnit.col).isAlive()) {
                return;
            }
            currentGrid.setUnit(setUnit.row, setUnit.col, setUnit.unit);

        } else { // remove unit
            Unit unit = currentGrid.getUnit(setUnit.row, setUnit.col);
            if (!unit.isAlive()) {
                return;
            }
            if (unit.getPlayerID() != playerData.ID) {
                return;
            }
            currentGrid.removeUnit(setUnit.row, setUnit.col);
        }

        panel.getGameStatusPanel().setPlayerPanels(getPlayerRankings());

    }

    @Override
    public void sendLogMessage(String msg) {
        ApplicationFrame.writeToStatusLog(msg);
        LogMessageRequest req = new LogMessageRequest(msg);
        try {
            for (Integer key : connectedClients.keySet()) {
                connectedClients.get(key).stream.writeObject(req);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getLocalPlayerID() {
        return localPlayerData.ID;
    }
    
    @Override
    public boolean isLocallyControlled() {
        return true;
    }
}

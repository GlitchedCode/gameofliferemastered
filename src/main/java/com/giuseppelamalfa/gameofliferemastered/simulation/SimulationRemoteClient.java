/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.simulation;

import com.giuseppelamalfa.gameofliferemastered.simulation.request.GameStatusRequest;
import com.giuseppelamalfa.gameofliferemastered.simulation.request.InvalidRequestException;
import com.giuseppelamalfa.gameofliferemastered.simulation.request.SetUnitRequest;
import com.giuseppelamalfa.gameofliferemastered.simulation.request.SyncSpeciesDataRequest;
import com.giuseppelamalfa.gameofliferemastered.simulation.request.Request;
import com.giuseppelamalfa.gameofliferemastered.simulation.request.LogMessageRequest;
import com.giuseppelamalfa.gameofliferemastered.simulation.request.UpdatePlayerDataRequest;
import com.giuseppelamalfa.gameofliferemastered.simulation.request.SyncGridRequest;
import com.giuseppelamalfa.gameofliferemastered.simulation.request.DisconnectRequest;
import com.giuseppelamalfa.gameofliferemastered.ApplicationFrame;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.GameLogicException;
import com.giuseppelamalfa.gameofliferemastered.ui.GridPanel;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.grid.Grid;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.PlayerData;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.SpeciesLoader;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.Unit;
import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author glitchedcode
 */
public class SimulationRemoteClient extends SimulationInterface {

    Grid currentGrid;

    GridPanel panel = new GridPanel();
    Socket clientSocket;
    ObjectOutputStream outputStream;
    String host;
    int portNumber;
    PlayerData localPlayerData;
    SpeciesLoader speciesLoader = new SpeciesLoader();

    ArrayList<DisconnectEventListener> disconnectListeners = new ArrayList<>();

    public void addDisconnectEventListener(DisconnectEventListener listener) {
        disconnectListeners.add(listener);
    }

    public SimulationRemoteClient(String playerName, String host, int portNumber) throws IOException, Exception {
        localPlayerData = new PlayerData(playerName);
        init(host, portNumber);
    }

    boolean requestedClosure = false;

    void init(String host, int portNumber) throws IOException, Exception {
        clientSocket = new Socket(host, portNumber);
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());

        outputStream.writeObject(new UpdatePlayerDataRequest(localPlayerData, true, true));
        ApplicationFrame.writeToStatusLog("Connected to " + host + ":" + portNumber);

        new Thread(()
                -> {
            try {
                ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
                while (true) {
                    Request r = (Request) input.readObject();
                    handleRequest(r, 0);
                }
            } catch (EOFException | SocketException e) {
            } catch (InvalidRequestException | IOException | ClassNotFoundException e) {
                e.printStackTrace();
                currentGrid.setRunning(false);
            }
            try {
                clientSocket.close();
            } catch (IOException a) {
            }

            if (!requestedClosure) {
                disconnectListeners.forEach((el) -> el.onDisconnect());
            }
        }).start();

        currentGrid = new Grid(1, 1);
    }

    @Override
    public boolean isLocked() {
        return currentGrid.isLocked();
    }

    @Override
    public boolean isRunning() {
        return currentGrid.isRunning();
    }

    @Override
    public boolean isLocallyControlled() {
        return false;
    }

    @Override
    public String getGameModeName() {
        return currentGrid.getGameModeName();
    }

    @Override
    public int getLocalPlayerID() {
        return localPlayerData.ID;
    }

    @Override
    public PlayerData.TeamColor getPlayerColor(int ID) {
        return currentGrid.getPlayerColor(ID);
    }

    @Override
    public ArrayList<PlayerData> getPlayerRankings() {
        return currentGrid.getPlayerRankings();
    }
    
    @Override
    public SpeciesLoader getSpeciesLoader(){
        return speciesLoader;
    }

    @Override
    public int getRowCount() {
        return currentGrid.getRowCount();
    }

    @Override
    public int getColumnCount() {
        return currentGrid.getColumnCount();
    }

    @Override
    public int getSectorSideLength() {
        return currentGrid.SECTOR_SIDE_LENGTH;
    }

    @Override
    public String getStatusString() {
        return currentGrid.getStatusString();
    }

    @Override
    public int getCurrentTurn() {
        return currentGrid.getCurrentTurn();
    }

    @Override
    public Unit getUnit(int row, int col) {
        return currentGrid.getUnit(row, col);
    }

    @Override
    public void removeUnit(int row, int col) {
        Unit unit = currentGrid.getUnit(row, col);
        if (unit == null) {
            return;
        }
        if (unit.getPlayerID() != localPlayerData.ID) {
            return;
        }

        try {
            outputStream.writeObject(new SetUnitRequest(row, col, null));
        } catch (IOException ex) {
            Logger.getLogger(SimulationRemoteClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        currentGrid.removeUnit(row, col);
    }

    @Override
    public void setUnit(int row, int col, Unit unit) {

        if (currentGrid.getUnit(row, col).isAlive()) {
            return;
        }

        try {
            outputStream.writeObject(new SetUnitRequest(row, col, unit));
        } catch (IOException ex) {
            Logger.getLogger(SimulationRemoteClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            currentGrid.setUnit(row, col, unit);
        } catch (GameLogicException ex) {
            Logger.getLogger(SimulationRemoteClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void computeNextTurn() throws Exception {
        currentGrid.computeNextTurn(speciesLoader);
    }

    @Override
    public void setRunning(boolean val) {
        try {
            outputStream.writeObject(new GameStatusRequest(val));
        } catch (IOException ex) {
            Logger.getLogger(SimulationRemoteClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void handleLogMessageRequest(Request r, Integer ID) {
        ApplicationFrame.writeToStatusLog(((LogMessageRequest) r).message);
    }

    void handleSyncSpeciesDataRequest(Request r, Integer ID) {
        SyncSpeciesDataRequest speciesData = (SyncSpeciesDataRequest) r;
        try {
            speciesLoader.loadJSONString(speciesData.jsonString);
            panel.getPalette().resetPaletteItems();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void handleSyncGridRequest(Request r, Integer ID) {
        SyncGridRequest sync = (SyncGridRequest) r;
        if (sync.grid != null) {
            currentGrid = sync.grid;
            currentGrid.afterSync();
            currentGrid.setSimulation(this);
            //currentGrid.setPlayerIDCheckNextTurn();
            currentGrid.addPlayer(localPlayerData);
            if (!currentGrid.showWinner()) {
                currentGrid.calculateScore();
            }
        }

        if (sync.skipTurn) {
            try {
                computeNextTurn();
            } catch (Exception ex) {
                Logger.getLogger(SimulationRemoteClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        panel.getGameStatusPanel().setPlayerPanels(getPlayerRankings());
        panel.getGameStatusPanel().setShowWinner(currentGrid.showWinner());
    }

    void handleUpdatePlayerDataRequest(Request r, Integer ID) {
        UpdatePlayerDataRequest updateRequest = (UpdatePlayerDataRequest) r;
        PlayerData playerData = updateRequest.playerData;

        if (updateRequest.updateLocal) {
            if (playerData.color != PlayerData.TeamColor.NONE) {
                localPlayerData.color = playerData.color;
            }
            if (playerData.ID != -1) {
                currentGrid.removePlayer(localPlayerData.ID);
                localPlayerData.ID = playerData.ID;
                currentGrid.addPlayer(localPlayerData);
            }
        }
        if (playerData.ID != localPlayerData.ID) {
            if (updateRequest.connected) {
                currentGrid.addPlayer(playerData);
            } else {
                currentGrid.removePlayer(playerData.ID);
            }
        }
        try {
            panel.getGameStatusPanel().setPlayerPanels(getPlayerRankings());
        } catch (Exception e) {
            // IDK DUDE XDDDDDDDDDD
        }
    }

    void handleDisconnectRequest(Request r, Integer ID) {
        String msg = ((DisconnectRequest) r).message;
        ApplicationFrame.writeToStatusLog("Disconnected from " + clientSocket.getRemoteSocketAddress()
                + ": " + msg);
        try {
            clientSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(SimulationRemoteClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        setRunning(false);

    }

    void handleGameStatusRequest(Request r, Integer ID) {
        GameStatusRequest req = ((GameStatusRequest) r);
        currentGrid.setRunning(req.running);
    }

    void handleSetUnitRequest(Request r, Integer ID) throws GameLogicException {
        SetUnitRequest setUnit = (SetUnitRequest) r;
        if (setUnit.unit != null) {
            currentGrid.setUnit(setUnit.row, setUnit.col, setUnit.unit);
        } else {
            currentGrid.removeUnit(setUnit.row, setUnit.col);
        }
        panel.getGameStatusPanel().setPlayerPanels(getPlayerRankings());

    }

    @Override
    public void synchronize() {
    }

    public void initializeGridPanel(GridPanel panel) {
        this.panel = panel;
    }
    
    @Override
    public void loadGrid(File file) throws Exception {
        currentGrid.readBoardFromFile(file, speciesLoader);
    }

    @Override
    public void saveGrid() throws Exception{
        LocalDateTime now = LocalDateTime.now();
        String isoFormat = DateTimeFormatter.ISO_INSTANT.format(now.toInstant(ZoneOffset.UTC));
        currentGrid.writeBoardToFile("grid-"+isoFormat);
    }
    
    @Override
    public void close() {
        requestedClosure = true;
        try {
            outputStream.writeObject(new DisconnectRequest("Disconnection requested."));
        } catch (Exception e) {
        }
        try {
            clientSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(SimulationRemoteClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            panel.getPalette().resetPaletteItems();
        } catch (Exception e) {

        }
    }

    @Override
    public void resize(int rows, int cols) {
    }
}

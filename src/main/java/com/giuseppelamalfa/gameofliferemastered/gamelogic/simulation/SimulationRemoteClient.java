/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.simulation;

import com.giuseppelamalfa.gameofliferemastered.ApplicationFrame;
import com.giuseppelamalfa.gameofliferemastered.ui.GridPanel;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.grid.Grid;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.PlayerData;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.requests.*;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.UnitInterface;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author glitchedcode
 */
public class SimulationRemoteClient implements SimulationInterface {

    Grid currentGrid;

    GridPanel panel;
    Socket clientSocket;
    ObjectOutputStream outputStream;
    String host;
    int portNumber;
    PlayerData localPlayerData;

    public SimulationRemoteClient(String playerName, String host, int portNumber) throws IOException, Exception {
        localPlayerData = new PlayerData(playerName);
        init(host, portNumber);
    }

    public void init(String host, int portNumber) throws IOException, Exception {
        clientSocket = new Socket(host, portNumber);
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());

        outputStream.writeObject(new UpdatePlayerDataRequest(localPlayerData, true, true));
        ApplicationFrame.writeToStatusLog("Connected to " + host + ":" + portNumber);

        new Thread(() -> {
            try {
                ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
                while (true) {
                    handleRequest((Request) input.readObject(), 0);
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
    public UnitInterface getUnit(int row, int col) {
        return currentGrid.getUnit(row, col);
    }

    @Override
    public void removeUnit(int row, int col) {
        try {
            outputStream.writeObject(new SetUnitRequest(row, col, null));
        } catch (IOException ex) {
            Logger.getLogger(SimulationRemoteClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        currentGrid.removeUnit(row, col);
    }

    @Override
    public void setUnit(int row, int col, UnitInterface unit) {
        try {
            outputStream.writeObject(new SetUnitRequest(row, col, unit));
        } catch (IOException ex) {
            Logger.getLogger(SimulationRemoteClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        currentGrid.setUnit(row, col, unit);
    }

    @Override
    public synchronized void computeNextTurn() throws Exception {
        currentGrid.computeNextTurn();
    }

    @Override
    public void setRunning(boolean val) {
        if (isLocallyControlled()) {
            setRunning(val);
        } else {
            try {
                outputStream.writeObject(new GameStatusRequest(val));
            } catch (IOException ex) {
                Logger.getLogger(SimulationRemoteClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void handleRequest(Request request, int ID)
            throws IOException, InvalidRequestException {
        switch (request.getType()) {
            case LOG_MESSAGE:
                ApplicationFrame.writeToStatusLog(((LogMessageRequest) request).message);
                break;
            case SYNC_GRID:
                currentGrid = (Grid) ((SyncGridRequest) request).grid;
                currentGrid.setSimulation(this);
                currentGrid.setPlayerIDCheckNextTurn();
                currentGrid.addPlayer(localPlayerData);
                currentGrid.afterSync();
                if (!currentGrid.showWinner()) {
                    currentGrid.calculateScore();
                }
                panel.getGameStatusPanel().setPlayerPanels(getPlayerRankings());
                panel.getGameStatusPanel().setShowWinner(currentGrid.showWinner());
                break;
            case UPDATE_PLAYER_DATA:
                UpdatePlayerDataRequest updateRequest = (UpdatePlayerDataRequest) request;
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
                break;
            case DISCONNECT:
                String msg = ((DisconnectRequest) request).message;
                ApplicationFrame.writeToStatusLog("Disconnected from " + clientSocket.getRemoteSocketAddress()
                        + ": " + msg);
                clientSocket.close();
                setRunning(false);
                break;
            case PAUSE:
                setRunning(((GameStatusRequest) request).running);
                break;
            case SET_UNIT:
                SetUnitRequest setUnit = (SetUnitRequest) request;
                if (setUnit.unit != null) {
                    currentGrid.setUnit(setUnit.row, setUnit.col, setUnit.unit);
                } else {
                    currentGrid.removeUnit(setUnit.row, setUnit.col);
                }
                panel.getGameStatusPanel().setPlayerPanels(getPlayerRankings());
                break;
        }
    }

    @Override
    public void synchronize() {
    }

    @Override
    public void initializeGridPanel(GridPanel panel) {
        this.panel = panel;
    }

    @Override
    public void close() {
        try {
            outputStream.writeObject(new UpdatePlayerDataRequest(localPlayerData, false));
            clientSocket.close();
        } catch (Exception e) {
            System.err.println("e");
        }
    }

    public void resize(int rows, int cols) {
    }
}

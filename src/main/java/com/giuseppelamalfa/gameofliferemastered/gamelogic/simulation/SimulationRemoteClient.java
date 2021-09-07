/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.simulation;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.request.GameStatusRequest;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.request.InvalidRequestException;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.request.SetUnitRequest;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.request.SyncSpeciesDataRequest;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.request.Request;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.request.LogMessageRequest;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.request.UpdatePlayerDataRequest;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.request.SyncGridRequest;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.request.DisconnectRequest;
import com.giuseppelamalfa.gameofliferemastered.ApplicationFrame;
import com.giuseppelamalfa.gameofliferemastered.ui.GridPanel;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.grid.Grid;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.PlayerData;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.SpeciesLoader;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.Unit;

/**
 *
 * @author glitchedcode
 */
public class SimulationRemoteClient extends SimulationInterface {

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

        new Thread(()
                -> {
            try {
                ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
                while ( true ) {
                    Request r = (Request) input.readObject();
                    handleRequest(r, 0);
                }
            }
            catch (EOFException | SocketException e) {
            }
            catch (InvalidRequestException | IOException | ClassNotFoundException e) {
                e.printStackTrace();
                currentGrid.setRunning(false);
            }
            try {
                clientSocket.close();
            }
            catch (IOException a) {
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
    public Unit getUnit(int row, int col) {
        return currentGrid.getUnit(row, col);
    }

    @Override
    public void removeUnit(int row, int col) {
        Unit unit = currentGrid.getUnit(row, col);
        if ( unit == null ) {
            return;
        }
        if ( unit.getPlayerID() != localPlayerData.ID ) {
            return;
        }

        try {
            outputStream.writeObject(new SetUnitRequest(row, col, null));
        }
        catch (IOException ex) {
            Logger.getLogger(SimulationRemoteClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        currentGrid.removeUnit(row, col);
    }

    @Override
    public void setUnit(int row, int col, Unit unit) {

        if ( currentGrid.getUnit(row, col).isAlive()) {
            return;
        }

        try {
            outputStream.writeObject(new SetUnitRequest(row, col, unit));
        }
        catch (IOException ex) {
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
        try {
            outputStream.writeObject(new GameStatusRequest(val));
        }
        catch (IOException ex) {
            Logger.getLogger(SimulationRemoteClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void handleLogMessageRequest(Request r, Integer ID) {
        ApplicationFrame.writeToStatusLog(((LogMessageRequest) r).message);
    }

    private void handleSyncSpeciesDataRequest(Request r, Integer ID) {
        SyncSpeciesDataRequest speciesData = (SyncSpeciesDataRequest) r;
        try {
            SpeciesLoader.loadJSONString(speciesData.jsonString);
            panel.getPalette().resetPaletteItems();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void handleSyncGridRequest(Request r, Integer ID) {
        SyncGridRequest sync = (SyncGridRequest) r;
        if ( sync.grid != null ) {
            currentGrid = sync.grid;
            currentGrid.afterSync();
            currentGrid.setSimulation(this);
            currentGrid.setPlayerIDCheckNextTurn();
            currentGrid.addPlayer(localPlayerData);
            if ( !currentGrid.showWinner() ) {
                currentGrid.calculateScore();
            }
        }

        if ( sync.skipTurn ) {
            try {
                computeNextTurn();
            }
            catch (Exception ex) {
                Logger.getLogger(SimulationRemoteClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        panel.getGameStatusPanel().setPlayerPanels(getPlayerRankings());
        panel.getGameStatusPanel().setShowWinner(currentGrid.showWinner());

    }

    private void handleUpdatePlayerDataRequest(Request r, Integer ID) {
        UpdatePlayerDataRequest updateRequest = (UpdatePlayerDataRequest) r;
        PlayerData playerData = updateRequest.playerData;

        if ( updateRequest.updateLocal ) {
            if ( playerData.color != PlayerData.TeamColor.NONE ) {
                localPlayerData.color = playerData.color;
            }
            if ( playerData.ID != -1 ) {
                currentGrid.removePlayer(localPlayerData.ID);
                localPlayerData.ID = playerData.ID;
                currentGrid.addPlayer(localPlayerData);
            }
        }
        if ( playerData.ID != localPlayerData.ID ) {
            if ( updateRequest.connected ) {
                currentGrid.addPlayer(playerData);
            }
            else {
                currentGrid.removePlayer(playerData.ID);
            }
        }
        try {
            panel.getGameStatusPanel().setPlayerPanels(getPlayerRankings());
        }
        catch (Exception e) {
            // IDK DUDE XDDDDDDDDDD
        }
    }

    private void handleDisconnectRequest(Request r, Integer ID) {
        String msg = ((DisconnectRequest) r).message;
        ApplicationFrame.writeToStatusLog("Disconnected from " + clientSocket.getRemoteSocketAddress()
                + ": " + msg);
        try {
            clientSocket.close();
        }
        catch (IOException ex) {
            Logger.getLogger(SimulationRemoteClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        setRunning(false);

    }

    private void handleGameStatusRequest(Request r, Integer ID) {
        GameStatusRequest req = ((GameStatusRequest) r);
        currentGrid.setRunning(req.running);
    }

    private void handleSetUnitRequest(Request r, Integer ID) {
        SetUnitRequest setUnit = (SetUnitRequest) r;
        if ( setUnit.unit != null ) {
            currentGrid.setUnit(setUnit.row, setUnit.col, setUnit.unit);
        }
        else {
            currentGrid.removeUnit(setUnit.row, setUnit.col);
        }
        panel.getGameStatusPanel().setPlayerPanels(getPlayerRankings());

    }

    @Override
    public void handleRequest(Request request, int ID)
            throws IOException, InvalidRequestException {
        try {
            Method method = SimulationRemoteClient.class.getDeclaredMethod(request.type.procedureName,
                    Request.class, Integer.class);
            method.invoke(this, request, ID);
        }
        catch (NoSuchMethodException e) {
        }
        catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void synchronize() {
    }

    public void initializeGridPanel(GridPanel panel) {
        this.panel = panel;
    }

    @Override
    public void close() {
        try {
            SpeciesLoader.loadJSONString(SpeciesLoader.getLocalSpeciesJSONString());
            panel.getPalette().resetPaletteItems();
            outputStream.writeObject(new UpdatePlayerDataRequest(localPlayerData, false));
            clientSocket.close();
        }
        catch (Exception e) {
            System.err.println("e");
        }
    }

    public void resize(int rows, int cols) {
    }
}

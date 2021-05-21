/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.simulation;

import com.giuseppelamalfa.gameofliferemastered.ApplicationFrame;
import com.giuseppelamalfa.gameofliferemastered.GridPanel;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.Grid;
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
    boolean isStarted = false;
    boolean isRunning = false;
    
    int playerCount;
    int rowCount = 1;
    int columnCount = 1;
    Grid currentGrid;
    Grid syncGrid = new Grid(1,1);

    GridPanel panel;
    Socket clientSocket;
    ObjectOutputStream outputStream;
    String host;
    int portNumber;
    PlayerData localPlayerData;
    
    public SimulationRemoteClient(String playerName, String host, int portNumber) throws IOException, Exception {
        localPlayerData = new PlayerData(playerName);
        init(host,portNumber);
    }
    
    public void init(String host, int portNumber) throws IOException, Exception{
        clientSocket = new Socket(host, portNumber);
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());

        outputStream.writeObject(new UpdatePlayerDataRequest(localPlayerData, true, true));
        ApplicationFrame.writeToStatusLog("Connected to " + host + ":" + portNumber);
        
        new Thread(() -> {
                try {
                    ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
                    while(true)
                        handleRequest(input.readObject(), 0);
                }
                catch (EOFException | SocketException e) { }
                catch (InvalidRequestException | IOException | ClassNotFoundException e){
                    e.printStackTrace();
                    isRunning = false;
                    isStarted = false;
                }
                try{clientSocket.close();} catch (IOException a) {}
        }).start();
        
        currentGrid = new Grid(1, 1);
        isStarted = true;
    }
    
    
    @Override public boolean isStarted() { return isStarted; }
    @Override public boolean isRunning() { return isRunning; }
    @Override public boolean isLocallyControlled() { return false; }
    @Override public int getLocalPlayerID() { 
        return localPlayerData.ID; 
    }
    @Override public PlayerData.TeamColor getPlayerColor(int ID){
        return currentGrid.getPlayerColor(ID);
    }
    @Override public ArrayList<PlayerData> getPlayerRankings() { return currentGrid.getPlayerRankings(); }

    @Override public int getRowCount() { return rowCount; }
    @Override public int getColumnCount() { return columnCount; }
    @Override public int getSectorSideLength() { return currentGrid.getSectorSideLength(); }
    @Override public int getCurrentTurn() { return currentGrid.getCurrentTurn(); }

    @Override public UnitInterface    getUnit(int row, int col) { return currentGrid.getUnit(row, col); }
    @Override
    public void             setUnit(int row, int col, UnitInterface unit) {
        try {
            outputStream.writeObject(new SetUnitRequest(row, col, unit));
        } catch (IOException ex) {
            Logger.getLogger(SimulationRemoteClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        currentGrid.setUnit(row, col, unit); 
        panel.getGameStatusPanel().setPlayerPanels(getPlayerRankings());
    }
    
    @Override
    public synchronized void computeNextTurn() throws Exception {
        currentGrid.computeNextTurn();
        panel.getGameStatusPanel().setPlayerPanels(getPlayerRankings());
    }
    
    @Override
    public void setRunning(boolean val) {
        if(!val){
            try {
                outputStream.writeObject(new PauseRequest(true));
            } catch (IOException ex) {
                Logger.getLogger(SimulationRemoteClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @Override
    public void handleRequest(Object requestObject, int ID)
            throws IOException, InvalidRequestException {
        Request request = (Request)requestObject;
        
        switch(request.getType()) {
            case LOG_MESSAGE:
                ApplicationFrame.writeToStatusLog(((LogMessageRequest)request).message);
                break;
            case SYNC_GRID:
                synchronized(syncGrid){
                    syncGrid = ((SyncGridRequest)request).grid;
                }
                rowCount = syncGrid.getRowCount();
                columnCount = syncGrid.getColumnCount();
                Grid tmpGrid = (Grid)syncGrid.clone();
                synchronized(currentGrid) {
                    currentGrid = tmpGrid;
                    currentGrid.setPlayerIDCheckNextTurn();
                    currentGrid.addPlayer(localPlayerData);
                    currentGrid.calculateScore();
                }
                panel.getGameStatusPanel().setPlayerPanels(getPlayerRankings());
                break;
            case UPDATE_PLAYER_DATA:
                UpdatePlayerDataRequest updateRequest = (UpdatePlayerDataRequest)request;
                PlayerData playerData = updateRequest.playerData;
                
                if(updateRequest.updateLocal)
                {
                    if(playerData.color != PlayerData.TeamColor.NONE)
                        localPlayerData.color = playerData.color;
                    if(playerData.ID != -1)
                    {
                        currentGrid.removePlayer(localPlayerData.ID);
                        localPlayerData.ID = playerData.ID;
                        currentGrid.addPlayer(localPlayerData);
                    }
                } 
                if(playerData.ID != localPlayerData.ID) {
                    if(updateRequest.connected) 
                        currentGrid.addPlayer(playerData);
                    else
                        currentGrid.removePlayer(playerData.ID);
                }
                try{
                    panel.getGameStatusPanel().setPlayerPanels(getPlayerRankings());
                }catch(Exception e){
                    // IDK DUDE XDDDDDDDDDD
                }
                break;
            case DISCONNECT:
                String msg = ((DisconnectRequest)request).message;
                ApplicationFrame.writeToStatusLog("Disconnected from " + clientSocket.getRemoteSocketAddress()
                    + ": " + msg);
                clientSocket.close();
                isRunning = false;
                isStarted = false;
                break;
            case PAUSE:
                isRunning = ((PauseRequest)request).running;
                break;
            case SET_UNIT:
                SetUnitRequest setUnit = (SetUnitRequest)request;
                currentGrid.setUnit(setUnit.row, setUnit.col, setUnit.unit);
                panel.getGameStatusPanel().setPlayerPanels(getPlayerRankings());
                break;
        }
    }
    @Override
    public void             synchronize() {
        if(isStarted)
        {
            SyncGridRequest req = new SyncGridRequest();
            try {
                outputStream.writeObject(req);
            } catch (IOException ex) {
                Logger.getLogger(SimulationRemoteClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    @Override
    public void             initializeGridPanel(GridPanel panel) {
        this.panel = panel;
    }
    @Override
    public void          close(){
        try {
            outputStream.writeObject(new UpdatePlayerDataRequest(localPlayerData, false));
            clientSocket.close();
        }catch(Exception e) {
            System.err.println("e");
        }
        
        isStarted = false;
    }
    
    public void resize(int rows, int cols){}
}

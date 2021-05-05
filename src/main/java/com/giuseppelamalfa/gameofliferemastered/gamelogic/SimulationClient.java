/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic;

import com.giuseppelamalfa.gameofliferemastered.ApplicationFrame;
import com.giuseppelamalfa.gameofliferemastered.BoardUpdateTask;
import com.giuseppelamalfa.gameofliferemastered.GridPanel;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.requests.*;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.UnitInterface;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author glitchedcode
 */
public class SimulationClient implements SimulationInterface {
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
    
    public SimulationClient(String host, int portNumber) throws IOException, Exception {
        init(host,portNumber);
    }
    
    public void init(String host, int portNumber) throws IOException, Exception{
        clientSocket = new Socket(host, portNumber);
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());

        ApplicationFrame.writeToStatusLog("Connected to " + host + ":" + portNumber);
        
        new Thread(() -> {
                try {
                    ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());

                    while(true){
                        handleRequest(input.readObject(), 0);
                    }
                }
                catch (EOFException | SocketException e){
                    
                }
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
    
    @Override
    public boolean          isSimulationStarted() { return isStarted; }
    @Override
    public boolean          isSimulationRunning() { return isRunning; }
    
    @Override
    public int              getRowCount() { return rowCount; }
    @Override
    public int              getColumnCount() { return columnCount; }
    @Override
    public int              getSectorSideLength() { return currentGrid.getSectorSideLength(); }
    @Override
    public int              getCurrentTurn() { return currentGrid.getCurrentTurn(); }
    
    @Override
    public UnitInterface    getUnit(int row, int col) { 
        return currentGrid.getUnit(row, col); 
    }
    @Override
    public void             setUnit(int row, int col, UnitInterface unit) { 
        currentGrid.setUnit(row, col, unit); 
    }
    
    @Override
    public synchronized void computeNextTurn() throws Exception {
        currentGrid.computeNextTurn(); 
    }
    
    public void setRunning(boolean val) {
        if(!val){
            try {
                outputStream.writeObject(new PauseRequest(true));
            } catch (IOException ex) {
                Logger.getLogger(SimulationClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @Override
    public void handleRequest(Object requestObject, int ID)
            throws IOException, InvalidRequestException {
        Request request = (Request)requestObject;
        
        switch(request.getType())
        {
            case SYNC:
                synchronized(syncGrid){
                    syncGrid = ((SyncRequest)request).grid;
                }
                rowCount = syncGrid.getRowCount();
                columnCount = syncGrid.getColumnCount();
                Grid tmpGrid = (Grid)syncGrid.clone();
                synchronized(currentGrid) {
                    currentGrid = tmpGrid;
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
        }
    }
    @Override
    public void             synchronize() {
        if(isStarted)
        {
            SyncRequest req = new SyncRequest();
            try {
                outputStream.writeObject(req);
            } catch (IOException ex) {
                Logger.getLogger(SimulationClient.class.getName()).log(Level.SEVERE, null, ex);
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
            clientSocket.close();
        }catch(Exception e) {
            System.err.println("e");
        }
        
        isStarted = false;
    }
}

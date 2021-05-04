/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic;

import com.giuseppelamalfa.gameofliferemastered.ApplicationFrame;
import com.giuseppelamalfa.gameofliferemastered.GridPanel;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.requests.*;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.UnitInterface;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author glitchedcode
 */
public class SimulationClient implements SimulationInterface {
    boolean isStarted = false;
    boolean isRunning = false;
    
    Integer playerCount;
    Integer rowCount = 1;
    Integer columnCount = 1;
    Grid currentGrid;
    Grid syncGrid;

    GridPanel panel;
    Socket clientSocket;
    ObjectOutputStream outputStream;
    String host;
    Integer portNumber;
    
    public SimulationClient(String host, Integer portNumber) throws IOException, Exception {
        init(host,portNumber);
    }
    
    public void init(String host, Integer portNumber) throws IOException, Exception{
        clientSocket = new Socket(host, portNumber);
        outputStream = new ObjectOutputStream(clientSocket.getOutputStream());

        ApplicationFrame.writeToStatusLog("Connected to " + host + ":" + portNumber);
        
        new Thread(() -> {
                try {
                    ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());

                    while(true){
                        handleRequest(input.readObject(), clientSocket);
                    }
                }
                catch (EOFException e){}
                catch (InvalidRequestException | IOException | ClassNotFoundException e){
                    e.printStackTrace();
                    isRunning = false;
                    isStarted = false;
                }
                try{clientSocket.close();} catch (IOException a) {}
        }).start();
        
        currentGrid = new Grid(1, 1);
    }
    
    @Override
    public boolean          isSimulationStarted() { return isStarted; }
    @Override
    public boolean          isSimulationRunning() { return isRunning; }
    
    @Override
    public Integer          getRowCount() { return rowCount; }
    @Override
    public Integer          getColumnCount() { return columnCount; }
    @Override
    public Integer          getSectorSideLength() { return currentGrid.getSectorSideLength(); }
    @Override
    public Integer          getCurrentTurn() { return currentGrid.getCurrentTurn(); }
    
    @Override
    public UnitInterface    getUnit(int row, int col) { 
        return currentGrid.getUnit(row, col); 
    }
    @Override
    public void             setUnit(int row, int col, UnitInterface unit) { 
        currentGrid.setUnit(row, col, unit); 
    }
    
    @Override
    public void             computeNextTurn() throws Exception { 
        //synchronized(currentGrid){
            currentGrid.computeNextTurn(); 
            System.out.println(getCurrentTurn());
        //}
    }
    
    @Override
    public synchronized void handleRequest(Object requestObject, Socket server)
            throws IOException, InvalidRequestException {
        Request request = (Request)requestObject;
        
        switch(request.getType())
        {
            case SYNC:
                syncGrid = ((SyncRequest)request).grid;
                rowCount = syncGrid.getRowCount();
                columnCount = syncGrid.getColumnCount();
                Grid tmpGrid = (Grid)syncGrid.clone();
                //synchronized(currentGrid) {
                    currentGrid = tmpGrid;
                    panel.setGrid(tmpGrid, false);
                //}
                System.out.println(getCurrentTurn());
                break;
            case DISCONNECT:
                String msg = ((DisconnectRequest)request).message;
                ApplicationFrame.writeToStatusLog("Disconnected from " + server.getRemoteSocketAddress()
                    + ": " + msg);
                server.close();
                isRunning = false;
                isStarted = false;
                break;
        }
    }
    @Override
    public void             synchronize() {}
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
    }
}

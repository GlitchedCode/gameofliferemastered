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
import java.io.IOException;
import java.io.InputStream;
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
    Integer rowCount;
    Integer columnCount;
    Grid currentGrid;
    Grid syncGrid;
    
    Socket clientSocket;
    String host;
    Integer portNumber;
    
    public SimulationClient(String host, Integer portNumber) throws IOException {
        init(host,portNumber);;
        
    }
    
    public void init(String host, Integer portNumber) throws IOException{
        clientSocket = new Socket(host, portNumber);
        ApplicationFrame.writeToStatusLog("Connected to " + host + ":" + portNumber);
        
        new Thread(() -> {
            try {
                ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
                
                Object obj;
                while((obj = input.readObject()) != null){
                    handleRequest(obj, clientSocket);
                }
            }catch (Exception e){
                System.err.println(e);
                isRunning = false;
                isStarted = false;
            }
            try{clientSocket.close();} catch (IOException a) {}
        }).start();
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
        synchronized(currentGrid){
            currentGrid.computeNextTurn(); 
        }
    }
    
    @Override
    public void             handleRequest(Object requestObject, Socket server)
            throws IOException, InvalidRequestException {
        Request request = (Request)requestObject;
        ObjectOutputStream output = new ObjectOutputStream(server.getOutputStream());

        switch(request.getType())
        {
            case SYNC:
                syncGrid = ((SyncRequest)request).grid;
                Grid tmpGrid = (Grid)syncGrid.clone();
                synchronized(currentGrid) {
                    currentGrid = tmpGrid;
                }
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
    public void             initializeGridPanel(GridPanel panel) {}
    
    protected void finalize(){
        try {
            clientSocket.close();
        }catch(Exception e) {
            System.err.println("e");
        }
    }
}

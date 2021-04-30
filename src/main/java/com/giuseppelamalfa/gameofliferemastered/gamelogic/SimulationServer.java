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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author glitchedcode
 */
public class SimulationServer implements SimulationInterface{
    public static final Integer defaultPlayerCount = 4;
    public static final Integer maxPlayers = 8;

    boolean         isStarted = false;
    boolean         isRunning = false;
    
    Integer         playerCount;
    Integer         rowCount;
    Integer         columnCount;
    Grid            currentGrid;
    Grid            syncGrid;
    
    Object          gridLock;
    Thread          acceptConnectionThread;
    Thread          readClientInputThread;
    
    List<Socket>    connectedClients;
    ServerSocket    serverSocket;
    String          serverIP;
    Integer         portNumber;
    
    public SimulationServer(Integer portNumber, Integer playerCount) throws IOException {
        init(portNumber, playerCount);        
    }
    
    public void init(Integer portNumber, Integer playerCount) throws IOException {
        if(playerCount < 2 | playerCount > maxPlayers)
        {
            this.playerCount = defaultPlayerCount;
        }
        else
            this.playerCount = playerCount;
        
        serverSocket = new ServerSocket(portNumber);
        
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                whatismyip.openStream()));

        serverIP = in.readLine(); //you get the IP as a String
        this.portNumber = portNumber;
        ApplicationFrame.writeToStatusLog("Server open at " + serverIP + ":" + portNumber);
        ApplicationFrame.writeToStatusLog("Max players: " + this.playerCount);
        
        acceptConnectionThread = new Thread(() -> {
            try{
                while(true){
                    Socket conn = serverSocket.accept();
                    if(connectedClients.size() + 1 < playerCount)
                    {    
                        connectedClients.add(conn);
                        ApplicationFrame.writeToStatusLog(conn.getRemoteSocketAddress() + " joined the game.");
                        // Make a thread for each client connection. 
                        new Thread(() -> {
                            try{
                                InputStream input = conn.getInputStream();
                                ObjectInputStream objectInputStream = new ObjectInputStream(input);
                                
                                Object obj;
                                while((obj = objectInputStream.readObject()) != null)
                                {
                                    handleRequest(obj, conn);
                                }
                            }
                            catch (InvalidRequestException | IOException | ClassNotFoundException e){
                                System.err.println(e);
                                connectedClients.remove(conn);
                            }
                            try{
                                conn.close();
                            }catch(IOException e) {}
                        }).start();
                    }
                    else {
                        ObjectOutputStream stream = new ObjectOutputStream(conn.getOutputStream());
                        DisconnectRequest req = new DisconnectRequest("No player slots available. Closing connection.");
                        stream.writeObject(req);
                        conn.close();
                    }
                }
            }catch(IOException e){
                System.out.println(e);
            }
        });
        acceptConnectionThread.start();
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
    public UnitInterface    getUnit(int row, int col) { return currentGrid.getUnit(row, col); }
    @Override
    public void             setUnit(int row, int col, UnitInterface unit) { currentGrid.setUnit(row, col, unit); }
    
    @Override
    public void             computeNextTurn() throws Exception { currentGrid.computeNextTurn(); }
    
    @Override
    public void             synchronize() {
        synchronized(gridLock)
        {
            syncGrid = (Grid)currentGrid.clone();
            try{
                SyncRequest syncRequest = new SyncRequest(syncGrid);
                for(int i = 0; i < connectedClients.size(); i++)
                {
                    ObjectOutputStream output = new ObjectOutputStream
                        (connectedClients.get(i).getOutputStream());
                    output.writeObject(syncRequest);
                }
            }catch(Exception e) {
                System.out.println(e);
            }
        }
    }
    
    public void             synchronize(ObjectOutputStream output) {
        synchronized(gridLock){
            syncGrid = (Grid)currentGrid.clone();
            SyncRequest syncRequest = new SyncRequest(syncGrid);
            try{
                output.writeObject(syncRequest);
            }catch(Exception e){
                System.out.println(e);
            }
        }
    }
    
    @Override
    public void             initializeGridPanel(GridPanel panel) {}
    
    @Override
    public void             handleRequest(Object requestObject, Socket client) throws IOException, InvalidRequestException {
        Request tmp = (Request)requestObject;
        ObjectOutputStream output = new ObjectOutputStream(client.getOutputStream());
        switch(tmp.getType())   
        {
            case SYNC:
                synchronize(output);
                break;
            case DISCONNECT:
                DisconnectRequest disconnect = (DisconnectRequest)tmp;
                ApplicationFrame.writeToStatusLog(client.getRemoteSocketAddress() + " disconnected: " + disconnect.message);
                connectedClients.remove(client);
                client.close();
                break;
        }
    }
    
    protected void finalize()
    {
        try {
            for(int i = 0; i < connectedClients.size(); i++)
                connectedClients.get(i).close();
            serverSocket.close();  
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }
}
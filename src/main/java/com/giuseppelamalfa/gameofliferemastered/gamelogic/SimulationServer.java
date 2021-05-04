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
import com.sun.tools.javac.util.Pair;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.tools.java.ClassDefinition;

/**
 *
 * @author glitchedcode
 */
public class SimulationServer implements SimulationInterface{
    public static final Integer defaultPlayerCount = 4;
    public static final Integer maxPlayers = 8;
    
    public static Integer syncTurnCount = 20;
    public static Integer simulationPhaseLength = 60;

    boolean         isStarted = false;
    boolean         isRunning = false;
    
    Integer         playerCount;
    Integer         rowCount;
    Integer         columnCount;
    Grid            currentGrid;
    Grid            syncGrid;
    
    Object          gridLock = new Object();
    Thread          acceptConnectionThread;
    Thread          readClientInputThread;
    
    ArrayList<Pair<Socket, ObjectOutputStream>>    connectedClients = new ArrayList<Pair<Socket, ObjectOutputStream>>();
    ServerSocket    serverSocket;
    String          serverIP;
    Integer         portNumber;
    
    public SimulationServer(Integer portNumber, Integer playerCount, 
            Integer rowCount, Integer columnCount) throws IOException 
    {
        init(portNumber, playerCount, rowCount, columnCount);        
    }
    
    public void init(Integer portNumber, Integer playerCount, Integer rowCount, Integer columnCount) throws IOException {
        if(playerCount < 2 | playerCount > maxPlayers)
            this.playerCount = defaultPlayerCount;
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
        
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        try {
            currentGrid = new Grid(rowCount, columnCount);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        acceptConnectionThread = new Thread(() -> {
            try{
                while(true){
                    Socket conn = serverSocket.accept();    
                    ObjectOutputStream stream = new ObjectOutputStream(conn.getOutputStream());

                    if(connectedClients.size() + 1 < playerCount)
                    {    
                        connectedClients.add(new Pair<Socket, ObjectOutputStream>(conn, stream));
                        ApplicationFrame.writeToStatusLog(conn.getRemoteSocketAddress() + " joined the game.");
                        // Make a thread for each client connection. 
                        new Thread(() -> {
                            try{
                                InputStream input = conn.getInputStream();
                                ObjectInputStream objectInputStream = new ObjectInputStream(input);
                                
                                while(true)
                                {
                                    handleRequest(objectInputStream.readObject(), conn);
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
                        
                        stream.writeObject(new SyncRequest(currentGrid));
                    }
                    else {
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
    public synchronized void computeNextTurn() throws Exception { 
        currentGrid.computeNextTurn();
        if(currentGrid.getTurn() % syncTurnCount == 0)
            synchronize();
    }
    
    @Override
    public synchronized void synchronize() {
            SyncRequest syncRequest = new SyncRequest((Grid)currentGrid.clone());
            System.out.println(getCurrentTurn());
            try{
                for(int i = 0; i < connectedClients.size(); i++)
                    connectedClients.get(i).snd.writeObject(syncRequest);
            }catch(Exception e) {
                System.out.println(e);
            }

    }
    
    public synchronized void synchronize(ObjectOutputStream output) {
        syncGrid = (Grid)currentGrid.clone();
        SyncRequest syncRequest = new SyncRequest(syncGrid);
        try{
            output.writeObject(syncRequest);
        }catch(Exception e){
            System.out.println(e);
        }
        
    }

    @Override
    public void             initializeGridPanel(GridPanel panel) {}
    @Override
    public synchronized void handleRequest(Object requestObject, Socket client) throws IOException, InvalidRequestException {
        Request tmp = (Request)requestObject;
        switch(tmp.getType())   
        {
            case SYNC:
                for(int i = 0; i < connectedClients.size(); i++)
                    if(connectedClients.get(i).fst == client){
                        synchronize(connectedClients.get(i).snd);
                        break;
                    }
                break;
            case DISCONNECT:
                DisconnectRequest disconnect = (DisconnectRequest)tmp;
                ApplicationFrame.writeToStatusLog(client.getRemoteSocketAddress() + " disconnected: " + disconnect.message);
                for(int i = 0; i < connectedClients.size(); i++)
                    if(connectedClients.get(i).fst == client){
                        connectedClients.remove(i);
                        break;
                    }
                client.close();
                break;
        }
    }
    
    @Override
    public void close()
    {
        try {
            for(int i = 0; i < connectedClients.size(); i++)
                connectedClients.get(i).fst.close();
            serverSocket.close();  
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }
}
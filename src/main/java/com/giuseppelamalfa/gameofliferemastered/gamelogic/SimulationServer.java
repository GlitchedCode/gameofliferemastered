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
import com.giuseppelamalfa.gameofliferemastered.utils.PlayerData;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

class ClientData{
    public Socket socket;
    public ObjectOutputStream stream;
    public int ID;
    public PlayerData playerData = new PlayerData();
    
    public ClientData(Socket socket, ObjectOutputStream stream, int ID)
    {
        this.socket = socket;
        this.stream = stream;
        this.ID = ID;
        this.playerData.playerName = "Player" + ID;
    }
    
    public ClientData(Socket socket, ObjectOutputStream stream, int ID, String playerName)
    {
        this.socket = socket;
        this.stream = stream;
        this.ID = ID;
        this.playerData.playerName = playerName;
    }
}

/**
 *
 * @author glitchedcode
 */
public class SimulationServer implements SimulationInterface{
    public static final Integer DEFAULT_PLAYER_COUNT = 4;
    public static final Integer MAX_PLAYER_COUNT = 8;
    
    public static Integer syncTurnCount = 40;
    public static Integer simulationPhaseLength = 80;

    boolean         isStarted = false;
    boolean         isRunning = false;
    
    int             nextClientID = 0;
    int             playerCount;
    int             rowCount;
    int             columnCount;
    Grid            currentGrid;
    Grid            syncGrid = new Grid(1,1);
    
    Object          gridLock = new Object();
    Thread          acceptConnectionThread;
    Thread          readClientInputThread;
    
    HashMap<Integer, ClientData>    connectedClients = new HashMap<>();
    ServerSocket    serverSocket;
    String          serverIP;
    int         portNumber;    
  
    public SimulationServer(Integer portNumber, Integer playerCount, 
            Integer rowCount, Integer columnCount) throws Exception {
        
        init(portNumber, playerCount, rowCount, columnCount);        
    }
    
    public final void init(Integer portNumber, Integer playerCount, Integer rowCount, Integer columnCount) throws IOException {
        if(playerCount < 2 | playerCount > MAX_PLAYER_COUNT)
            this.playerCount = DEFAULT_PLAYER_COUNT;
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
                    int clientID = nextClientID;
                    Socket conn = serverSocket.accept();    
                    ObjectOutputStream stream = new ObjectOutputStream(conn.getOutputStream());
                    nextClientID++;

                    if(connectedClients.size() + 1 < playerCount)
                    {    
                        ClientData newClient = new ClientData(conn, stream, clientID);
                        connectedClients.put(clientID, newClient);
                        
                        ApplicationFrame.writeToStatusLog("Accepting connection from" + conn.getRemoteSocketAddress());
                        // Make a thread for each client connection. 
                        new Thread(() -> {
                            ClientData clientData = newClient;
                            try{
                                InputStream input = conn.getInputStream();
                                ObjectInputStream objectInputStream = new ObjectInputStream(input);
                                
                                while(true)
                                    handleRequest(objectInputStream.readObject(), clientData.ID);
                            }
                            catch(EOFException e) {}
                            catch (InvalidRequestException | IOException | ClassNotFoundException e){
                                System.err.println(e);
                            }
                            connectedClients.remove(clientData.ID);
                            try{
                                conn.close();
                            }catch(IOException e) {}
                            
                            ApplicationFrame.writeToStatusLog(clientData.playerData.playerName + " left the game.");
                        }).start();
                        
                        stream.writeObject(new PauseRequest(isRunning));
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
    public UnitInterface    getUnit(int row, int col) { return currentGrid.getUnit(row, col); }
    @Override
    public void             setUnit(int row, int col, UnitInterface unit) {
        SetUnitRequest req = new SetUnitRequest(row, col, unit);
        try {
            handleRequest(req, -1);
        } catch (IOException ex) {
            Logger.getLogger(SimulationServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidRequestException ex) {
            Logger.getLogger(SimulationServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void computeNextTurn() throws Exception { 
        synchronized(currentGrid){
            currentGrid.computeNextTurn();
            if(currentGrid.getCurrentTurn() % syncTurnCount == 0)
                synchronize();
        }
    }
    
    @Override
    public synchronized void synchronize() {
        if(syncGrid.equals(currentGrid)) return;
        
        syncGrid = (Grid)currentGrid.clone();
        SyncRequest syncRequest = new SyncRequest(syncGrid);
        try{
            for(Integer key : connectedClients.keySet())
                connectedClients.get(key).stream.writeObject(syncRequest);
        }catch(Exception e) {
            System.out.println(e);
        }

    }
    
    public synchronized void synchronize(ObjectOutputStream output) {
        syncGrid = (Grid)currentGrid.clone();
        try{
            output.writeObject(new SyncRequest(syncGrid));
        }catch(Exception e){
            System.out.println(e);
        }
        
    }
    @Override
    public void             initializeGridPanel(GridPanel panel) {}
    
    @Override
    public void             setRunning(boolean val) {
        isRunning = val;
        PauseRequest req = new PauseRequest(val);
        try{
            for (Integer key : connectedClients.keySet())
                connectedClients.get(key).stream.writeObject(req);
        }catch(IOException e){
            e.printStackTrace();
        }
        
        synchronize();
    }
    
    @Override
    public synchronized void handleRequest(Object requestObject, int clientID) throws IOException, InvalidRequestException {
        Request tmp = (Request)requestObject;
        ClientData data;
        if (clientID >= 0)
            data = connectedClients.get(clientID);
        else data = null;
        switch(tmp.getType())   
        {
            case SYNC:
                if (data != null) synchronize(data.stream);
                break;
            case UPDATE_PLAYER_DATA:
                if(data == null) break;
                PlayerData playerData = ((UpdatePlayerDataRequest)tmp).playerData;
                if(playerData.playerName != null)
                    data.playerData.playerName = playerData.playerName;
                break;
            case DISCONNECT:
                if (data == null) break;
                DisconnectRequest disconnect = (DisconnectRequest)tmp;
                ApplicationFrame.writeToStatusLog(data.socket.getRemoteSocketAddress() + " disconnected: " + disconnect.message);
                connectedClients.remove(clientID);
                data.socket.close();
                break;
            case SET_UNIT:
                SetUnitRequest setUnit = (SetUnitRequest)tmp;
                for (Integer key : connectedClients.keySet())
                    if(key != clientID | clientID < 0)
                        connectedClients.get(key).stream.writeObject(requestObject);
                currentGrid.setUnit(setUnit.row, setUnit.col, setUnit.unit);
                break;
        }
    }
    
    @Override
    public void close()
    {
        try {
            for(int i = 0; i < connectedClients.size(); i++)
                connectedClients.get(i).socket.close();
            serverSocket.close();  
        }
        catch(Exception e) {
            System.out.println(e);
        }
        
        isStarted = false;
    }
}
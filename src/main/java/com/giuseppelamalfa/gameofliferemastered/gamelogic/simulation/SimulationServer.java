/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.simulation;

import com.giuseppelamalfa.gameofliferemastered.ApplicationFrame;
import com.giuseppelamalfa.gameofliferemastered.GridPanel;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.grid.Grid;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.PlayerData;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.grid.GameMode;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.requests.*;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.UnitInterface;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

class ClientData {

    public final Socket socket;
    public final ObjectOutputStream stream;
    public final PlayerData playerData = new PlayerData();

    public ClientData(Socket socket, ObjectOutputStream stream, int ID) {
        this.socket = socket;
        this.stream = stream;
        this.playerData.ID = ID;
    }

    public ClientData(Socket socket, ObjectOutputStream stream, int ID, String playerName) {
        this.socket = socket;
        this.stream = stream;
        this.playerData.ID = ID;
        this.playerData.playerName = playerName;
    }
}

/**
 *
 * @author glitchedcode
 */
public class SimulationServer implements SimulationInterface {

    public static final Integer DEFAULT_PLAYER_COUNT = 4;
    public static final Integer MAX_PLAYER_COUNT = 8;

    public final static Integer GRYD_SYNC_TURN_COUNT = 40;

    final GameMode mode;
    int nextClientID = 1;
    int playerCount;
    int rowCount;
    int columnCount;
    Grid currentGrid;
    Grid syncGrid = new Grid(1, 1);

    Object gridLock = new Object();
    Thread acceptConnectionThread;

    boolean remoteInstance = false;
    static final ArrayList<PlayerData> offlineRanking = new ArrayList<>();
    HashMap<Integer, ClientData> connectedClients = new HashMap<>();
    ServerSocket serverSocket;
    String serverIP;
    int portNumber;

    ArrayList<PlayerData.TeamColor> availableColors = new ArrayList<>();
    PlayerData localPlayerData;
    GridPanel panel;

    public SimulationServer(String playerName, int portNumber, int playerCount,
            int rowCount, int columnCount, GameMode mode) throws Exception {
        this.mode = mode;
        initializeRemoteServer(portNumber, playerCount, rowCount, columnCount);
        localPlayerData = new PlayerData(playerName, extractRandomColor());
        localPlayerData.ID = 0;
        remoteInstance = true;
        currentGrid.addPlayer(localPlayerData);
    }

    public SimulationServer(int rowCount, int columnCount) throws Exception {
        mode = GameMode.SANDBOX;
        currentGrid = new Grid(rowCount, columnCount);
        localPlayerData = new PlayerData();
        localPlayerData.ID = 0;
        currentGrid.addPlayer(localPlayerData);
    }

    private void initializeRemoteServer(int portNumber, int playerCount,
            int rowCount, int columnCount) throws IOException, Exception {
        if (playerCount < 2 | playerCount > MAX_PLAYER_COUNT) {
            this.playerCount = DEFAULT_PLAYER_COUNT;
        } else {
            this.playerCount = playerCount;
        }

        for (PlayerData.TeamColor color : PlayerData.TeamColor.values()) {
            if (color != PlayerData.TeamColor.NONE) {
                availableColors.add(color);
            }
        }

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
        currentGrid = mode.getNewGrid(rowCount, columnCount);

        // Spawn a thread to accept client connections.
        acceptConnectionThread = new Thread(() -> {
            try {
                while (true) {
                    int clientID = nextClientID;
                    Socket conn = serverSocket.accept();
                    ObjectOutputStream outputStream = new ObjectOutputStream(conn.getOutputStream());
                    nextClientID++;

                    if (connectedClients.size() + 1 < playerCount) {
                        ClientData client = new ClientData(conn, outputStream, clientID);
                        client.playerData.color = extractRandomColor();
                        client.playerData.ID = clientID;
                        connectedClients.put(clientID, client);

                        ApplicationFrame.writeToStatusLog("Accepting connection from" + conn.getRemoteSocketAddress());
                        // Make a thread for each client connection to handle their requests separately.
                        new Thread(() -> {
                            ClientData clientData = client;
                            try {
                                InputStream input = conn.getInputStream();
                                ObjectInputStream inputStream = new ObjectInputStream(input);

                                PlayerData tmp = new PlayerData(clientData.playerData);
                                currentGrid.addPlayer(client.playerData);
                                outputStream.writeObject(new UpdatePlayerDataRequest(tmp, true, true));
                                outputStream.writeObject(new SyncGridRequest(currentGrid));
                                outputStream.writeObject(new GameStatusRequest(isRunning()));

                                for (ClientData data : connectedClients.values()) {
                                    if (data.playerData.ID != clientID) {
                                        UpdatePlayerDataRequest req = new UpdatePlayerDataRequest(data.playerData, true);
                                        sendToAll(req);
                                    }
                                }

                                while (true) {
                                    handleRequest(inputStream.readObject(), clientData.playerData.ID);
                                }
                            } // These exceptions are caught when the client disconnects
                            catch (EOFException e) {
                            } catch (InvalidRequestException | IOException | ClassNotFoundException e) {
                                ApplicationFrame.writeToStatusLog(e.toString());
                            }

                            sendToAll(new UpdatePlayerDataRequest(clientData.playerData, false));

                            currentGrid.removePlayer(clientData.playerData.ID);
                            connectedClients.remove(clientData.playerData.ID);
                            panel.getGameStatusPanel().setPlayerPanels(getPlayerRankings());
                            try {
                                conn.close();
                            } catch (IOException e) {
                            }

                            sendLogMessage(clientData.playerData.playerName + " left the game.");
                            if (clientData.playerData.color != PlayerData.TeamColor.NONE) {
                                availableColors.add(clientData.playerData.color);
                            }
                        }).start();
                    } else {
                        DisconnectRequest req = new DisconnectRequest("No player slots available. Closing connection.");
                        outputStream.writeObject(req);
                        conn.close();
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        });
        acceptConnectionThread.start();
    }

    public final PlayerData.TeamColor extractRandomColor() {
        if (availableColors.isEmpty()) {
            return PlayerData.TeamColor.NONE;
        }
        int index = (int) (Math.random() * availableColors.size());
        PlayerData.TeamColor ret = availableColors.get(index);
        availableColors.remove(index);
        return ret;
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
        return mode.locallyControlled;
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
        if (remoteInstance) {
            return currentGrid.getPlayerRankings();
        } else {
            return offlineRanking;
        }
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
        currentGrid.removeUnit(row, col);
    }

    @Override
    public void setUnit(int row, int col, UnitInterface unit) {
        SetUnitRequest req = new SetUnitRequest(row, col, unit);
        try {
            handleRequest(req, -1);
        } catch (IOException ex) {
            Logger.getLogger(SimulationServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidRequestException ex) {
            Logger.getLogger(SimulationServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        panel.getGameStatusPanel().setPlayerPanels(getPlayerRankings());
    }

    @Override
    public void computeNextTurn() throws Exception {
        synchronized (currentGrid) {
            currentGrid.computeNextTurn();

            // Syncronize grid and player data at regular intervals.
            if (currentGrid.getCurrentTurn() % GRYD_SYNC_TURN_COUNT == 0 | !isRunning()) {
                synchronize();
            }
        }
        panel.getGameStatusPanel().setPlayerPanels(getPlayerRankings());
    }

    @Override
    public synchronized void synchronize() {
        if (syncGrid.equals(currentGrid)) {
            return;
        }

        try {
            syncGrid = (Grid) currentGrid.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(SimulationServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        SyncGridRequest syncRequest = new SyncGridRequest(syncGrid);
        try {
            for (Integer key : connectedClients.keySet()) {
                connectedClients.get(key).stream.writeObject(syncRequest);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void synchronize(ObjectOutputStream output) {
        try {
            syncGrid = (Grid) currentGrid.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(SimulationServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            output.writeObject(new SyncGridRequest(syncGrid));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendToAll(Request req) {
        for (ClientData client : connectedClients.values())
            try {
            client.stream.writeObject(req);
        } catch (IOException ex) {
            Logger.getLogger(SimulationServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendToAll(Request req, int excludeID) {
        for (ClientData client : connectedClients.values()) {
            if (client.playerData.ID != excludeID)
                try {
                client.stream.writeObject(req);
            } catch (IOException ex) {
                Logger.getLogger(SimulationServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void initializeGridPanel(GridPanel panel) {
        this.panel = panel;
    }

    @Override
    public void setRunning(boolean val) {
        currentGrid.setRunning(val);
        GameStatusRequest req = new GameStatusRequest(isRunning());
        try {
            for (Integer key : connectedClients.keySet()) {
                connectedClients.get(key).stream.writeObject(req);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        synchronize();
    }

    @Override
    public synchronized void handleRequest(Object requestObject, int clientID) throws IOException, InvalidRequestException {
        Request tmp = (Request) requestObject;
        ClientData clientData;
        clientData = connectedClients.get(clientID);
        switch (tmp.getType()) {
            case SYNC_GRID:
                if (clientData != null) {
                    synchronize(clientData.stream);
                }
                break;
            case UPDATE_PLAYER_DATA:
                if (clientData == null) {
                    break;
                }
                UpdatePlayerDataRequest updateRequest = (UpdatePlayerDataRequest) tmp;
                PlayerData playerData = updateRequest.playerData;

                if (!updateRequest.connected) {
                    currentGrid.removePlayer(playerData.ID);
                    UpdatePlayerDataRequest disconnectRequest = new UpdatePlayerDataRequest(playerData, false);
                    sendToAll(disconnectRequest, playerData.ID);
                } else if (updateRequest.updateLocal) {
                    if (playerData.playerName != null) {
                        if (clientData.playerData.playerName == null) {
                            sendLogMessage(playerData.playerName + " joined the game.");
                        }
                        clientData.playerData.playerName = playerData.playerName;
                    }
                    if (playerData.color != PlayerData.TeamColor.NONE
                            & availableColors.contains(playerData.color)) {
                        if (clientData.playerData.color != PlayerData.TeamColor.NONE) {
                            availableColors.add(clientData.playerData.color);
                        }
                        clientData.playerData.color = playerData.color;
                        availableColors.remove(playerData.color);
                    }

                    currentGrid.addPlayer(clientData.playerData);
                    UpdatePlayerDataRequest clientUpdateRequest
                            = new UpdatePlayerDataRequest(clientData.playerData, true);
                    sendToAll(clientUpdateRequest, playerData.ID);
                }
                panel.getGameStatusPanel().setPlayerPanels(getPlayerRankings());
                break;
            case DISCONNECT:
                if (clientData == null) {
                    break;
                }
                DisconnectRequest disconnect = (DisconnectRequest) tmp;
                ApplicationFrame.writeToStatusLog(clientData.socket.getRemoteSocketAddress() + " disconnected: " + disconnect.message);
                connectedClients.remove(clientID);
                clientData.socket.close();
                break;
            case SET_UNIT:
                SetUnitRequest setUnit = (SetUnitRequest) tmp;
                for (Integer key : connectedClients.keySet()) {
                    if (key != clientID | clientID < 0) {
                        connectedClients.get(key).stream.writeObject(requestObject);
                    }
                }
                currentGrid.setUnit(setUnit.row, setUnit.col, setUnit.unit);
                panel.getGameStatusPanel().setPlayerPanels(getPlayerRankings());
                break;
        }
    }

    @Override
    public void close() {
        try {
            for (int i = 0; i < connectedClients.size(); i++) {
                connectedClients.get(i).socket.close();
            }
            serverSocket.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void resize(int rows, int cols) {
        try {
            currentGrid.resize(rows, cols);
            synchronize();
        } catch (Exception ex) {
            ApplicationFrame.writeToStatusLog(ex.toString());
        }
    }

    public void sendLogMessage(String msg) {
        ApplicationFrame.writeToStatusLog(msg);
        LogMessageRequest req = new LogMessageRequest(msg);
        try {
            for (Integer key : connectedClients.keySet()) {
                connectedClients.get(key).stream.writeObject(req);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.simulation;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.PlayerData;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.grid.GameMode;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.grid.Grid;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.requests.DisconnectRequest;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.requests.GameStatusRequest;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.requests.InvalidRequestException;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.requests.LogMessageRequest;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.requests.Request;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.requests.SetUnitRequest;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.requests.SyncGridRequest;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.requests.UpdatePlayerDataRequest;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.UnitInterface;
import com.giuseppelamalfa.gameofliferemastered.ui.GridPanel;
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

/**
 *
 * @author glitchedcode
 */
public class SimulationCLIServer implements SimulationInterface {

    public static void writeToStatusLog(String msg) {
        System.out.println(msg + "\n");
    }
    
    public static void printUsage() {
        System.out.println(
                "usage: java -jar liferemastered-headless.jar <args>\n"
                + "-m <sandbox|competitive>: sets the game mode (default: sandbox)\n"
                + "-r <rowCount>: grid row count (default: 50true)\n"
                + "-c <columnCount>: grid column count (default: 70)\n"
                + "-p <portNumber>: set port number for server socket (default: 7777)\n"
                + "-P <maxPlayers>: set max player count (default: 4, max: 8)\n"
        );
        System.exit(-1);
    }

    public static GameMode getSimulationMode(String modeString) {
        GameMode ret = null;

        switch (modeString) {
            case "sandbox":
                return GameMode.SANDBOX;
            case "competitive":
                return GameMode.COMPETITIVE;
        }

        return ret;
    }

    public static void main(String args[]) {
        SimulationCLIServer server = null;

        // defaults
        GameMode mode = GameMode.SANDBOX;
        int rows = 50;
        int cols = 70;
        int port = 7777;
        int playerCount = 4;

        // Read command line arguments
        try {
            for (int i = 0; i < args.length; i++) {
                String currentArg = args[i];
                switch (currentArg) {
                    case "-m":
                        mode = getSimulationMode(args[++i]);
                        break;
                    case "-r":
                        rows = Integer.decode(args[++i]);
                        break;
                    case "-c":
                        cols = Integer.decode(args[++i]);
                        break;
                    case "-p":
                        port = Integer.decode(args[++i]);
                        break;
                    case "-P":
                        playerCount = Integer.decode(args[++i]);
                        ;
                        break;
                }
            }

            server = new SimulationCLIServer(port, playerCount, rows, cols, mode);

        } catch (Exception e) {
            printUsage();
        }
        if (server == null) {
            printUsage();
        }

    }

    public static final Integer DEFAULT_PLAYER_COUNT = 4;
    public static final Integer MAX_PLAYER_COUNT = 8;

    public final static Integer GRYD_SYNC_TURN_COUNT = 40;

    final GameMode mode;
    int nextClientID = 0;
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
    GridPanel panel;

    public SimulationCLIServer(int portNumber, int playerCount,
            int rowCount, int columnCount, GameMode mode) throws Exception {
        this.mode = mode;
        initializeRemoteServer(portNumber, playerCount, rowCount, columnCount);
        remoteInstance = true;
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
        writeToStatusLog("Server open at " + serverIP + ":" + portNumber);
        writeToStatusLog("Max players: " + this.playerCount);

        this.rowCount = rowCount;
        this.columnCount = columnCount;
        currentGrid = syncGrid;
        currentGrid = mode.getNewGrid(rowCount, columnCount);
        currentGrid.setSimulation(this);

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

                        writeToStatusLog("Accepting connection from" + conn.getRemoteSocketAddress());
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
                                outputStream.writeObject(new GameStatusRequest(isRunning(), getStatusString()));

                                for (ClientData data : connectedClients.values()) {
                                    if (data.playerData.ID != clientID) {
                                        UpdatePlayerDataRequest req = new UpdatePlayerDataRequest(data.playerData, true);
                                        sendToAll(req);
                                    }
                                }

                                while (true) {
                                    handleRequest((Request) inputStream.readObject(), clientData.playerData.ID);
                                }
                            } // These exceptions are caught when the client disconnects
                            catch (EOFException e) {
                            } catch (InvalidRequestException | IOException | ClassNotFoundException e) {
                                writeToStatusLog(e.toString());
                            }

                            sendToAll(new UpdatePlayerDataRequest(clientData.playerData, false));

                            currentGrid.removePlayer(clientData.playerData.ID);
                            connectedClients.remove(clientData.playerData.ID);
                            panel.getGameStatusPanel().setPlayerPanels(getPlayerRankings());
                            try {
                                conn.close();
                            } catch (IOException e) {
                            }

                            sendLogMessage(clientData.playerData.name + " left the game.");
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
        return false;
    }

    @Override
    public String getGameModeName() {
        return currentGrid.getGameModeName();
    }

    @Override
    public int getLocalPlayerID() {
        return -1;
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
    }

    @Override
    public void setUnit(int row, int col, UnitInterface unit) {
    }

    @Override
    public void computeNextTurn() throws Exception {
        currentGrid.computeNextTurn();
        // Syncronize grid and player data if the simulation is stepped forward manually
        if (!isRunning()) {
            sendToAll(new SyncGridRequest(null, true));
            panel.getGameStatusPanel().setShowWinner(currentGrid.showWinner());
        }
    }

    @Override
    public void synchronize() {
        if (syncGrid.equals(currentGrid)) {
            return;
        }

        try {
            syncGrid = (Grid) currentGrid.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(SimulationServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        panel.getGameStatusPanel().setShowWinner(currentGrid.showWinner());
        sendToAll(new SyncGridRequest(syncGrid));
    }

    public void synchronize(ObjectOutputStream output) {
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
        sendToAll(req, -1);
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
        //sendToAll(new GameStatusRequest(isRunning()));
        synchronize();
    }

    @Override
    public synchronized void handleRequest(Request request, int clientID) throws IOException, InvalidRequestException {
        ClientData clientData;
        PlayerData playerData;
        clientData = connectedClients.get(clientID);
        if (clientID == -1) {
            playerData = null;
        } else {
            playerData = clientData.playerData;
        }

        switch (request.getType()) {
            case SYNC_GRID:
                if (clientData != null) {
                    synchronize(clientData.stream);
                }
                break;

            case UPDATE_PLAYER_DATA:
                if (clientData == null) {
                    break;
                }
                UpdatePlayerDataRequest updateRequest = (UpdatePlayerDataRequest) request;
                PlayerData newPlayerData = updateRequest.playerData;

                if (!updateRequest.connected) {
                    currentGrid.removePlayer(newPlayerData.ID);
                    UpdatePlayerDataRequest disconnectRequest = new UpdatePlayerDataRequest(newPlayerData, false);
                    sendToAll(disconnectRequest, newPlayerData.ID);
                } else if (updateRequest.updateLocal) {
                    if (newPlayerData.name != null) {
                        if (clientData.playerData.name == null) {
                            sendLogMessage(newPlayerData.name + " joined the game.");
                        }
                        clientData.playerData.name = newPlayerData.name;
                    }
                    if (newPlayerData.color != PlayerData.TeamColor.NONE
                            & availableColors.contains(newPlayerData.color)) {
                        if (clientData.playerData.color != PlayerData.TeamColor.NONE) {
                            availableColors.add(clientData.playerData.color);
                        }
                        clientData.playerData.color = newPlayerData.color;
                        availableColors.remove(newPlayerData.color);
                    }

                    currentGrid.addPlayer(clientData.playerData);
                    UpdatePlayerDataRequest clientUpdateRequest
                            = new UpdatePlayerDataRequest(clientData.playerData, true);
                    sendToAll(clientUpdateRequest, newPlayerData.ID);
                }
                panel.getGameStatusPanel().setPlayerPanels(getPlayerRankings());
                break;

            case DISCONNECT:
                if (clientData == null) {
                    break;
                }
                DisconnectRequest disconnect = (DisconnectRequest) request;

                writeToStatusLog(clientData.socket.getRemoteSocketAddress() + " disconnected: " + disconnect.message);
                connectedClients.remove(clientID);
                clientData.socket.close();
                break;

            case SET_UNIT:
                SetUnitRequest setUnit = (SetUnitRequest) request;
                sendToAll(request, clientID);

                if (setUnit.unit != null) { // set unit
                    if (currentGrid.getUnit(setUnit.row, setUnit.col) != null) {
                        break;
                    }
                    currentGrid.setUnit(setUnit.row, setUnit.col, setUnit.unit);

                } else { // remove unit

                    UnitInterface unit = currentGrid.getUnit(setUnit.row, setUnit.col);
                    if (unit == null) {
                        break;
                    }
                    if (unit.getPlayerID() != playerData.ID) {
                        break;
                    }
                    currentGrid.removeUnit(setUnit.row, setUnit.col);

                }

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
        if (remoteInstance) {
            return;
        }
        try {
            currentGrid.resize(rows, cols);
        } catch (Exception ex) {
            writeToStatusLog(ex.toString());
        }
    }

    public void sendLogMessage(String msg) {
        writeToStatusLog(msg);
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

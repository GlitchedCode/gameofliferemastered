/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.simulation;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.PlayerData;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.grid.GameMode;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.grid.Grid;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.SpeciesLoader;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author glitchedcode
 */
public class SimulationCLIServerIT {

    SimulationCLIServer server;
    SimulationRemoteClient[] clients;
    SpeciesLoader loader;
    
    ReentrantLock lock = new ReentrantLock();

    @Before
    public void setUp() throws Exception {
        server = new SimulationCLIServer(8000, 4, 10, 10, GameMode.SANDBOX);
        server.reloadSpeciesConf("species/testSpecies.json");
        loader = server.getSpeciesLoader();
        server.computeNextTurn();

        clients = new SimulationRemoteClient[]{
            new SimulationRemoteClient("test0", "localhost", 8000),
            new SimulationRemoteClient("test1", "localhost", 8000),
            new SimulationRemoteClient("test2", "localhost", 8000),
            new SimulationRemoteClient("test3", "localhost", 8000)
        };

        waitForTurn(1);
    }

    private void waitForTurn(int turn) {
        boolean wait = true;
        while (wait) {
            sleep(100);
            wait = false;
            for (SimulationRemoteClient client : clients) {
                if (client.getCurrentTurn() != turn) {
                    wait = true;
                    break;
                }
            }
        }
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis, 0);
        } catch (InterruptedException ex) {
        }
    }

    @After
    public void tearDown() {
        for (SimulationRemoteClient client : clients) {
            client.close();
            sleep(200);
        }
        server.close();
    }

    @Test
    public void testSynchronization() throws Exception {
        System.out.println("synchronization");
        server.computeNextTurn();
        sleep(100);
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                int clientID = (r + c) % 5;
                if (clientID == 4) {
                    continue;
                }

                SimulationRemoteClient client = clients[clientID];
                client.setUnit(r, c, loader.getNewUnit(0, client.getLocalPlayerID()));
                sleep(200);
            }
        }
        for (SimulationRemoteClient client : clients) {
            client.currentGrid = new Grid(10, 10);
        }

        server.synchronize();
        waitForTurn(2);
        for (SimulationRemoteClient client : clients) {
            assertEquals(true, client.currentGrid.areGridContentsEqual(server.currentGrid));
        }
    }

    private boolean isSimulationRunning() {
        boolean ret = true;
        for (SimulationRemoteClient client : clients) {
            ret &= client.isRunning();
        }
        return ret & server.isRunning();
    }

    @Test
    public void testGameStatusRequest() {
        System.out.println("cliGameStatusRequest");
        assertEquals(false, isSimulationRunning());
        clients[0].setRunning(true);
        sleep(100);
        assertEquals(false, isSimulationRunning());
        clients[1].setRunning(true);
        sleep(100);
        clients[2].setRunning(true);
        sleep(100);
        clients[3].setRunning(true);
        assertEquals(true, isSimulationRunning());
        server.setRunning(false);
        sleep(100);
        assertEquals(false, isSimulationRunning());
    }
    
    @Test
    public void testUpdatePlayerDataRequest() {
        System.out.println("updatePlayerDataRequest");
        for(SimulationRemoteClient client: clients) {
            assertEquals(true, client.localPlayerData.color != PlayerData.TeamColor.NONE);
        }
        for(ClientData client : server.connectedClients.values()) {
            assertEquals(true, client.playerData.name != null);
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.simulation;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.grid.GameMode;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.grid.Grid;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.SpeciesLoader;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author glitchedcode
 */
public class SimulationCLIServerTest {

    SimulationCLIServer cliServer;
    SimulationRemoteClient[] clients;

    ReentrantLock lock = new ReentrantLock();

    @BeforeClass
    public static void setUpClass() throws Exception {
        SpeciesLoader.loadSpeciesFromLocalJSON("testSpecies.json");
    }

    @AfterClass
    public static void tearDownClass() {
        SpeciesLoader.tearDown();
    }

    @Before
    public void setUp() throws Exception {
        cliServer = new SimulationCLIServer(8000, 4, 10, 10, GameMode.SANDBOX);
        cliServer.computeNextTurn();

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
        cliServer.close();
    }

    @Test
    public void testSynchronization() throws Exception {
        System.out.println("synchronization");
        cliServer.computeNextTurn();
        sleep(100);
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                int clientID = (r + c) % 5;
                if (clientID == 4) {
                    continue;
                }

                SimulationRemoteClient client = clients[clientID];
                client.setUnit(r, c, SpeciesLoader.getNewUnit(0, client.getLocalPlayerID()));
                sleep(100);
            }
        }
        for (SimulationRemoteClient client : clients) {
            client.currentGrid = new Grid(10, 10);
        }

        cliServer.synchronize();
        waitForTurn(2);
        for (SimulationRemoteClient client : clients) {
            assertEquals(true, client.currentGrid.areGridContentsEqual(cliServer.currentGrid));
        }
    }
}

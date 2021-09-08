/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.simulation;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.grid.GameMode;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.SpeciesLoader;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.Unit;
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
public class SimulationInterfaceIT {

    static SimulationCLIServer cliServer;
    static SimulationGUIServer guiServer;

    static SimulationRemoteClient[] clients;

    public SimulationInterfaceIT() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        SpeciesLoader.loadSpeciesFromLocalJSON("testSpecies.json");

        cliServer = new SimulationCLIServer(8000, 3, 10, 10, GameMode.SANDBOX);
        guiServer = new SimulationGUIServer("unitTestServer", 8001, 4, 10, 10, GameMode.SANDBOX);
    }

    @AfterClass
    public static void tearDownClass() {
        cliServer.close();
        guiServer.close();

        SpeciesLoader.tearDown();
    }

    @Before
    public void setUp() throws Exception {
        clients = new SimulationRemoteClient[]{
            new SimulationRemoteClient("testClient0", "localhost", 8000),
            new SimulationRemoteClient("testClient1", "localhost", 8000),
            new SimulationRemoteClient("testClient2", "localhost", 8000),
            new SimulationRemoteClient("testClient3", "localhost", 8000)
        };
        
        cliServer.flushStreams();
    }

    @After
    public void tearDown() {
        for (SimulationRemoteClient client : clients) {
            client.close();
        }
        clients = null;
    }

    private void setUnit(int clientIndex, int speciesID, int row, int col) {
        int id = clients[clientIndex].localPlayerData.ID;
        clients[clientIndex].setUnit(row, col, SpeciesLoader.getNewUnit(speciesID, id));
    }

    private boolean areGridsEqual(SimulationRemoteClient client, SimulationCLIServer server) {
        int cols, rows;

        cols = client.getColumnCount();
        rows = client.getRowCount();

        if (cols != server.getColumnCount() | rows != server.getRowCount()) {
            return false;
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Unit clientUnit, serverUnit;
                clientUnit = client.getUnit(r, c);
                serverUnit = server.getUnit(r, c);

                if (!clientUnit.isAlive()) {
                    if (clientUnit.isAlive() == serverUnit.isAlive()) {
                        continue;
                    } else {
                        return false;
                    }
                }

                if (clientUnit != serverUnit) {
                    return false;
                }
            }
        }

        return true;
    }

    @Test
    public void testSynchronization() throws Exception {

        System.out.println("testSynchronization");
        cliServer.computeNextTurn();
        Thread.sleep(1000, 0);

        setUnit(0, 0, 0, 0);
        setUnit(0, 0, 0, 1);
        setUnit(0, 0, 1, 0);
        setUnit(1, 1, 2, 1);
        setUnit(1, 1, 1, 2);
        setUnit(1, 1, 2, 2);

        clients[0].currentGrid.clearBoard();
        clients[1].currentGrid.clearBoard();

        assertEquals(false, clients[0].getUnit(0, 0).isAlive());
        assertEquals(false, clients[1].getUnit(2, 2).isAlive());

        cliServer.synchronize();
        cliServer.flushStreams();

        Thread.sleep(1000, 0);

        assertEquals(true, areGridsEqual(clients[0], cliServer));
        assertEquals(true, areGridsEqual(clients[1], cliServer));

    }
}

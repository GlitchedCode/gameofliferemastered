/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.simulation;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.PlayerData;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.grid.GameMode;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.request.Request;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.SpeciesLoader;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.Unit;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.util.ArrayList;
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

        clients = new SimulationRemoteClient[]{
            new SimulationRemoteClient("test0", "localhost", 8000),
            new SimulationRemoteClient("test1", "localhost", 8000),
            new SimulationRemoteClient("test2", "localhost", 8000),
            new SimulationRemoteClient("test3", "localhost", 8000)
        };
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSynchronization() {
        
    }
}

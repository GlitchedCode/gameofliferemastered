/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.simulation;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.PlayerData;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.grid.GameMode;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.SpeciesLoader;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.Unit;
import com.giuseppelamalfa.gameofliferemastered.simulation.request.Request;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author glitchedcode
 */
public class SimulationCLIServerTest {

    public SimulationCLIServerTest() {
    }

    /**
     * Test of reloadSpeciesConf method, of class SimulationCLIServer.
     */
    @Test
    public void testReloadSpeciesConf() throws Exception {
        System.out.println("reloadSpeciesConf");
        SimulationCLIServer instance = new SimulationCLIServer(10, 10);
        instance.reloadSpeciesConf("species/testSpecies.json");
        assertEquals("Mimic", instance.getSpeciesLoader().getSpeciesData(2).name);
    }

    /**
     * Test of isRemoteInstance method, of class SimulationCLIServer.
     */
    @Test
    public void testIsRemoteInstance() throws Exception {
        System.out.println("isRemoteInstance");
        assertEquals(false, new SimulationCLIServer(10, 10).isRemoteInstance());
        SimulationCLIServer remote = new SimulationCLIServer(7777, 4, 10, 10, GameMode.SANDBOX);
        assertEquals(true, remote.isRemoteInstance());
        remote.close();
    }

    /**
     * Test of getNextClientID method, of class SimulationCLIServer.
     */
    @Test
    public void testGetNextClientID() throws Exception {
        System.out.println("getNextClientID");
        SimulationCLIServer instance = new SimulationCLIServer(10, 10);
        assertEquals(0, instance.getNextClientID());
        assertEquals(1, instance.getNextClientID());
        assertEquals(2, instance.getNextClientID());
    }

    /**
     * Test of extractRandomColor method, of class SimulationCLIServer.
     */
    @Test
    public void testExtractRandomColor() throws Exception {
        System.out.println("extractRandomColor");
        SimulationCLIServer instance = new SimulationCLIServer(10,10);
        PlayerData.TeamColor color = instance.extractRandomColor();
        HashSet<PlayerData.TeamColor> extracted = new HashSet<>();
        while (color != PlayerData.TeamColor.NONE){
            assertEquals(false, extracted.contains(color));
            extracted.add(color);
            color = instance.extractRandomColor();
        }
        assertEquals(true, extracted.size() > 0);
    }

    /**
     * Test of isLocked method, of class SimulationCLIServer.
     */
    @Test
    public void testIsLocked() {
        System.out.println("isLocked");
        SimulationCLIServer instance = null;
        boolean expResult = false;
        boolean result = instance.isLocked();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isRunning method, of class SimulationCLIServer.
     */
    @Test
    public void testIsRunning() {
        System.out.println("isRunning");
        SimulationCLIServer instance = null;
        boolean expResult = false;
        boolean result = instance.isRunning();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isLocallyControlled method, of class SimulationCLIServer.
     */
    @Test
    public void testIsLocallyControlled() {
        System.out.println("isLocallyControlled");
        SimulationCLIServer instance = null;
        boolean expResult = false;
        boolean result = instance.isLocallyControlled();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getGameModeName method, of class SimulationCLIServer.
     */
    @Test
    public void testGetGameModeName() {
        System.out.println("getGameModeName");
        SimulationCLIServer instance = null;
        String expResult = "";
        String result = instance.getGameModeName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLocalPlayerID method, of class SimulationCLIServer.
     */
    @Test
    public void testGetLocalPlayerID() {
        System.out.println("getLocalPlayerID");
        SimulationCLIServer instance = null;
        int expResult = 0;
        int result = instance.getLocalPlayerID();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPlayerColor method, of class SimulationCLIServer.
     */
    @Test
    public void testGetPlayerColor() {
        System.out.println("getPlayerColor");
        int ID = 0;
        SimulationCLIServer instance = null;
        PlayerData.TeamColor expResult = null;
        PlayerData.TeamColor result = instance.getPlayerColor(ID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPlayerRankings method, of class SimulationCLIServer.
     */
    @Test
    public void testGetPlayerRankings() {
        System.out.println("getPlayerRankings");
        SimulationCLIServer instance = null;
        ArrayList<PlayerData> expResult = null;
        ArrayList<PlayerData> result = instance.getPlayerRankings();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSpeciesLoader method, of class SimulationCLIServer.
     */
    @Test
    public void testGetSpeciesLoader() {
        System.out.println("getSpeciesLoader");
        SimulationCLIServer instance = null;
        SpeciesLoader expResult = null;
        SpeciesLoader result = instance.getSpeciesLoader();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getRowCount method, of class SimulationCLIServer.
     */
    @Test
    public void testGetRowCount() {
        System.out.println("getRowCount");
        SimulationCLIServer instance = null;
        int expResult = 0;
        int result = instance.getRowCount();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getColumnCount method, of class SimulationCLIServer.
     */
    @Test
    public void testGetColumnCount() {
        System.out.println("getColumnCount");
        SimulationCLIServer instance = null;
        int expResult = 0;
        int result = instance.getColumnCount();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSectorSideLength method, of class SimulationCLIServer.
     */
    @Test
    public void testGetSectorSideLength() {
        System.out.println("getSectorSideLength");
        SimulationCLIServer instance = null;
        int expResult = 0;
        int result = instance.getSectorSideLength();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStatusString method, of class SimulationCLIServer.
     */
    @Test
    public void testGetStatusString() {
        System.out.println("getStatusString");
        SimulationCLIServer instance = null;
        String expResult = "";
        String result = instance.getStatusString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCurrentTurn method, of class SimulationCLIServer.
     */
    @Test
    public void testGetCurrentTurn() {
        System.out.println("getCurrentTurn");
        SimulationCLIServer instance = null;
        int expResult = 0;
        int result = instance.getCurrentTurn();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUnit method, of class SimulationCLIServer.
     */
    @Test
    public void testGetUnit() {
        System.out.println("getUnit");
        int row = 0;
        int col = 0;
        SimulationCLIServer instance = null;
        Unit expResult = null;
        Unit result = instance.getUnit(row, col);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeUnit method, of class SimulationCLIServer.
     */
    @Test
    public void testRemoveUnit() {
        System.out.println("removeUnit");
        int row = 0;
        int col = 0;
        SimulationCLIServer instance = null;
        instance.removeUnit(row, col);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setUnit method, of class SimulationCLIServer.
     */
    @Test
    public void testSetUnit() {
        System.out.println("setUnit");
        int row = 0;
        int col = 0;
        Unit unit = null;
        SimulationCLIServer instance = null;
        instance.setUnit(row, col, unit);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of computeNextTurn method, of class SimulationCLIServer.
     */
    @Test
    public void testComputeNextTurn() throws Exception {
        System.out.println("computeNextTurn");
        SimulationCLIServer instance = null;
        instance.computeNextTurn();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of synchronize method, of class SimulationCLIServer.
     */
    @Test
    public void testSynchronize_0args() {
        System.out.println("synchronize");
        SimulationCLIServer instance = null;
        instance.synchronize();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of synchronize method, of class SimulationCLIServer.
     */
    @Test
    public void testSynchronize_ObjectOutputStream() {
        System.out.println("synchronize");
        ObjectOutputStream output = null;
        SimulationCLIServer instance = null;
        instance.synchronize(output);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sendToAll method, of class SimulationCLIServer.
     */
    @Test
    public void testSendToAll_Request() {
        System.out.println("sendToAll");
        Request req = null;
        SimulationCLIServer instance = null;
        instance.sendToAll(req);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sendToAll method, of class SimulationCLIServer.
     */
    @Test
    public void testSendToAll_Request_int() {
        System.out.println("sendToAll");
        Request req = null;
        int excludeID = 0;
        SimulationCLIServer instance = null;
        instance.sendToAll(req, excludeID);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setRunning method, of class SimulationCLIServer.
     */
    @Test
    public void testSetRunning() {
        System.out.println("setRunning");
        boolean val = false;
        SimulationCLIServer instance = null;
        instance.setRunning(val);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of handleSyncGridRequest method, of class SimulationCLIServer.
     */
    @Test
    public void testHandleSyncGridRequest() {
        System.out.println("handleSyncGridRequest");
        Request r = null;
        Integer clientID = null;
        SimulationCLIServer instance = null;
        instance.handleSyncGridRequest(r, clientID);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of handleUpdatePlayerDataRequest method, of class
     * SimulationCLIServer.
     */
    @Test
    public void testHandleUpdatePlayerDataRequest() {
        System.out.println("handleUpdatePlayerDataRequest");
        Request r = null;
        Integer clientID = null;
        SimulationCLIServer instance = null;
        instance.handleUpdatePlayerDataRequest(r, clientID);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of handleDisconnectRequest method, of class SimulationCLIServer.
     */
    @Test
    public void testHandleDisconnectRequest() {
        System.out.println("handleDisconnectRequest");
        Request r = null;
        Integer clientID = null;
        SimulationCLIServer instance = null;
        instance.handleDisconnectRequest(r, clientID);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of handleSetUnitRequest method, of class SimulationCLIServer.
     */
    @Test
    public void testHandleSetUnitRequest() throws Exception {
        System.out.println("handleSetUnitRequest");
        Request r = null;
        Integer clientID = null;
        SimulationCLIServer instance = null;
        instance.handleSetUnitRequest(r, clientID);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of handleGameStatusRequest method, of class SimulationCLIServer.
     */
    @Test
    public void testHandleGameStatusRequest() {
        System.out.println("handleGameStatusRequest");
        Request r = null;
        Integer clientID = null;
        SimulationCLIServer instance = null;
        instance.handleGameStatusRequest(r, clientID);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of saveGrid method, of class SimulationCLIServer.
     */
    @Test
    public void testSaveGrid() throws Exception {
        System.out.println("saveGrid");
        SimulationCLIServer instance = null;
        instance.saveGrid();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of close method, of class SimulationCLIServer.
     */
    @Test
    public void testClose() {
        System.out.println("close");
        SimulationCLIServer instance = null;
        instance.close();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of resize method, of class SimulationCLIServer.
     */
    @Test
    public void testResize() {
        System.out.println("resize");
        int rows = 0;
        int cols = 0;
        SimulationCLIServer instance = null;
        instance.resize(rows, cols);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sendLogMessage method, of class SimulationCLIServer.
     */
    @Test
    public void testSendLogMessage() {
        System.out.println("sendLogMessage");
        String msg = "";
        SimulationCLIServer instance = null;
        instance.sendLogMessage(msg);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}

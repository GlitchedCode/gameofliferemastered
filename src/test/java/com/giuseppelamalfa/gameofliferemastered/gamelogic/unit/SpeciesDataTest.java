/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import static com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.SpeciesLoader.localSpeciesDataJSON;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author glitchedcode
 */
public class SpeciesDataTest {

    JSONArray unitDataArray;
    HashMap<String, Integer> speciesIDs;

    public SpeciesDataTest(){
        InputStream istream = new SpeciesLoader().getClass().
                getClassLoader().getResourceAsStream("badSpecies.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
        StringBuilder strBuilder = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                strBuilder.append(line);
            }
        } catch (IOException ex) {
            Logger.getLogger(SpeciesDataTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        String speciesDataJSON = strBuilder.toString();
        JSONObject root = new JSONObject(speciesDataJSON);
        unitDataArray = root.getJSONArray("speciesData");

        speciesIDs = new HashMap<>();
        speciesIDs.put("Cell", 0);
        speciesIDs.put("Snake", 1);
    }
    
    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testMissingColorTintFilter() {
        System.out.println("testMissingColorTintFilter");

        try {
            SpeciesData data = new SpeciesData(unitDataArray.getJSONObject(0), speciesIDs);
            if(data.filter != null){
                fail("SpeciesData object should have no filter object");
            }
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException | JSONException e) {
            fail("SpeciesData object should be constructed without throwing exceptions.");
        }
    }
    
    @Test
    public void testMissingTextureCode() {
        System.out.println("testMissingTextureCode");
        boolean failed = false;
        
        try {
            SpeciesData data = new SpeciesData(unitDataArray.getJSONObject(2), speciesIDs);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException | JSONException e) {
            failed = true;
        }
        assertEquals(failed, true);
    }
    
    @Test
    public void testMissingName() {
        System.out.println("testMissingName");
        boolean failed = false;
        
        try {
            SpeciesData data = new SpeciesData(unitDataArray.getJSONObject(3), speciesIDs);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException | JSONException e) {
            failed = true;
        }
        assertEquals(failed, true);
    }
    
    @Test
    public void testMissingID() {
        System.out.println("testMissingID");
        boolean failed = false;
        
        try {
            SpeciesData data = new SpeciesData(unitDataArray.getJSONObject(4), speciesIDs);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException | JSONException e) {
            failed = true;
        }
        assertEquals(failed, true);
    }
    
    @Test
    public void testMissingImplementingType() {
        System.out.println("testMissingImplementingType");
        boolean failed = false;
        
        try {
            SpeciesData data = new SpeciesData(unitDataArray.getJSONObject(5), speciesIDs);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException | JSONException e) {
            failed = true;
        }
        assertEquals(failed, true);
    }
    
    @Test
    public void testInvalidImplementingType() {
        System.out.println("testInvalidImplementingType");
        boolean failed = false;
        
        try {
            SpeciesData data = new SpeciesData(unitDataArray.getJSONObject(6), speciesIDs);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException | JSONException e) {
            failed = true;
        }
        assertEquals(failed, true);
    }
    
    @Test
    public void testMissingInitialState() {
        System.out.println("testMissingInitialState");
        boolean failed = false;
        
        try {
            SpeciesData data = new SpeciesData(unitDataArray.getJSONObject(7), speciesIDs);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException | JSONException e) {
            failed = true;
        }
        assertEquals(failed, true);
    }
    
    @Test
    public void testInvalidInitialState() {
        System.out.println("testInvalidInitialState");
        boolean failed = false;
        
        try {
            SpeciesData data = new SpeciesData(unitDataArray.getJSONObject(8), speciesIDs);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException | JSONException e) {
            failed = true;
        }
        assertEquals(failed, true);
    }
    
    @Test
    public void testMissingHealth() {
        System.out.println("testMissingID");
        boolean failed = false;
        
        try {
            SpeciesData data = new SpeciesData(unitDataArray.getJSONObject(9), speciesIDs);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException | JSONException e) {
            failed = true;
        }
        assertEquals(failed, true);
    }
    
    @Test
    public void testMissingFriendlySpecies() {
        System.out.println("testMissingID");
        boolean failed = false;
        
        try {
            SpeciesData data = new SpeciesData(unitDataArray.getJSONObject(10), speciesIDs);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException | JSONException e) {
            failed = true;
        }
        assertEquals(failed, false);
    }
    
    @Test
    public void testMissingHostileSpecies() {
        System.out.println("testMissingID");
        boolean failed = false;
        
        try {
            SpeciesData data = new SpeciesData(unitDataArray.getJSONObject(11), speciesIDs);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException | JSONException e) {
            failed = true;
        }
        assertEquals(failed, false);
    }
    
    @Test
    public void testMissingFriendlyCountSelector() {
        System.out.println("testMissingFriendlyCountSelector");
        boolean failed = false;
        
        try {
            SpeciesData data = new SpeciesData(unitDataArray.getJSONObject(12), speciesIDs);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException | JSONException e) {
            failed = true;
        }
        assertEquals(failed, false);
    }
    
    @Test
    public void testInvalidFriendlyCountSelector() {
        System.out.println("testInvalidFriendlyCountSelector");
        boolean failed = false;
        
        try {
            SpeciesData data = new SpeciesData(unitDataArray.getJSONObject(13), speciesIDs);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException | JSONException e) {
            failed = true;
        }
        assertEquals(failed, false);
    }
    
    @Test
    public void testInitializedFilter() {
        System.out.println("testInitializedFilter");
        boolean failed = false;
        
        try {
            SpeciesData data = new SpeciesData(unitDataArray.getJSONObject(14), speciesIDs);
            failed = data.filter == null;
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException | JSONException e) {
            failed |= true;
        }
        assertEquals(failed, false);
    }
}

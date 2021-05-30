/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.RuleInterface;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author glitchedcode
 */
public class SpeciesLoader {
    
    static public final String RULE_CLASS_PATH = "com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.";
    static public final String UNIT_CLASS_PATH = "com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.";
    
    static HashMap<Integer, UnitInterface.SpeciesData> speciesData;
    
    public static synchronized void loadUnitClasses() 
            throws IOException, ClassNotFoundException, InstantiationException, 
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {        
        
        InputStream istream = new SpeciesLoader().getClass().
                getClassLoader().getResourceAsStream("species.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
        StringBuilder strBuilder = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null)
            strBuilder.append(line);

        speciesData = new HashMap<>();
        HashMap<String, Integer> speciesIDs = new HashMap<>();
        JSONArray unitDataArray = new JSONObject(strBuilder.toString()).getJSONArray("speciesData");
        
        // Correlate names to IDs
        for(int c = 0; c < unitDataArray.length(); c++)
        {
            JSONObject current = unitDataArray.getJSONObject(c);
            speciesIDs.put(current.getString("name"), current.getInt("id"));
        }
        
        for(int c = 0; c < unitDataArray.length(); c++)
        {
            JSONObject current = unitDataArray.getJSONObject(c);
            speciesData.put(c, new UnitInterface.SpeciesData(speciesIDs.get(current.getString("name")), current, speciesIDs));
        }
    }
    
    public static synchronized UnitInterface.SpeciesData getSpecies(int index){
        return speciesData.get(index);
    }
    
    public static synchronized int getSpeciesCount(){
        return speciesData.size();
    }
    
    public static synchronized UnitInterface getNewUnit(int speciesID, int playerID, boolean competitive) {
        try{
            UnitInterface.SpeciesData data = speciesData.get(speciesID);
            return (UnitInterface) data.constructor.newInstance(data, playerID, competitive);
        }catch (IllegalAccessException | IllegalArgumentException | InstantiationException 
                | SecurityException | InvocationTargetException e) {
            return null;
        }
    }
    
    public static synchronized UnitInterface getNewUnit(int speciesID, int playerID) throws IllegalArgumentException {
        return getNewUnit(speciesID, playerID, false);
    }

    public static synchronized UnitInterface getNewUnit(int speciesID) throws IllegalArgumentException {
        return getNewUnit(speciesID, 0);
    }

}

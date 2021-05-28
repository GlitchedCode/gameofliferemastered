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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author glitchedcode
 */
public class SpeciesLoader {
    
    static private final String rulePath = "com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.";
    static HashMap<Integer, UnitInterface.SpeciesData> speciesData;
    
    public static synchronized void loadUnitClasses() 
            throws IOException, ClassNotFoundException, InstantiationException, 
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {        
        
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
            speciesIDs.put(unitDataArray.getJSONObject(c).getString("name"), c);
        
        for(int c = 0; c < unitDataArray.length(); c++)
        {
            // Calculate friendly and hostile species sets
            JSONObject current = unitDataArray.getJSONObject(c);
            JSONArray friendlies = current.getJSONArray("friendlySpecies");
            HashSet<Integer> friendlySpecies = new HashSet<>();
            JSONArray hostiles = current.getJSONArray("hostileSpecies");
            HashSet<Integer> hostileSpecies = new HashSet<>();

            for(int i = 0; i < friendlies.length(); i++)
                friendlySpecies.add(speciesIDs.get(friendlies.getString(i)));
            for(int i = 0; i < hostiles.length(); i++)
                hostileSpecies.add(speciesIDs.get(friendlies.getString(i)));    
            
            current.put("friendlySpeciesSet", friendlySpecies);
            current.put("hostileSpeciesSet", hostileSpecies);
            
            // Create rule objects
            JSONObject friendlyCountJSON = current.getJSONObject("friendlyCountSelector");
            Class<?> friendlyRule = Class.forName(rulePath + friendlyCountJSON.getString("ruleClassName"));
            JSONObject hostileCountJSON = current.getJSONObject("hostileCountSelector");
            Class<?> hostileRule = Class.forName(rulePath + hostileCountJSON.getString("ruleClassName"));
            JSONObject reproductionCount = current.getJSONObject("reproductionSelector");
            Class<?> reproductionRule = Class.forName(rulePath + reproductionCount.getString("ruleClassName"));
            

            @SuppressWarnings("unchecked")
            RuleInterface<Integer> friendlyCountRule = 
                    (RuleInterface<Integer>)friendlyRule.getConstructors()[0].newInstance(friendlyCountJSON.getJSONArray("args").toList());
            @SuppressWarnings("unchecked")
            RuleInterface<Integer> hostileCountRule = 
                    (RuleInterface<Integer>)hostileRule.getConstructors()[0].newInstance(hostileCountJSON.getJSONArray("args").toList());
            @SuppressWarnings("unchecked")
            RuleInterface<Integer> reproductionCountRule = 
                    (RuleInterface<Integer>)reproductionRule.getConstructors()[0].newInstance(reproductionCount.getJSONArray("args").toList());

            speciesData.put(c, new UnitInterface.SpeciesData(speciesIDs.get(current.getString("name")), current, friendlySpecies, 
                    hostileSpecies, friendlyCountRule, hostileCountRule, reproductionCountRule));
        }
    }
    
    public static synchronized UnitInterface.SpeciesData getSpecies(int index){
        return speciesData.get(index);
    }
    
    public static synchronized int getSpeciesCount(){
        return speciesData.size();
    }
    
    public static synchronized Unit getNewUnit(int speciesID, int playerID) throws IllegalArgumentException {
        return new Unit(speciesData.get(speciesID), playerID);
    }

    public static synchronized Unit getNewUnit(int speciesID) throws IllegalArgumentException {
        return getNewUnit(speciesID, 0);
    }

}

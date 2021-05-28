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
public class UnitClassLoader {
    
    static private final String rulePath = "com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.";
    static HashMap<Integer, UnitInterface.SpeciesData> speciesData;
    
    public static synchronized void loadUnitClasses() 
            throws IOException, ClassNotFoundException, InstantiationException, 
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {        
        
        InputStream istream = new UnitClassLoader().getClass().
                getClassLoader().getResourceAsStream("units.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
        StringBuilder strBuilder = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null)
            strBuilder.append(line);

        speciesData = new HashMap<>();
        HashMap<String, Integer> speciesIDs = new HashMap<>();
        JSONArray unitDataArray = new JSONObject(strBuilder.toString()).getJSONArray("unitData");
        
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
            
            current.put("friendlySpecies", friendlySpecies);
            current.put("hostileSpecies", hostileSpecies);
            
            // Create rule objects
            JSONObject friendlyCount = current.getJSONObject("friendlyCountSelector");
            Class<?> friendlyRule = Class.forName(rulePath + friendlyCount.getString("ruleClassName"));
            JSONObject hostileCount = current.getJSONObject("hostileCountSelector");
            Class<?> hostileRule = Class.forName(rulePath + friendlyCount.getString("ruleClassName"));
            JSONObject reproductionCount = current.getJSONObject("reproductionSelector");
            Class<?> reproductionRule = Class.forName(rulePath + friendlyCount.getString("ruleClassName"));
            
            try {
                current.put("friendlyCountSelector", friendlyRule.getConstructors()[0].newInstance(friendlyCount.getJSONArray("args").toList()));
                current.put("hostileCountSelector", hostileRule.getConstructors()[0].newInstance(friendlyCount.getJSONArray("args").toList()));
                current.put("reproductionSelector", reproductionRule.getConstructors()[0].newInstance(friendlyCount.getJSONArray("args").toList()));
            } catch (InvocationTargetException ex) {
                ex.printStackTrace();
            }
                        
            speciesData.put(c, new UnitInterface.SpeciesData(c, current));
        }
    }
}

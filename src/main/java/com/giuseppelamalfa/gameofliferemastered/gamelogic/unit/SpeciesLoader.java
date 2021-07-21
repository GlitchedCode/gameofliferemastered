/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author glitchedcode
 */
public class SpeciesLoader {

    static public final String RULE_CLASS_PATH = "com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.";
    static public final String UNIT_CLASS_PATH = "com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.";

    static String localSpeciesDataJSON = "";

    static HashMap<Integer, SpeciesData> speciesData;

    public static synchronized void loadSpeciesFromJSON()
            throws IOException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {

        // Load species.json into localSpeciesDataJSON
        InputStream istream = new SpeciesLoader().getClass().
                getClassLoader().getResourceAsStream("species.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
        StringBuilder strBuilder = new StringBuilder();
        String line;
        while ( (line = reader.readLine()) != null ) {
            strBuilder.append(line);
        }
        localSpeciesDataJSON = strBuilder.toString();

        loadJSONString(localSpeciesDataJSON);
    }

    public static synchronized void loadJSONString(String jsonString)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException {

        speciesData = new HashMap<>();
        HashMap<String, Integer> speciesIDs = new HashMap<>();
        JSONArray unitDataArray = new JSONObject(jsonString).getJSONArray("speciesData");

        // Correlate names to IDs
        for (int c = 0; c < unitDataArray.length(); c++) {
            JSONObject current = unitDataArray.getJSONObject(c);
            speciesIDs.put(current.getString("name"), current.getInt("id"));
        }

        for (int c = 0; c < unitDataArray.length(); c++) {
            JSONObject current = unitDataArray.getJSONObject(c);
            speciesData.put(c, new SpeciesData(speciesIDs.get(current.getString("name")), current, speciesIDs));
        }
    }

    public static String getLocalSpeciesJSONString() {
        return localSpeciesDataJSON;
    }

    public static synchronized SpeciesData getSpeciesData(int index) {
        return speciesData.get(index);
    }

    public static synchronized int getSpeciesCount() {
        return speciesData.size();
    }

    public static synchronized Unit getNewUnit(int speciesID, int playerID) throws IllegalArgumentException {
        try {
            SpeciesData data = speciesData.get(speciesID);
            return (Unit) data.constructor.newInstance(data, playerID);
        }
        catch (IllegalAccessException | IllegalArgumentException | InstantiationException
                | SecurityException | InvocationTargetException e) {
            return null;
        }
    }

    public static synchronized Unit getNewUnit(int speciesID) throws IllegalArgumentException {
        return getNewUnit(speciesID, 0);
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import java.awt.Color;
import java.awt.image.BufferedImageOp;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author glitchedcode
 */
public class SpeciesLoader {

    static public final String RULE_CLASS_PATH = "com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.";
    static public final String UNIT_CLASS_PATH = "com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.";

    static private String customSpeciesConfigPath = null;

    String loadedJSON = "";

    HashMap<Integer, SpeciesData> speciesData = new HashMap<>();

    public static void setCustomSpeciesConfigPath(String filename) throws Exception {
        if (customSpeciesConfigPath != null) {
            throw new Exception("Custom species config path was already set!");
        }

        Path path = Paths.get(filename);
        File file = path.toFile();
        if (file.exists() && file.canRead() && file.isFile()) {
            customSpeciesConfigPath = filename;
        } else {
            throw new InvalidPathException(filename, "Could not read from file.");
        }
    }

    public synchronized void loadSpeciesFromCustomConfig() throws Exception {
        if (customSpeciesConfigPath == null) {
            throw new Exception("Custom species config path was not set!");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(customSpeciesConfigPath))));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        loadedJSON = builder.toString();
        loadJSONString(loadedJSON);
    }

    public synchronized void loadSpeciesFromLocalJSON(String path)
            throws IOException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, Exception {

        // Load species.json into loadedJSON
        InputStream istream = getClass().getClassLoader().getResourceAsStream(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
        StringBuilder strBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            strBuilder.append(line);
        }
        loadedJSON = strBuilder.toString();

        loadJSONString(loadedJSON);
    }

    public synchronized void loadSpeciesFromLocalJSON() throws Exception {
        loadSpeciesFromLocalJSON("species.json");
    }

    public synchronized void loadSpecies() throws Exception {
        if (customSpeciesConfigPath != null) {
            loadSpeciesFromCustomConfig();
        } else {
            loadSpeciesFromLocalJSON();
        }
    }

    public synchronized void loadJSONString(String jsonString) throws Exception {

        speciesData = new HashMap<>();
        HashMap<String, Integer> speciesIDs = new HashMap<>();
        JSONArray unitDataArray = new JSONObject(jsonString).getJSONArray("speciesData");

        // Correlate names to IDs
        for (int c = 0; c < unitDataArray.length(); c++) {
            JSONObject current = unitDataArray.getJSONObject(c);
            if (speciesIDs.put(current.getString("name"), current.getInt("id")) != null) {
                throw new Exception("Species names must be unique!");
            }
        }

        // Create SpeciesData objects from JSON objects
        for (int c = 0; c < unitDataArray.length(); c++) {
            try {
                JSONObject current = unitDataArray.getJSONObject(c);
                int id = current.getInt("id");
                SpeciesData tmp = speciesData.get(id);
                if (tmp != null) {
                    throw new Exception("Species IDs must be unique!");
                }
                speciesData.put(id, new SpeciesData(current, speciesIDs));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getJSONString() {
        return loadedJSON;
    }

    public synchronized SpeciesData getSpeciesData(int index) {
        return speciesData.get(index);
    }

    public synchronized BufferedImageOp getSpeciesFilter(int index) {
        SpeciesData data = getSpeciesData(index);
        if (data != null) {
            return data.filter;
        } else {
            return null;
        }
    }

    public synchronized String getSpeciesTextureCode(int index) {
        SpeciesData data = getSpeciesData(index);
        if (data != null) {
            return data.textureCode;
        } else {
            return null;
        }
    }

    public synchronized Color getSpeciesColor(int index) {
        SpeciesData data = getSpeciesData(index);
        if (data != null) {
            return data.color;
        } else {
            return null;
        }

    }

    public synchronized Set<Integer> getSpeciesIDs() {
        return speciesData.keySet();
    }

    public Unit getNewUnit(int speciesID, int playerID) {
        try {
            SpeciesData data = speciesData.get(speciesID);
            return (Unit) data.constructor.newInstance(data, playerID);
        } catch (Exception e) {
            return null;
        }
    }

    public Unit getNewUnit(int speciesID) {
        return getNewUnit(speciesID, 0);
    }

    public void writePalette(OutputStream stream) {
        PrintStream output = new PrintStream(stream);

        output.println("0,0,0");
        getSpeciesIDs().stream().map(id -> getSpeciesColor(id)).forEachOrdered(c -> {
            output.printf("%d,%d,%d\n", c.getRed(), c.getGreen(), c.getBlue());
        });
    }
}

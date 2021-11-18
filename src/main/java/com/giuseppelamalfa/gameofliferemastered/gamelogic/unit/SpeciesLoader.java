/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.grid.Grid;
import com.giuseppelamalfa.gameofliferemastered.ui.colors.ColorProvider;
import java.awt.image.BufferedImageOp;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
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

    ConcurrentHashMap<Integer, SpeciesData> speciesData = new ConcurrentHashMap<Integer, SpeciesData>(32, 0.9f, Grid.PROCESSOR_COUNT);

    public static void generateTemplateSpeciesConfig(File outFile) throws FileNotFoundException {
        JSONObject root = new JSONObject();
        JSONObject readme = new JSONObject();

        readme.put("__general_info", "each object in the speciesData array represents a species, defined by the following fields."
                + "for more info on implementing types, selectors and initial states check the source code."
                + "a species configuration example has been included to represent conway's life cell.");

        readme.put("name", "the species' name");
        readme.put("implementingType", "unit class name that provides the implementation for this species, must implement com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.Unit");
        readme.put("id", "the species' numeric ID, which must be unique");
        readme.put("textureCode", "the species' texture code");
        readme.put("color", "the species' color for pixel rendering");
        readme.put("filterColor", "the species' texture filter color");
        readme.put("friendlySpecies", "a set of species the unit considers friendly, you can use names or species IDs");
        readme.put("hostileSpecies", "a set of species the unit considers friendly, you can use names or species IDs");
        String statesText = "the unit's initial state, available states are: ";
        for (State state : State.values()) {
            if (state != State.DEAD) {
                statesText += state.toString() + " ";
            }
        }
        readme.put("initialState", statesText);

        JSONObject selectorsReadme = new JSONObject();
        selectorsReadme.put("__help", "rules for HP reduction and reproduction based on how many friendly or hostile units are adjacent");
        selectorsReadme.put("ruleClassName", "rule's class name, derived from com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.RuleInterface");
        selectorsReadme.put("args", "constructor arguments for rule class");
        readme.put("friendlyCountSelector, hostileCountSelector, reproductionSelector", selectorsReadme);

        readme.put("color", "the species' color for pixel rendering");

        root.put("README", readme);

        JSONArray speciesArray = new JSONArray();
        speciesArray.put(SpeciesData.lifeSpecies.toJSONObject());
        root.put("speciesData", speciesArray);

        PrintStream stream = new PrintStream(outFile);
        stream.print(root.toString(4));
    }

    public static void setCustomSpeciesConfigPath(String filename) throws Exception {
        if (customSpeciesConfigPath != null) {
            throw new Exception("Custom species config path was already set!");
        }
        try {
            Path path = Paths.get(filename);
            File file = path.toFile();
            customSpeciesConfigPath = filename;
            if (!file.exists()) {
                System.out.println("Attempting to create an example species configuration template.");
                file.createNewFile();
                generateTemplateSpeciesConfig(file);
            } else if (!file.canRead()) {
                customSpeciesConfigPath = null;
            }
        } catch (Exception e) {
            System.err.println("Could not read custom species config.");
            customSpeciesConfigPath = null;
        }
    }

    public void loadSpeciesFromCustomConfig() throws Exception {
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

    public void loadSpeciesFromLocalJSON(String path)
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

    public void loadSpeciesFromLocalJSON() throws Exception {
        loadSpeciesFromLocalJSON("species.json");
    }

    public void loadSpecies() throws Exception {
        if (customSpeciesConfigPath != null) {
            loadSpeciesFromCustomConfig();
        } else {
            loadSpeciesFromLocalJSON();
        }
    }

    public void loadJSONString(String jsonString) throws Exception {

        speciesData = new ConcurrentHashMap<>(32, 0.75f, Grid.PROCESSOR_COUNT);
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

    public SpeciesData getSpeciesData(int index) {
        return speciesData.get(index);
    }

    public BufferedImageOp getSpeciesFilter(int index) {
        SpeciesData data = getSpeciesData(index);
        if (data != null) {
            return data.filter;
        } else {
            return null;
        }
    }

    public String getSpeciesTextureCode(int index) {
        SpeciesData data = getSpeciesData(index);
        if (data != null) {
            return data.textureCode;
        } else {
            return null;
        }
    }

    public ColorProvider getSpeciesColor(int index) {
        SpeciesData data = getSpeciesData(index);
        if (data != null) {
            return data.color;
        } else {
            return null;
        }

    }

    public void updateColors(double delta) {
        for (SpeciesData species : speciesData.values()) {
            species.color.update(delta);
        }
    }

    public Collection<Integer> getSpeciesIDs() {
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
        getSpeciesIDs().stream().map(id -> getSpeciesColor(id).mainColor()).forEachOrdered(c -> {
            output.printf("%d,%d,%d\n", c.getRed(), c.getGreen(), c.getBlue());
        });
    }
}

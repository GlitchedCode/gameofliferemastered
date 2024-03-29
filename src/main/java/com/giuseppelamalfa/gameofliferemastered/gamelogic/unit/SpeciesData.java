/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.IntegerRangeRule;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.IntegerSetRule;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.RuleInterface;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.StubRule;
import com.giuseppelamalfa.gameofliferemastered.ui.colors.ColorProvider;
import com.giuseppelamalfa.gameofliferemastered.ui.colors.FlatColorProvider;
import com.giuseppelamalfa.gameofliferemastered.utils.ColorTintFilter;
import java.awt.Color;
import java.awt.image.BufferedImageOp;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author glitchedcode
 */
public class SpeciesData implements Serializable {

    public final int speciesID;
    public final String name;
    public final String textureCode;
    public final State initialState;
    public final Integer health;
    public final Set<Integer> friendlySpecies;
    public final Set<Integer> hostileSpecies;
    public final RuleInterface<Integer> friendlyCountSelector;
    public final RuleInterface<Integer> hostileCountSelector;
    public final RuleInterface<Integer> reproductionSelector;

    // WARNING: THE FOLLOWING TRANSIENT FIELDS SHOULD ONLY
    // BE ACCESSED FROM SPECIESLOADER!
    protected final transient Constructor<?> constructor;
    protected final transient BufferedImageOp filter;
    protected final transient ColorProvider color;

    public static final SpeciesData lifeSpecies = new SpeciesData(0, "Life", new FlatColorProvider(new Color(0xa07d2b)), "cell", State.ALIVE, 1,
            new HashSet<Integer>(Arrays.asList(new Integer[]{0})),
            new HashSet<Integer>(),
            new IntegerRangeRule(2, 3), new StubRule<Integer>(true), new IntegerSetRule(Arrays.asList(new Integer[]{3})),
            LifeUnit.class, 0);

    private HashSet<Integer> getSpeciesFromJSONArray(JSONArray array, HashMap<String, Integer> speciesIDs) {
        HashSet<Integer> ret = new HashSet<>();
        for (int i = 0; i < array.length(); i++) {
            Object obj = array.get(i);
            if (obj instanceof String) {
                String name = (String) obj;
                if (speciesIDs.containsKey(name)) {
                    int id = speciesIDs.get(name);
                    ret.add(id);
                }
            } else if (obj instanceof Integer) {
                int id = (Integer) obj;
                if (speciesIDs.values().contains(id)) {
                    ret.add(id);
                }
            }
        }
        return ret;
    }

    public SpeciesData(
            int speciesID,
            String name,
            ColorProvider color,
            String textureCode,
            State initialState,
            int health,
            Set<Integer> friendlySpecies,
            Set<Integer> hostileSpecies,
            RuleInterface<Integer> friendlyCountSelector,
            RuleInterface<Integer> hostileCountSelector,
            RuleInterface<Integer> reproductionSelector,
            Class<?> clazz,
            int filterColor
    ) {

        this.speciesID = Math.max(speciesID, 0);
        this.name = name;
        this.color = color;
        this.textureCode = textureCode;
        this.initialState = initialState;
        this.health = health;
        this.friendlySpecies = friendlySpecies;
        this.hostileSpecies = hostileSpecies;
        this.friendlyCountSelector = friendlyCountSelector;
        this.hostileCountSelector = hostileCountSelector;
        this.reproductionSelector = reproductionSelector;

        if (filterColor == 0) {
            filter = new ColorTintFilter(new Color(filterColor), 0.75f);
        } else {
            filter = null;
        }
        
        Constructor<?> _con;
        try {
            _con = clazz.getConstructor(SpeciesData.class, Integer.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            _con = null;
        }
        constructor = _con;
    }

    public JSONObject toJSONObject() {
        JSONObject ret = new JSONObject();
        
        ret.put("name", name);
        ret.put("implementingType", constructor.getDeclaringClass().getSimpleName());
        ret.put("id", speciesID);
        ret.put("textureCode", textureCode);
        ret.put("color", color.toJSONObject());
        ret.put("filterColor", Integer.toUnsignedString(((ColorTintFilter) filter).mixColor.getRGB()));
        ret.put("health", health);
        ret.put("initialState", initialState.toString());
        
        ret.put("friendlySpecies", new JSONArray(friendlySpecies));
        ret.put("hostileSpecies", new JSONArray(hostileSpecies));

        JSONObject friendlyCount = friendlyCountSelector.toJSONObject();
        JSONObject hostileCount = hostileCountSelector.toJSONObject();
        JSONObject reproductionCount = reproductionSelector.toJSONObject();
        if (friendlyCount != null) {
            ret.put("friendlyCountSelector", friendlyCount);
        }
        if (hostileCount != null) {
            ret.put("hostileCountSelector", hostileCount);
        }
        if (reproductionCount != null) {
            ret.put("reproductionSelector", reproductionCount);
        }

        return ret;
    }

    @SuppressWarnings(value = {"unchecked", "unchecked", "unchecked"})
    protected SpeciesData(JSONObject obj, HashMap<String, Integer> speciesIDs) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, Exception {

        speciesID = obj.getInt("id");
        if (speciesID < 0) {
            throw new Exception("Invalid species ID!");
        }
        name = obj.getString("name");

        String implementingTypeName;
        try {
            implementingTypeName = obj.getString("implementingType");
            Class<?> implementingClass = Class.forName(SpeciesLoader.UNIT_CLASS_PATH + implementingTypeName);
            constructor = implementingClass.getConstructor(SpeciesData.class, Integer.class);
        } catch (Exception e) {
            throw new Exception("Invalid or missing implementing type for species" + speciesID + ":" + name);
        }

        textureCode = obj.getString("textureCode");
        initialState = State.valueOf(obj.getString("initialState"));
        health = obj.getInt("health");

        ColorProvider _color;
        try{
            JSONObject colorJSON = obj.getJSONObject("color");
            _color = (ColorProvider) ColorProvider.Type.valueOf(colorJSON.getString("type")).constructFromJSON(colorJSON);
        } catch (Exception e) {
            _color = new FlatColorProvider(new Color(0));
        }
        color = _color;

        // Calculate friendly and hostile species sets
        HashSet<Integer> _friendlySpecies;
        HashSet<Integer> _hostileSpecies;
        try {
            _friendlySpecies = getSpeciesFromJSONArray(obj.getJSONArray("friendlySpecies"), speciesIDs);
        } catch (JSONException e) {
            // ignore if missing 
            _friendlySpecies = new HashSet<>();
        }
        try {
            _hostileSpecies = getSpeciesFromJSONArray(obj.getJSONArray("hostileSpecies"), speciesIDs);
        } catch (JSONException e) {
            // ignore if missing
            _hostileSpecies = new HashSet<>();
        }
        this.friendlySpecies = (HashSet<Integer>) _friendlySpecies.clone();
        this.hostileSpecies = (HashSet<Integer>) _hostileSpecies.clone();

        // Create rule objects
        RuleInterface<Integer> _friendlyCountSelector;
        RuleInterface<Integer> _hostileCountSelector;
        RuleInterface<Integer> _reproductionSelector;
        try {
            JSONObject friendlyCountJSON = obj.getJSONObject("friendlyCountSelector");
            Class<?> friendlyRuleClass = Class.forName(SpeciesLoader.RULE_CLASS_PATH + friendlyCountJSON.getString("ruleClassName"));
            _friendlyCountSelector = (RuleInterface<Integer>) friendlyRuleClass.getConstructor(Collection.class).newInstance(friendlyCountJSON.getJSONArray("args").toList());
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException | JSONException e) {
            _friendlyCountSelector = new StubRule<>(true);
        }
        try {
            JSONObject hostileCountJSON = obj.getJSONObject("hostileCountSelector");
            Class<?> hostileRuleClass = Class.forName(SpeciesLoader.RULE_CLASS_PATH + hostileCountJSON.getString("ruleClassName"));
            _hostileCountSelector = (RuleInterface<Integer>) hostileRuleClass.getConstructor(Collection.class).newInstance(hostileCountJSON.getJSONArray("args").toList());
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException | JSONException e) {
            _hostileCountSelector = new StubRule<>(true);
        }
        try {
            JSONObject reproductionCountJSON = obj.getJSONObject("reproductionSelector");
            Class<?> reproductionRuleClass = Class.forName(SpeciesLoader.RULE_CLASS_PATH + reproductionCountJSON.getString("ruleClassName"));
            _reproductionSelector = (RuleInterface<Integer>) reproductionRuleClass.getConstructor(Collection.class).newInstance(reproductionCountJSON.getJSONArray("args").toList());
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException | JSONException e) {
            _reproductionSelector = new StubRule<>();
        }
        this.friendlyCountSelector = _friendlyCountSelector;
        this.hostileCountSelector = _hostileCountSelector;
        this.reproductionSelector = _reproductionSelector;
        BufferedImageOp _filter = null;
        try {
            int filterColor = Integer.decode(obj.getString("filterColor"));
            _filter = new ColorTintFilter(new Color(filterColor), 0.75f);
        } catch (NumberFormatException | JSONException e) {
            _filter = null;
        }
        filter = _filter;
    } // Calculate friendly and hostile species sets
    // Create rule objects

    public HashSet<Integer> getFriendlySpeciesCopy() {
        return new HashSet<>(friendlySpecies);
    }

    public HashSet<Integer> getHostileSpeciesCopy() {
        return new HashSet<>(hostileSpecies);
    }
}

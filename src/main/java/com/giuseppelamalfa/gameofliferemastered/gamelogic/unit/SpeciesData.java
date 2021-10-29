/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.RuleInterface;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.StubRule;
import com.giuseppelamalfa.gameofliferemastered.utils.ColorTintFilter;
import java.awt.Color;
import java.awt.image.BufferedImageOp;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
    public final Color color;
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
                if(speciesIDs.values().contains(id))
                    ret.add(id);
            }
        }
        return ret;
    }
    
    @SuppressWarnings(value = {"unchecked", "unchecked", "unchecked"})
    protected SpeciesData(JSONObject obj, HashMap<String, Integer> speciesIDs) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, Exception {
        String implementingTypeName;
        try {
            implementingTypeName = obj.getString("implementingType");
        } catch (Exception e) {
            implementingTypeName = "Unit";
        }
        
        speciesID = obj.getInt("id");
        name = obj.getString("name");
        
        try{
            Class<?> implementingClass = Class.forName(SpeciesLoader.UNIT_CLASS_PATH + implementingTypeName);
            constructor = implementingClass.getConstructor(SpeciesData.class, Integer.class);
        } catch (Exception e) {
            throw new Exception("Invalid or missing implementing type for species" + speciesID + ":" + name);
        }        
        
        textureCode = obj.getString("textureCode");
        initialState = State.valueOf(obj.getString("initialState"));
        health = obj.getInt("health");

        Color _color;
        try {
            _color = new Color(Integer.decode(obj.getString("color")));
        } catch (Exception e) {
            _color = Color.BLACK;
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

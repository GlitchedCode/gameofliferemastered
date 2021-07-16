/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.RuleInterface;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.StubRule;
import com.giuseppelamalfa.gameofliferemastered.ui.ColorTintFilter;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
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
public class SpeciesData {

    //public final Class<?>               implementingClass;
    public final Constructor<?> constructor;
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

    public final BufferedImageOp filter;

    @SuppressWarnings(value = {"unchecked", "unchecked", "unchecked"})
    protected SpeciesData(int ID, JSONObject obj, HashMap<String, Integer> speciesIDs) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        String implementingTypeName;
        try {
            implementingTypeName = obj.getString("implementingType");
        } catch (Exception e) {
            implementingTypeName = "Unit";
        }
        Class<?> implementingClass = Class.forName(SpeciesLoader.UNIT_CLASS_PATH + implementingTypeName);
        constructor = implementingClass.getConstructor(SpeciesData.class, Integer.class);
        speciesID = ID;
        name = obj.getString("name");
        textureCode = obj.getString("textureCode");
        initialState = State.valueOf(obj.getString("initialState"));
        health = obj.getInt("health");
        // Calculate friendly and hostile species sets
        JSONArray friendlies = obj.getJSONArray("friendlySpecies");
        JSONArray hostiles = obj.getJSONArray("hostileSpecies");
        HashSet<Integer> _friendlySpecies = new HashSet<>();
        HashSet<Integer> _hostileSpecies = new HashSet<>();
        for (int i = 0; i < friendlies.length(); i++) {
            _friendlySpecies.add(speciesIDs.get(friendlies.getString(i)));
        }
        for (int i = 0; i < hostiles.length(); i++) {
            _hostileSpecies.add(speciesIDs.get(friendlies.getString(i)));
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
            _friendlyCountSelector = new StubRule<>();
        }
        try {
            JSONObject hostileCountJSON = obj.getJSONObject("hostileCountSelector");
            Class<?> hostileRuleClass = Class.forName(SpeciesLoader.RULE_CLASS_PATH + hostileCountJSON.getString("ruleClassName"));
            _hostileCountSelector = (RuleInterface<Integer>) hostileRuleClass.getConstructor(Collection.class).newInstance(hostileCountJSON.getJSONArray("args").toList());
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException | JSONException e) {
            _hostileCountSelector = new StubRule<>();
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
        } catch (Exception e) {
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

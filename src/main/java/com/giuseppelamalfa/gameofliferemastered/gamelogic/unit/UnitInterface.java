/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.GameLogicException;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.RuleInterface;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.StubRule;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.state.*;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author glitchedcode
 */
public abstract interface UnitInterface
{
    public class SpeciesData {

        //public final Class<?>               implementingClass;
        public final Constructor<?>         constructor;
        
        public final int                    speciesID;
        public final String                 name;
        public final String                 textureCode;
        public final State                  initialState;
        public final Integer                health;
        
        public final Set<Integer>           friendlySpecies;
        public final Set<Integer>           hostileSpecies;
        
        public final RuleInterface<Integer> friendlyCountSelector;
        public final RuleInterface<Integer> hostileCountSelector;
        public final RuleInterface<Integer> reproductionSelector;
        
        @SuppressWarnings({"unchecked", "unchecked", "unchecked"})
        protected SpeciesData(int ID, JSONObject obj, HashMap<String, Integer> speciesIDs) 
                throws ClassNotFoundException, InstantiationException, IllegalAccessException, 
                IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
            
            String implementingTypeName;
            try{
                implementingTypeName = obj.getString("implementingType");
            } catch (Exception e){
                implementingTypeName = "Unit";
            }
            Class<?> implementingClass = Class.forName(SpeciesLoader.UNIT_CLASS_PATH + implementingTypeName);
            constructor = implementingClass.getConstructor(UnitInterface.SpeciesData.class, Integer.class, Boolean.class);
            
            speciesID = ID;
            name = obj.getString("name");
            textureCode = obj.getString("textureCode");
            initialState = Unit.State.valueOf(obj.getString("initialState"));
            health = obj.getInt("health");
            
            // Calculate friendly and hostile species sets
            JSONArray friendlies = obj.getJSONArray("friendlySpecies");
            JSONArray hostiles = obj.getJSONArray("hostileSpecies");
            
            HashSet<Integer> _friendlySpecies = new HashSet<>();
            HashSet<Integer> _hostileSpecies = new HashSet<>();
            for(int i = 0; i < friendlies.length(); i++)
                _friendlySpecies.add(speciesIDs.get(friendlies.getString(i)));
            for(int i = 0; i < hostiles.length(); i++)
                _hostileSpecies.add(speciesIDs.get(friendlies.getString(i)));
            this.friendlySpecies = Set.copyOf(_friendlySpecies);
            this.hostileSpecies = Set.copyOf(_hostileSpecies);
            
            // Create rule objects
            RuleInterface<Integer> _friendlyCountSelector;
            RuleInterface<Integer> _hostileCountSelector;
            RuleInterface<Integer> _reproductionSelector;
            
            try {
                JSONObject friendlyCountJSON = obj.getJSONObject("friendlyCountSelector");
                Class<?> friendlyRuleClass = Class.forName(SpeciesLoader.RULE_CLASS_PATH + friendlyCountJSON.getString("ruleClassName"));
                _friendlyCountSelector = (RuleInterface<Integer>)friendlyRuleClass.getConstructor(Collection.class)
                    .newInstance(friendlyCountJSON.getJSONArray("args").toList());
            } catch (Exception e) { _friendlyCountSelector = new StubRule<Integer>(); }
            try {
                JSONObject hostileCountJSON = obj.getJSONObject("hostileCountSelector");
                Class<?> hostileRuleClass = Class.forName(SpeciesLoader.RULE_CLASS_PATH + hostileCountJSON.getString("ruleClassName"));
                _hostileCountSelector = (RuleInterface<Integer>)hostileRuleClass.getConstructor(Collection.class)
                    .newInstance(hostileCountJSON.getJSONArray("args").toList());
            } catch (Exception e) { _hostileCountSelector = new StubRule<Integer>(); }
            try {
                JSONObject reproductionCountJSON = obj.getJSONObject("reproductionSelector");
                Class<?> reproductionRuleClass = Class.forName(SpeciesLoader.RULE_CLASS_PATH + reproductionCountJSON.getString("ruleClassName"));
                _reproductionSelector = (RuleInterface<Integer>)reproductionRuleClass.getConstructor(Collection.class)
                    .newInstance(reproductionCountJSON.getJSONArray("args").toList());
            } catch (Exception e) { _reproductionSelector = new StubRule<Integer>(); }
            
            this.friendlyCountSelector = _friendlyCountSelector;
            this.hostileCountSelector = _hostileCountSelector;
            this.reproductionSelector = _reproductionSelector;
        }
        
        public HashSet<Integer> getFriendlySpeciesCopy() { return new HashSet<>(friendlySpecies); }
        public HashSet<Integer> getHostileSpeciesCopy() { return new HashSet<>(hostileSpecies); }

    }
    
    enum State implements Serializable
    {
        INVALID(null),
        DEAD(null),
        ALIVE(null),
        ALIVE_AGING(new AgingStateInterface());
        
        private final StateInterface stateImplementationInterface;
        
        private State(StateInterface iface)
        {
            stateImplementationInterface = iface;
        }
        
        public final void enter(UnitInterface unit)
        {
            if ( stateImplementationInterface != null )
                stateImplementationInterface.enter(unit);
        }
        
        public final void exit(UnitInterface unit)
        {
            if ( stateImplementationInterface != null )
                stateImplementationInterface.exit(unit);
        }
        
        public final boolean attackModifier(boolean speciesResult, Integer adjacencyPosition)
        {
            if ( stateImplementationInterface != null)
                return stateImplementationInterface.attackModifier(speciesResult, adjacencyPosition);
            else
                return speciesResult;
        }

        public final boolean reproductionModifier(boolean speciesResult, Integer adjacencyPosition)
        {
            if ( stateImplementationInterface != null )
                return stateImplementationInterface.reproductionModifier(speciesResult, adjacencyPosition);
            else
                return speciesResult;
        }
        
        public final void independentAction(UnitInterface unit)
        {
            if ( stateImplementationInterface != null)
                stateImplementationInterface.independentAction(unit);
        }
    }
    
    enum Species
    {
        SNAKE("snake", null),
        CELL("cell", null),
        INVALID("", null);
        
        private final String textureCode;
        private final Class unitClass;
        
        private Species(String textureCode, Class unitClass)
        {
            this.textureCode = textureCode;
            this.unitClass = unitClass;
        }
        
        public final String getTextureCode()
        {
            return textureCode;
        }
        
        public final Class getUnitClass()
        {
            return unitClass;
        }
    }
    
    public static Integer getOppositeDirection(Integer adjacencyPosition)
    {
        return (adjacencyPosition + 4) % 8;
    }
    
    public int                      getPlayerID();
    public boolean                  isCompetitive();
    public void                     setCompetitive(boolean val);
    
    public void                     computeNextTurn(UnitInterface[] adjacentUnits);
    public void                     update();
    public void                     kill();
    
    public boolean                  reproduce(Integer adjacencyPosition);
    public boolean                  attack(Integer adjacencyPosition);
    public void                     independentAction();

    public int                      getSpeciesID();
    public Set<Integer>             getFriendlySpecies();
    public Set<Integer>             getHostileSpecies();
    public RuleInterface<Integer>   getReproductionSelector();

    public boolean                  isAlive();
    public Integer                  getHealth();
    public void                     incrementHealth(int increment);
    public State                    getCurrentState();
    public State                    getNextTurnState() throws GameLogicException;
}

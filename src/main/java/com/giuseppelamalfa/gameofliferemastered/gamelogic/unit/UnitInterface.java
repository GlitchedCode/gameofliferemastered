/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.GameLogicException;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.RuleInterface;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.state.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONObject;

/**
 *
 * @author glitchedcode
 */
public abstract interface UnitInterface
{
    public class SpeciesData {
        public final int speciesID;
        public final String name;
        public final String textureCode;
        public final State initialState;
        public final Integer health;
        
        public final HashSet<Integer> friendlySpecies;
        public final HashSet<Integer> hostileSpecies;
        
        public final RuleInterface<Integer> friendlyCountSelector;
        public final RuleInterface<Integer> hostileCountSelector;
        public final RuleInterface<Integer> reproductionSelector;
        
        @SuppressWarnings("unchecked")
        public SpeciesData(int ID, JSONObject obj, 
                HashSet<Integer> friendlySpecies, HashSet<Integer> hostileSpecies,
                RuleInterface<Integer> friendlyCountSelector, RuleInterface<Integer> hostileCountSelector,
                RuleInterface<Integer> reproductionSelector) {
            speciesID = ID;
            name = obj.getString("name");
            textureCode = obj.getString("textureCode");
            initialState = Unit.State.valueOf(obj.getString("initialState"));
            health = obj.getInt("health");
            
            this.friendlySpecies = friendlySpecies;
            this.hostileSpecies = hostileSpecies;
            this.friendlyCountSelector = friendlyCountSelector;
            this.hostileCountSelector = hostileCountSelector;
            this.reproductionSelector = reproductionSelector;
        }
        
        public HashSet<Integer> getFriendlySpecies() { return new HashSet<>(friendlySpecies); }
        public HashSet<Integer> getHostileSpecies() { return new HashSet<>(hostileSpecies); }

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

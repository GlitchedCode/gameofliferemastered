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
import java.util.Set;

/**
 *
 * @author glitchedcode
 */
public abstract interface UnitInterface
{
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
        SNAKE("snake", Snake.class),
        CELL("cell", Cell.class),
        INVALID("", DeadUnit.class);
        
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

    public Species                  getSpecies();
    public Set<Species>             getFriendlySpecies();
    public Set<Species>             getHostileSpecies();
    public RuleInterface<Integer>   getReproductionSelector();

    public boolean                  isAlive();
    public Integer                  getHealth();
    public void                     incrementHealth(int increment);
    public State                    getCurrentState();
    public State                    getNextTurnState() throws GameLogicException;
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.DeadUnit;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.Cell;
import java.util.Set;
import com.giuseppelamalfa.gameofliferemastered.utils.Rule;

/**
 *
 * @author glitchedcode
 */
public abstract interface UnitInterface 
{
    
    enum State
    {
        INVALID(null),
        DEAD(null),
        ALIVE(null);
        
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
        SNAKE("snake", DeadUnit.class),
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
    
    public void                 computeNextTurn(UnitInterface[] adjacentUnits);
    public void                 update();
    public boolean              reproduce(Integer adjacencyPosition);
    public boolean              attack(Integer adjacencyPosition);
    public void                 independentAction();
    public State                getNextTurnState() throws GameLogicException;
    public State                getCurrentState();
    public Species              getSpecies();
    public Set<Species>         getFriendlySpecies();
    public Set<Species>         getHostileSpecies();
    public Rule<Integer>    getReproductionSelector();
    public Integer              getHealth();
    public void                 incrementHealth(Integer increment);
}

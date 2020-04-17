/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic;

import java.util.Set;

/**
 *
 * @author glitchedcode
 */
public abstract interface UnitInterface 
{
    
    enum State
    {
        INVALID,
        DEAD,
        ALIVE
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
    
    public void         computeNextTurn(UnitInterface[] adjacentUnits);
    public void         update();
    public boolean      reproduce(Integer adjacencyPosition);
    public boolean      attack(Integer adjacencyPosition);
    public void         passiveAction();
    public State        getNextTurnState() throws GameLogicException;
    public State        getCurrentState();
    public Species      getSpecies();
    public Set<Species> getFriendlySpecies();
    public Set<Species> getHostileSpecies();
    public Integer      getMinimumFriendlyUnits();
    public Integer      getHealth();
    public void         incrementHealth(Integer increment);
}

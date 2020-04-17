/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author glitchedcode
 */
public abstract class Unit implements UnitInterface {
    
    // ALL FIELDS MARKED WITH * MUST BE INITIALIZED IN ALL SUBCLASSES
    
    protected State             currentState;   // *
    protected State             nextTurnState;  // *
    
    protected Species           species;        // *
    protected Set<Species>      friendlySpecies;
    protected Set<Species>      hostileSpecies;
    
    protected Integer           health;         // *
    protected boolean           healthChanged;  
    protected Integer           minimumFriendly;
    protected Integer           maximumFriendly;
    protected Integer           minimumHostile;
    protected Integer           maximumHostile;
    
    protected Unit()
    {
        healthChanged = false;
        
        // default population and hostility parameters
        friendlySpecies = new HashSet<>();
        hostileSpecies = new HashSet<>();
        minimumHostile = 0;
        maximumHostile = 9;
        minimumFriendly = 0;
        maximumFriendly = 9;
        
        currentState = State.INVALID;
        nextTurnState = State.ALIVE;
    }
    
    /**
     * Intermediary function to compute the unit's state relative to the board
     * @param adjacentUnits array of units adjacent to this unit
     */
    protected void boardStep(UnitInterface[] adjacentUnits)
    {
        int hostileCount = 0;
        int friendlyCount = 0;
        int healthIncrement = 0;
        
        for (int i = 0; i < 8; i++) // conto le unità ostili ed amichevoli
        {
            UnitInterface current = adjacentUnits[i];
            if (current == null) 
                continue;
            
            Integer oppositeDir = UnitInterface.getOppositeDirection(i);
            if (friendlySpecies.contains(current.getSpecies()))
            {
                friendlyCount++;
            }
            
            // additionally check if the adjacent cell can attack from 
            // their position relative to this cell
            if (current.attack(oppositeDir) & 
                    hostileSpecies.contains(current.getSpecies()))
            {
                hostileCount++;
            }
        }
        
        // rule #1: population
        // rule #2: hostility
        
        boolean friendlyPenalty = friendlyCount < minimumFriendly | friendlyCount > maximumFriendly;
        boolean hostilePenalty = hostileCount < minimumHostile | hostileCount > maximumHostile;
        
        if (friendlyPenalty | hostilePenalty)
        {
            healthIncrement--;
        }
        
        
        if (healthIncrement != 0)
        {
            incrementHealth(healthIncrement);
        }
    }
    
    protected void endStep()
    {
        if (!healthChanged) // rule #4: inactivity
        {
            passiveAction();
        }
        
        if (health < 1) // regola #5: hp
        {
            nextTurnState = State.DEAD;
        }
        else
        {
            nextTurnState = State.ALIVE;
        }
    }
    
    /**
     * Computes the unit's state for the next turn
     * @param adjacentUnits
     */
    @Override
    public final void computeNextTurn(UnitInterface[] adjacentUnits)
    {
        boardStep(adjacentUnits);
        endStep();
    }
    
    /**
     * Updates the unit's status to the next turn
     */
    @Override
    public void update()
    {
        currentState = nextTurnState;
        nextTurnState = State.INVALID;
        healthChanged = false;
    }

    
    /**
     * Returns true if the unit can reproduce based on the unit's position relative to the caller
     * @param adjacencyPostition This unit's position relative to the caller
     * @return
     */
    @Override
    public boolean reproduce(Integer adjacencyPostition)
    {
        boolean ret = currentState == State.DEAD | currentState == State.INVALID;
        return !ret;
    }
    
    /**
     * Returns true if the unit can attack based on the unit's position relative to the caller
     * @param adjacencyPosition This unit's position relative to the caller
     * @return
     */
    @Override
    public boolean attack(Integer adjacencyPosition)
    {
        return currentState != State.DEAD & currentState != State.INVALID;
    }
    
    /**
     * In base alla regola #4, questa funzione viene eseguita
     * durante lo computeNextTurn se i punti vita dell'unità
     * non sono cambiati
     */
    @Override
    public void passiveAction()
    {
        // vuoto
    }
    
    /**
     * Returns the unit's state for the next turn
     * @return 
     * @throws com.giuseppelamalfa.gameofliferemastered.gamelogic.GameLogicException 
     */
    @Override
    public State getNextTurnState() throws GameLogicException
    {
        if (nextTurnState == State.INVALID)
        {
            throw new GameLogicException(this, "Invalid state.");
        }
        return nextTurnState;
    }
    
    /**
     * Get the unit's state in the current turn
     * @return
     */
    @Override
    public final State getCurrentState()
    {
        return currentState;
    }
    
    /**
     * Get the unit's texture code
     * @return
     */
    @Override
    public final Species getSpecies()
    {
        return species;
    }
    
    /**
     *
     * @return set with friendly species
     */
    @Override
    public final Set<Species> getFriendlySpecies()
    {
        return friendlySpecies;
    }
    
    /**
     *
     * @return set with hostile species
     */
    @Override
    public final Set<Species> getHostileSpecies()
    {
        return hostileSpecies;
    }
    
    /**
     *
     * @return lower bound of friendly units adjacent to this unit
     */
    @Override
    public final Integer getMinimumFriendlyUnits()
    {
        return minimumFriendly;
    }
    
    /**
     * @return unit's health points
     */
    @Override
    public final Integer getHealth()
    {
        return health;
    }
    
    /**
     * Increments the unit's health
     * @param increment health increment
     */
    @Override
    public void incrementHealth(Integer increment)
    {
        health = health + increment;
        healthChanged = true;
    }
    
    @Override
    public String toString()
    {
        String ret = species.toString();
        ret += "@" + hashCode();
        return ret;
    }
}

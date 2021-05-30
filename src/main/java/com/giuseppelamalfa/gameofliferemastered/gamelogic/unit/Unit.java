/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.state.State;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.GameLogicException;
import java.util.Set;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.RuleInterface;
import java.io.Serializable;

/**
 *
 * @author glitchedcode
 */
public class Unit implements UnitInterface, Serializable, Cloneable
{   
    public final int speciesID;
    
    protected State currentState = State.INVALID;
    protected State nextTurnState = State.INVALID;

    protected Set<Integer> friendlySpecies;   
    protected Set<Integer> hostileSpecies;

    protected Integer health;
    protected boolean healthChanged = false;

    protected RuleInterface<Integer> friendlyCountSelector;  
    protected RuleInterface<Integer> hostileCountSelector;   
    protected RuleInterface<Integer> reproductionSelector;   
    
    private int playerID;
    private boolean competitive = false;

    private void initSpeciesData(SpeciesData data)
    {
        health = data.health;
        nextTurnState = data.initialState;
        
        friendlySpecies = data.friendlySpecies;
        hostileSpecies = data.hostileSpecies;
        
        friendlyCountSelector = data.friendlyCountSelector;
        hostileCountSelector = data.hostileCountSelector;
        reproductionSelector = data.reproductionSelector;
    }
    
    public Unit(SpeciesData data) {
        speciesID = data.speciesID;
        playerID = 0;
        initSpeciesData(data);
    }
    
    public Unit(SpeciesData data, Integer playerID) {
        this.playerID = playerID;
        speciesID = data.speciesID;

        initSpeciesData(data);
    }
    
    public Unit(SpeciesData data, Integer playerID, Boolean competitive) {
        this.playerID = playerID;
        speciesID = data.speciesID;
        this.competitive = competitive;
        initSpeciesData(data);
    }

    @Override public final int getPlayerID() { return playerID; }
    @Override public final boolean isCompetitive() { return competitive; }
    @Override public final void setCompetitive(boolean val) { competitive = val; }
    
    /**
     * Intermediary function to compute the unit's state relative to the board
     *
     * @param adjacentUnits array of units adjacent to this unit
     */
    protected void boardStep(UnitInterface[] adjacentUnits) {
        int hostileCount = 0;
        int friendlyCount = 0;
        int healthIncrement = 0;

        for (int i = 0; i < 8; i++) // conto le unità ostili ed amichevoli
        {
            UnitInterface current = adjacentUnits[i];
            if (!current.isAlive())
                continue;

            Integer oppositeDir = UnitInterface.getOppositeDirection(i);

            if ( friendlySpecies.contains(current.getSpeciesID()) )
                friendlyCount++;

            // additionally check if the adjacent cell can attack from 
            // their position relative to this cell
            boolean attacked = false;
            if ( hostileSpecies.contains(current.getSpeciesID()) )
                attacked = current.attack(oppositeDir);
            if ( attacked )
                hostileCount++;
        }

        // rule #1: population
        // rule #2: hostility
        boolean friendlyPenalty = !friendlyCountSelector.test(friendlyCount);
        boolean hostilePenalty = !hostileCountSelector.test(hostileCount);

        if ( friendlyPenalty | hostilePenalty )
            healthIncrement--;

        if ( healthIncrement != 0 )
            incrementHealth(healthIncrement);
    }

    protected void endStep() {
        if ( !healthChanged ) // rule #4: inactivity
            independentAction();

        if ( health < 1 ) // rule #5: hp
            nextTurnState = State.DEAD;
        else
            nextTurnState = currentState;
    }

    /**
     * Computes the unit's state for the next turn
     *
     * @param adjacentUnits
     */
    @Override
    public final void computeNextTurn(UnitInterface[] adjacentUnits) {
        boardStep(adjacentUnits);
        endStep();
    }

    /**
     * Updates the unit's status to the next turn
     */
    @Override
    public void update() {
        if ( currentState != nextTurnState ) {
            currentState.exit(this);
            currentState = nextTurnState;
            currentState.enter(this);
            nextTurnState = State.INVALID;
        }
        healthChanged = false;
    }

    @Override
    public void kill() {
        currentState = State.DEAD;
        nextTurnState = State.DEAD;
    }
    
    @Override
    public boolean isAlive() {
        return currentState != State.DEAD & currentState != State.INVALID;
    }

    /**
     * Returns true if the unit can reproduce based on the unit's position
     * relative to the caller
     *
     * @param adjacencyPostition This unit's position relative to the caller
     * @return
     */
    @Override
    public boolean reproduce(Integer adjacencyPostition) {
        return currentState.reproductionModifier(isAlive(), adjacencyPostition);
    }

    /**
     * Returns true if the unit can attack based on the unit's position relative
     * to the caller
     *
     * @param adjacencyPosition This unit's position relative to the caller
     * @return
     */
    @Override
    public boolean attack(Integer adjacencyPosition) {
        return currentState.attackModifier(isAlive(), adjacencyPosition);
    }

    /**
     * In base alla regola #4, questa funzione viene eseguita durante lo
     * computeNextTurn se i punti vita dell'unità non sono cambiati
     */
    @Override
    public void independentAction() {
        currentState.independentAction(this);
    }

    /**
     * Returns the unit's state for the next turn
     *
     * @return
     * @throws
     * com.giuseppelamalfa.gameofliferemastered.gamelogic.GameLogicException
     */
    @Override
    public State getNextTurnState() throws GameLogicException {
        if ( nextTurnState == State.INVALID )
        {
            throw new GameLogicException(this, "Invalid state.");
        }
        return nextTurnState;
    }

    /**
     * Get the unit's state in the current turn
     *
     * @return
     */
    @Override public final State getCurrentState() { return currentState; }

    /**
     * Get the unit's texture code
     *
     * @return
     */
    @Override public final int getSpeciesID() { return speciesID; }

    /**
     * @return set with friendly species
     */
    @Override public final Set<Integer> getFriendlySpecies() { return friendlySpecies; }

    /**
     * @return set with hostile species
     */
    @Override public final Set<Integer> getHostileSpecies() { return hostileSpecies; }

    /**
     *
     * @return lower bound of friendly units adjacent to this unit
     */
    @Override public final RuleInterface<Integer> getReproductionSelector() { return reproductionSelector; }

    /**
     * @return unit's health points
     */
    @Override public final Integer getHealth() { return health; }

    /**
     * Increments the unit's health
     *
     * @param increment health increment
     */
    @Override 
    public void incrementHealth(int increment) {
        health = health + increment;
        healthChanged = true;
    }

    @Override
    public String toString()
    {
        String ret = SpeciesLoader.getSpecies(speciesID).name;
        ret += "@" + hashCode();
        ret += " " + currentState.toString();
        return ret;
    }
    
    @Override
    public Object clone() {
        try {
            return (Unit) super.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.Unit.clone()");
            return this;
        }
    }
}

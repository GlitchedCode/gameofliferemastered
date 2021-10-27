/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.GameLogicException;
import java.util.Set;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.RuleInterface;
import java.awt.Color;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author glitchedcode
 */
public class LifeUnit extends Unit {

    public final int speciesID;

    public final SpeciesData speciesData;

    private State state = null;
    private transient State nextState = null;

    private int health;
    private transient int healthIncrement = 0;

    private boolean stateChanged = true;

    private final int playerID;
    private boolean competitive = false;

    protected void initSpeciesData(SpeciesData data) {
        health = data.health;
        state = data.initialState;
    }

    public LifeUnit(SpeciesData data) {
        speciesData = data;
        speciesID = data.speciesID;
        playerID = 0;
        initSpeciesData(data);
    }

    public LifeUnit(SpeciesData data, Integer playerID) {
        speciesData = data;
        this.playerID = playerID;
        speciesID = data.speciesID;

        initSpeciesData(data);
    }

    public LifeUnit(SpeciesData data, Integer playerID, Boolean competitive) {
        speciesData = data;
        this.playerID = playerID;
        speciesID = data.speciesID;
        this.competitive = competitive;
        initSpeciesData(data);
    }

    @Override
    public final int getPlayerID() {
        return playerID;
    }

    @Override
    public final boolean isCompetitive() {
        return competitive;
    }

    @Override
    public final void setCompetitive(boolean val) {
        competitive = val;
    }

    /**
     * Intermediary function to compute the unit's state relative to the board
     *
     * @param adjacentUnits array of units adjacent to this unit
     */
    protected void boardStep(Unit[] adjacentUnits) {
        int hostileCount = 0;
        int friendlyCount = 0;

        for (int i = 0; i < 8; i++) // conto le unità ostili ed amichevoli
        {
            Unit current = adjacentUnits[i];
            if (!current.isAlive()) {
                continue;
            }

            if (getFriendlySpecies().contains(current.getSpeciesID())) {
                friendlyCount++;
            }

            // additionally check if the adjacent cell can attack from 
            // their position relative to this cell
            boolean attacked = attack(i, current);

            if (attacked) {
                hostileCount++;
            }
        }

        // rule #1: population
        // rule #2: hostility
        boolean friendlyPenalty = !getFriendlyCountSelector().test(friendlyCount);
        boolean hostilePenalty = !getHostileCountSelector().test(hostileCount);

        if (friendlyPenalty | hostilePenalty) {
            incrementHealth(-1);
        }
    }

    protected void endStep() {
        state.independentAction(this);

        if (health + healthIncrement < 1) { // rule #5: hp
            nextState = State.DEAD;
        }

        if (nextState != state & healthIncrement != 0) {
            setStateChanged();
        }
    }

    /**
     * Computes the unit's state for the next turn
     *
     * @param adjacentUnits
     */
    @Override
    public final void computeNextTurn(Unit[] adjacentUnits) {
        boardStep(adjacentUnits);
        endStep();
    }

    protected void setStateChanged() {
        stateChanged = true;
    }

    @Override
    public final boolean isStateChanged() {
        return stateChanged;
    }

    /**
     * Updates the unit's status to the next turn
     */
    @Override
    public void update() {
        health += healthIncrement;
        healthIncrement = 0;

        if (state != nextState & nextState != null) {
            state.exit(this);
            state = nextState;
            state.enter(this);
        }
        nextState = null;
        stateChanged = false;
    }

    @Override
    public void kill() {
        state = State.DEAD;
        nextState = State.DEAD;
    }

    @Override
    public boolean isAlive() {
        return state != State.DEAD & state != null;
    }

    /**
     * Returns true if the unit can reproduce based on the unit's position
     * relative to the caller
     *
     * @param adjacencyPostition This unit's position relative to the caller
     * @return
     */
    @Override
    public boolean reproduce(int adjacencyPostition) {
        return state.reproductionModifier(isAlive(), adjacencyPostition);
    }

    protected boolean isHostile(Unit unit) {
        return (competitive & unit.getPlayerID() != playerID)
                | getHostileSpecies().contains(unit.getSpeciesID());
    }

    /**
     * Returns true if the unit can attack based on the unit's position relative
     * to the caller
     *
     * @param adjacencyPosition This unit's position relative to the caller
     * @return
     */
    @Override
    public boolean attack(int adjacencyPosition, Unit unit) {
        boolean ret = state.attackModifier(isAlive(), adjacencyPosition);
        ret &= isHostile(unit);
        if (ret) {
            unit.incrementHealth(-1);
        }
        return ret;
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
        if (nextState == null) {
            return getCurrentState();
        }
        return nextState;
    }

    protected final void setCurrentState(State arg) {
        if (arg != state) {
            state = arg;
            setStateChanged();
        }
    }

    /**
     * Get the unit's state in the current turn
     *
     * @return
     */
    @Override
    public final State getCurrentState() {
        return state;
    }

    @Override
    public int getActualSpeciesID() {
        return speciesID;
    }

    @Override
    public int getSpeciesID() {
        return getActualSpeciesID();
    }

    @Override
    public SpeciesData getSpeciesData() {
        return speciesData;
    }

    @Override
    public Color getColor(){
        return speciesData.color;
    }
    
    @Override
    public int getBornSpeciesID() {
        return speciesID;
    }

    /**
     * @return set with friendly species
     */
    @Override
    public Set<Integer> getFriendlySpecies() {
        return speciesData.friendlySpecies;
    }

    /**
     * @return set with hostile species
     */
    @Override
    public Set<Integer> getHostileSpecies() {
        return speciesData.hostileSpecies;
    }

    @Override
    public RuleInterface<Integer> getFriendlyCountSelector() {
        return speciesData.friendlyCountSelector;
    }

    @Override
    public RuleInterface<Integer> getHostileCountSelector() {
        return speciesData.hostileCountSelector;
    }

    /**
     *
     * @return lower bound of friendly units adjacent to this unit
     */
    @Override
    public RuleInterface<Integer> getReproductionSelector() {
        return speciesData.reproductionSelector;
    }

    protected final void setHealth(int arg) {
        if (arg != health) {
            health = arg;
            setStateChanged();
        }
    }

    /**
     * @return unit's health points
     */
    @Override
    public final Integer getHealth() {
        return health;
    }

    /**
     * Increments the unit's health
     *
     * @param increment health increment
     */
    @Override
    public void incrementHealth(int increment) {
        healthIncrement += increment;
    }
    
    @Override
    protected void markAsNewborn(){
        state = State.DEAD;
        nextState = State.ALIVE;
    }
    

    @Override
    public String toString() {
        String ret = speciesData.name;
        ret += "@" + hashCode();
        ret += " " + state.toString();
        return ret;
    }

    @Override
    public Object clone() {
        return (LifeUnit) super.clone();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LifeUnit other = (LifeUnit) obj;
        if (this.speciesID != other.speciesID) {
            return false;
        }
        if (this.health != other.health) {
            return false;
        }
        if (this.playerID != other.playerID) {
            return false;
        }
        if (this.competitive != other.competitive) {
            return false;
        }
        if (this.state != other.state) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + this.speciesID;
        hash = 73 * hash + Objects.hashCode(this.state);
        hash = 73 * hash + this.health;
        hash = 73 * hash + this.playerID;
        hash = 73 * hash + (this.competitive ? 1 : 0);
        return hash;
    }
}

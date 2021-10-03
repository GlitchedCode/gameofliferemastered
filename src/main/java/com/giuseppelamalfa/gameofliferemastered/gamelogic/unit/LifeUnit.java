/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.GameLogicException;
import java.util.Set;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.RuleInterface;
import java.awt.image.BufferedImageOp;
import java.io.Serializable;

/**
 *
 * @author glitchedcode
 */
public class LifeUnit implements Unit, Serializable, Cloneable {

    public final int speciesID;

    public final SpeciesData speciesData;
    
    private State currentState = State.INVALID;
    private State nextTurnState = State.INVALID;

    private int health;

    private final int playerID;
    private boolean competitive = false;

    protected void initSpeciesData(SpeciesData data) {
        health = data.health;
        nextTurnState = data.initialState;
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
        int healthIncrement = 0;

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
            healthIncrement--;
        }

        if (healthIncrement != 0) {
            incrementHealth(healthIncrement);
        }
    }

    protected void endStep() {
        currentState.independentAction(this);

        if (health < 1) { // rule #5: hp
            nextTurnState = State.DEAD;
        } else {
            nextTurnState = currentState;
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

    /**
     * Updates the unit's status to the next turn
     */
    @Override
    public void update() {

        if (currentState != nextTurnState) {
            currentState.exit(this);
            currentState = nextTurnState;
            currentState.enter(this);
        }
        nextTurnState = State.INVALID;
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
    public boolean reproduce(int adjacencyPostition) {
        return currentState.reproductionModifier(isAlive(), adjacencyPostition);
    }

    protected boolean isHostile(Unit unit){
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
        boolean ret = currentState.attackModifier(isAlive(), adjacencyPosition);
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
        if (nextTurnState == State.INVALID) {
            throw new GameLogicException(this, "Invalid state.");
        }
        return nextTurnState;
    }

    protected final void setCurrentState(State arg) {
        currentState = arg;
    }
    
    /**
     * Get the unit's state in the current turn
     *
     * @return
     */
    @Override
    public final State getCurrentState() {
        return currentState;
    }

    /**
     * Get the unit's texture code
     *
     * @return
     */
    @Override
    public int getSpeciesID() {
        return speciesID;
    }
    
    @Override
    public SpeciesData getSpeciesData() {
        return speciesData;
    }

    @Override
    public int getBornSpeciesID() {
        return speciesID;
    }

    /**
     * @return set with friendly species
     */
    @Override
    public final Set<Integer> getFriendlySpecies() {
        return speciesData.friendlySpecies;
    }

    /**
     * @return set with hostile species
     */
    @Override
    public final Set<Integer> getHostileSpecies() {
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

    protected final void setHealth(int arg){
        health = arg;
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
        health = health + increment;
    }

    @Override
    public String toString() {
        String ret = speciesData.name;
        ret += "@" + hashCode();
        ret += " " + currentState.toString();
        return ret;
    }

    @Override
    public Object clone() {
        try {
            return (LifeUnit) super.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.Unit.clone()");
            return this;
        }
    }

    @Override
    public String getTextureCode() {
        return speciesData.textureCode;
    }

    @Override
    public BufferedImageOp getFilter() {
        return speciesData.filter;
    }

}

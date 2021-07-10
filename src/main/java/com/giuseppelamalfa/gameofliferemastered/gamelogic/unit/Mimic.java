/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

/**
 *
 * @author glitchedcode
 */
public class Mimic extends Unit {

    static public final int REPLICATION_COOLDOWN = 8;
    
    int turnsTillReplication = REPLICATION_COOLDOWN;
    
    public Mimic(SpeciesData data) {
        super(data);
    }

    public Mimic(SpeciesData data, Integer playerID) {
        super(data, playerID);
    }

    public Mimic(SpeciesData data, Integer playerID, Boolean competitive) {
        super(data, playerID, competitive);
    }

    @Override
    protected void endStep() {
        if (!healthChanged) { // rule #4: inactivity
            independentAction();
        }

        if (health < 1) { // rule #5: hp
            nextTurnState = State.DEAD;
        } else {
            nextTurnState = currentState;
        }
    }
    
    @Override
    public boolean attack(int adjacencyPosition, UnitInterface unit) {
        boolean ret = currentState.attackModifier(isAlive(), adjacencyPosition);
        if (ret) {
            unit.incrementHealth(-1);
        }
        return ret;
    }
}

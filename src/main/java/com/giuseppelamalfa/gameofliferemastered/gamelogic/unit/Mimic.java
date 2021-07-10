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
    int replicatedSpeciesID = -1;
    State replicatedState = State.INVALID;

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
        if ( !healthChanged ) { // rule #4: inactivity
            independentAction();
        }

        if ( health < 1 ) { // rule #5: hp
            nextTurnState = State.DEAD;
        }
        else {
            nextTurnState = currentState;
        }

        if ( turnsTillReplication > 0 ) {
            turnsTillReplication--;
        }
    }

    @Override
    public boolean attack(int adjacencyPosition, UnitInterface unit) {
        boolean ret = currentState.attackModifier(isAlive(), adjacencyPosition);
        if ( ret ) {
            unit.incrementHealth(-1);
            if ( turnsTillReplication == 0 ) {
                turnsTillReplication = REPLICATION_COOLDOWN;
                replicate(unit);
            }
        }
        return ret;
    }

    protected void replicate(UnitInterface unit) {
        replicatedSpeciesID = unit.getSpeciesID();
        SpeciesData data = SpeciesLoader.getSpeciesData(replicatedSpeciesID);
        SpeciesData myData = SpeciesLoader.getSpeciesData(speciesID);

        replicatedState = unit.getCurrentState();
        friendlySpecies = unit.getFriendlySpecies();
        hostileSpecies = unit.getHostileSpecies();
        friendlyCountSelector = unit.getFriendlyCountSelector();
        hostileCountSelector = unit.getHostileCountSelector();
        reproductionSelector = unit.getReproductionSelector();

        health = data.health - (health - myData.health);
        if ( health < 1 ) {
            health = 1;
        }
    }
}

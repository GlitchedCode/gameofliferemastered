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

    int turnsTillReplication = 0;
    
    UnitInterface replicationTarget;
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
    protected void boardStep(UnitInterface[] adjacentUnits) {
        int hostileCount = 0;
        int friendlyCount = 0;
        int healthIncrement = 0;

        for (int i = 0; i < 8; i++) // conto le unità ostili ed amichevoli
        {
            UnitInterface current = adjacentUnits[i];
            if (!current.isAlive()) {
                continue;
            }

            if (friendlySpecies.contains(current.getSpeciesID())) {
                friendlyCount++;
            }

            // additionally check if the adjacent cell can attack from 
            // their position relative to this cell
            boolean attacked = false;
            if (
                    (competitive & current.getPlayerID() != playerID) |
                    (
                        hostileSpecies.contains(current.getSpeciesID()) ^
                        (replicatedSpeciesID == -1 & current.getSpeciesID() != speciesID)
                    )) {
                attacked |= attack(i, current);
            }
            if (attacked) {
                hostileCount++;
            }
            
            if(replicatedSpeciesID == -1 & current.getSpeciesID() != speciesID){
                replicationTarget = current;
            }
        }

        // rule #1: population
        // rule #2: hostility
        boolean friendlyPenalty = !friendlyCountSelector.test(friendlyCount);
        boolean hostilePenalty = !hostileCountSelector.test(hostileCount);

        if (friendlyPenalty | hostilePenalty) {
            healthIncrement--;
        }

        if (healthIncrement != 0) {
            incrementHealth(healthIncrement);
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

    @Override
    protected void endStep() {
        if ( !healthChanged ) { // rule #4: inactivity
            independentAction();
        }
        
        if ( turnsTillReplication > 0 ) {
            turnsTillReplication--;
        } else if (replicationTarget != null) {
            replicate(replicationTarget);
            turnsTillReplication = REPLICATION_COOLDOWN;
        }

        if ( health < 1 ) { // rule #5: hp
            nextTurnState = State.DEAD;
        }
        else {
            nextTurnState = currentState;
        }
        
        replicationTarget = null;        
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
    
    @Override
    public String getTextureCode(){
        if(replicatedSpeciesID == -1)
            return SpeciesLoader.getSpeciesData(speciesID).textureCode;
        return SpeciesLoader.getSpeciesData(replicatedSpeciesID).textureCode;
    }
    
    @Override
    public int getSpeciesID(){
        if(replicatedSpeciesID != -1)
            return replicatedSpeciesID;
        return speciesID;
    }
}

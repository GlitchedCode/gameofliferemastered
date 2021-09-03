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
public class Mimic extends LifeUnit {

    static public final int REPLICATION_COOLDOWN = 8;

    private int turnsTillReplication = 0;

    private Unit replicationTarget;
    private int replicatedSpeciesID = -1;

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
    protected void boardStep(Unit[] adjacentUnits) {
        super.boardStep(adjacentUnits);
        for (int i = 0; i < 8; i++) // conto le unità ostili ed amichevoli
        {
            Unit current = adjacentUnits[i];
            if(replicatedSpeciesID != -1 | replicationTarget != null) {
                break;
            }
            if (current.getSpeciesID() != speciesID & current.isAlive()) {
                replicationTarget = current;
            }
        }
    }

    @Override
    public boolean attack(int adjacencyPosition, Unit unit) {
        boolean ret = currentState.attackModifier(isAlive(), adjacencyPosition);
        ret &= (competitive & unit.getPlayerID() != playerID)
                | (hostileSpecies.contains(unit.getSpeciesID())
                ^ (replicatedSpeciesID == -1 & unit.getSpeciesID() != speciesID));
        if (ret) {
            unit.incrementHealth(-1);
        }
        return ret;
    }

    @Override
    protected void endStep() {
        if (turnsTillReplication > 0) {
            turnsTillReplication--;
        } else if (replicationTarget != null) {
            replicate(replicationTarget);
            turnsTillReplication = REPLICATION_COOLDOWN;
        }
        super.endStep();
        replicationTarget = null;
    }

    protected void replicate(Unit unit) {
        replicatedSpeciesID = unit.getSpeciesID();
        SpeciesData data = SpeciesLoader.getSpeciesData(replicatedSpeciesID);
        SpeciesData myData = SpeciesLoader.getSpeciesData(speciesID);

        currentState = unit.getCurrentState();
        friendlySpecies = unit.getFriendlySpecies();
        hostileSpecies = unit.getHostileSpecies();
        friendlyCountSelector = unit.getFriendlyCountSelector();
        hostileCountSelector = unit.getHostileCountSelector();
        reproductionSelector = unit.getReproductionSelector();
        currentState = unit.getCurrentState();

        health = data.health - (health - myData.health);
        if (health < 1) {
            health = 1;
        }
    }

    @Override
    public String getTextureCode() {
        if (replicatedSpeciesID == -1) {
            return SpeciesLoader.getSpeciesData(speciesID).textureCode;
        }
        return SpeciesLoader.getSpeciesData(replicatedSpeciesID).textureCode;
    }

    @Override
    public int getSpeciesID() {
        if (replicatedSpeciesID != -1) {
            return replicatedSpeciesID;
        }
        return speciesID;
    }
}

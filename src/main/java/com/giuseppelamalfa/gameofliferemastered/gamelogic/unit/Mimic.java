/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.RuleInterface;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author glitchedcode
 */
public class Mimic extends LifeUnit {

    static public final int REPLICATION_COOLDOWN = 8;

    private int turnsTillReplication = 0;

    private transient Unit replicationTarget;
    private boolean replicated = false;
    
    private int replicatedID;
    private Set<Integer> friendlySpecies;
    private Set<Integer> hostileSpecies;
    
    private RuleInterface<Integer> friendlyCountSelector;
    private RuleInterface<Integer> hostileCountSelector;
    private RuleInterface<Integer> reproductionSelector;

    @Override
    protected void initSpeciesData(SpeciesData data){
        super.initSpeciesData(data);
        
        friendlySpecies =  data.friendlySpecies;
        hostileSpecies = data.hostileSpecies;
        
        friendlyCountSelector = data.friendlyCountSelector;
        hostileCountSelector = data.hostileCountSelector;
        reproductionSelector = data.friendlyCountSelector;
    }
    
    public Mimic(SpeciesData data) {
        super(data);
        initSpeciesData(data);
    }

    public Mimic(SpeciesData data, Integer playerID) {
        super(data, playerID);
        initSpeciesData(data);
    }

    public Mimic(SpeciesData data, Integer playerID, Boolean competitive) {
        super(data, playerID, competitive);
        initSpeciesData(data);
    }

    @Override
    protected void boardStep(Unit[] adjacentUnits) {
        super.boardStep(adjacentUnits);
        for (int i = 0; i < 8; i++)
        {
            Unit current = adjacentUnits[i];
            if(replicationTarget != null) {
                break;
            }
            if (current.getSpeciesID() != speciesID & current.isAlive()) {
                replicationTarget = current;
            }
        }
    }

    @Override
    public boolean attack(int adjacencyPosition, Unit unit) {
        boolean ret = getCurrentState().attackModifier(isAlive(), adjacencyPosition);
        ret &= (isCompetitive() & unit.getPlayerID() != getPlayerID())
                | (getHostileSpecies().contains(unit.getSpeciesID())
                ^ (!replicated & unit.getSpeciesID() != speciesID));
        if (ret) {
            unit.incrementHealth(-1);
        }
        return ret;
    }

    @Override
    public void update() {
        super.update();

        // Replication moved to update to avoid modifying grid state during survival/reproduction steps
        if (turnsTillReplication > 0) {
            turnsTillReplication--;
        } else if (replicationTarget != null) {
            replicate(replicationTarget);
            turnsTillReplication = REPLICATION_COOLDOWN;
        }
        replicationTarget = null;
    }

    protected void replicate(Unit unit) {
        SpeciesData otherData = unit.getSpeciesData();

        replicatedID = otherData.speciesID;
        friendlySpecies = unit.getFriendlySpecies();
        hostileSpecies = unit.getHostileSpecies();
        friendlyCountSelector = unit.getFriendlyCountSelector();
        hostileCountSelector = unit.getHostileCountSelector();
        reproductionSelector = unit.getReproductionSelector();
        setCurrentState(unit.getCurrentState());

        int newHealth = Math.max(getHealth() + (otherData.health - speciesData.health), 1);
        setHealth(newHealth);
        replicated = true;
    }

    @Override
    public int getSpeciesID() {
        if (!replicated) {
            return getActualSpeciesID();
        }
        return replicatedID;
    }
    
    /**
     * @return set with friendly species
     */
    @Override
    public final Set<Integer> getFriendlySpecies() {
        if(!replicated) {
            return super.getFriendlySpecies();
        }
        return friendlySpecies;
    }

    @Override
    public final Set<Integer> getHostileSpecies() {
        if(!replicated) {
            return super.getHostileSpecies();
        }
        return hostileSpecies;
    }

    @Override
    public RuleInterface<Integer> getFriendlyCountSelector() {
        if(!replicated) {
            return super.getFriendlyCountSelector();
        }
        return friendlyCountSelector;
    }

    @Override
    public RuleInterface<Integer> getHostileCountSelector() {
        if(!replicated) {
            return super.getHostileCountSelector();
        }
        return hostileCountSelector;
    }

    @Override
    public RuleInterface<Integer> getReproductionSelector() {
        if(!replicated) {
            return super.getReproductionSelector();
        }
        return reproductionSelector;
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
        final Mimic other = (Mimic) obj;
        if (this.turnsTillReplication != other.turnsTillReplication) {
            return false;
        }
        if (this.replicated != other.replicated) {
            return false;
        }
        if (this.replicatedID != other.replicatedID) {
            return false;
        }
        if (!Objects.equals(this.friendlySpecies, other.friendlySpecies)) {
            return false;
        }
        if (!Objects.equals(this.hostileSpecies, other.hostileSpecies)) {
            return false;
        }
        if (!Objects.equals(this.friendlyCountSelector, other.friendlyCountSelector)) {
            return false;
        }
        if (!Objects.equals(this.hostileCountSelector, other.hostileCountSelector)) {
            return false;
        }
        if (!Objects.equals(this.reproductionSelector, other.reproductionSelector)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + this.turnsTillReplication;
        hash = 29 * hash + (this.replicated ? 1 : 0);
        hash = 29 * hash + this.replicatedID;
        hash = 29 * hash + Objects.hashCode(this.friendlySpecies);
        hash = 29 * hash + Objects.hashCode(this.hostileSpecies);
        hash = 29 * hash + Objects.hashCode(this.friendlyCountSelector);
        hash = 29 * hash + Objects.hashCode(this.hostileCountSelector);
        hash = 29 * hash + Objects.hashCode(this.reproductionSelector);
        return hash;
    }

}

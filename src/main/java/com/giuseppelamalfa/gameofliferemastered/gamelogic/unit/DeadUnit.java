/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.RuleInterface;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.StubRule;
import java.awt.Color;
import java.io.Serializable;

class ReproductionCounter {

    int mimic_count = 0;
    int count = 0;
    HashMap<Integer, Integer> playerIDCounts = new HashMap<>();

    public ReproductionCounter() {
    }

    public ReproductionCounter(int startingPlayerID) {
        increment(startingPlayerID);
    }

    void increment(int playerID) {
        if (playerIDCounts.containsKey(playerID)) {
            playerIDCounts.put(playerID, playerIDCounts.get(playerID) + 1);
        } else {
            playerIDCounts.put(playerID, 1);
        }
        count++;
    }

    int getCount() {
        return count;
    }

    int getPlayerID() {
        int ret = -1;
        int max = 0;
        for (int id : playerIDCounts.keySet()) {
            if (max < playerIDCounts.get(id)) {
                max = playerIDCounts.get(id);
                ret = id;
            }
        }
        return ret;
    }
}

/**
 * @author glitchedcode
 */
public class DeadUnit extends Unit implements Serializable, Cloneable {

    // This function implements rule #3: reproduction
    @SuppressWarnings("unchecked")
    public final Unit getBornUnit(Unit[] adjacentUnits, SpeciesLoader speciesLoader) {
        // Contains how many units of a given species are adjacent.
        HashMap<Integer, ReproductionCounter> reproductionCounters = new HashMap<>();
        int candidate = -1;
        ReproductionCounter candidateCounter = new ReproductionCounter();
        reproductionCounters.put(-1, candidateCounter);

        // Contains the required amount of units of a given species to 
        // give birth to a new unit of that species.
        HashMap<Integer, RuleInterface<Integer>> reproductionSelectors = new HashMap<>();
        for (int i = 0; i < 8; i++) {
            Unit current = adjacentUnits[i];

            if (!current.isAlive()) // there is no adjacent unit in this direction
            {
                continue;
            }

            Integer oppositeDir = Unit.getOppositeDirection(i);
            if (!current.reproduce(oppositeDir)) // this unit doesn't reproduce from this direction
            {
                continue;
            }

            int species = current.getBornSpeciesID();
            // Add new species to the map as we find them in
            // nearby cells
            if (reproductionCounters.keySet().contains(species)) {
                reproductionCounters.get(species).increment(current.getPlayerID());
            } else {
                reproductionCounters.put(species, new ReproductionCounter(current.getPlayerID()));
                reproductionSelectors.put(species, speciesLoader.getSpeciesData(species).reproductionSelector);
            }
        }

        // Choose the candidate species to generate based on the reproduction
        // counters taken above and thei order in the Species enum
        for (int current : reproductionCounters.keySet()) {
            if (current == -1) {
                continue;
            }

            ReproductionCounter currentCounter = reproductionCounters.get(current);
            RuleInterface<Integer> selector = reproductionSelectors.get(current);

            if (currentCounter.getCount() == candidateCounter.getCount()) {
                if (current < candidate
                        & selector.test(currentCounter.getCount())) {
                    candidate = current;
                    candidateCounter = currentCounter;
                }
            } else if (currentCounter.getCount() > candidateCounter.getCount()
                    & selector.test(currentCounter.getCount())) {
                candidate = current;
                candidateCounter = currentCounter;
            }
        }

        if (candidate == -1) // neighboring units do not satisfy reproduction requirements
        {
            return null;
        }

        // If we have exactly as many units are necessary for reproduction,
        // we instantiate a new unit, mark it as a newborn and return it.
        Unit ret = speciesLoader.getNewUnit(candidate, candidateCounter.getPlayerID());
        ret.markAsNewborn();
        return ret;
    }

    /**
     *
     * @param adjacentUnits
     */
    @Override
    public void computeNextTurn(Unit[] adjacentUnits) {
    }

    @Override
    public SpeciesData getSpeciesData() {
        return null;
    }

    @Override
    public final boolean isStateChanged() {
        return false;
    }

    @Override
    public void update() {
    }

    @Override
    public boolean isAlive() {
        return false;
    }

    // unused overrides
    @Override
    public int getPlayerID() {
        return -1;
    }

    @Override
    public boolean isCompetitive() {
        return false;
    }

    @Override
    public void setCompetitive(boolean val) {
    }

    @Override
    public void kill() {
    }

    @Override
    public boolean reproduce(int a) {
        return false;
    }

    @Override
    public boolean attack(int a, Unit unit) {
        return false;
    }

    @Override
    public State getNextTurnState() {
        return null;
    }

    @Override
    public State getCurrentState() {
        return null;
    }

    @Override
    public int getActualSpeciesID() {
        return -1;
    }
    
    static Color color = new Color(0,0,0,0);
    
    @Override
    public Color getColor(){
        return color;
    }

    @Override
    public int getSpeciesID() {
        return -1;
    }

    @Override
    public int getBornSpeciesID() {
        return -1;
    }

    @Override
    public Set<Integer> getFriendlySpecies() {
        return new HashSet<>();
    }

    @Override
    public Set<Integer> getHostileSpecies() {
        return new HashSet<>();
    }

    static StubRule<Integer> stub = new StubRule<>();

    @Override
    public RuleInterface<Integer> getFriendlyCountSelector() {
        return stub;
    }

    @Override
    public RuleInterface<Integer> getHostileCountSelector() {
        return stub;
    }

    @Override
    public RuleInterface<Integer> getReproductionSelector() {
        return stub;
    }

    @Override
    public Integer getHealth() {
        return 0;
    }

    @Override
    public void incrementHealth(int increment) {
    }

    @Override
    protected void markAsNewborn() {
    }

    @Override
    public Object clone() {
        return (DeadUnit) super.clone();
    }

    @Override
    public String toString() {
        return "";
    }

}

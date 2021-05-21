/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.RuleInterface;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

class ReproductionCounter{
    int count = 0;
    HashMap<Integer, Integer> playerIDCounts = new HashMap<>();

    public ReproductionCounter() {    }
    
    public ReproductionCounter(int startingPlayerID){
        increment(startingPlayerID);
    }
        
    void increment(int playerID) { 
        if(playerIDCounts.containsKey(playerID))
            playerIDCounts.put(playerID, playerIDCounts.get(playerID) + 1);
        else
            playerIDCounts.put(playerID, 1);
        count++; 
    }
    int getCount() { return count; }
    int getPlayerID(){
        int ret = -1;
        int max = 0;
        for(int id : playerIDCounts.keySet())
            if(max < playerIDCounts.get(id))
            {
                max = playerIDCounts.get(id);
                ret = id;
            }
        return ret;
    }
}

/**
 * @author glitchedcode
 */
public class DeadUnit implements UnitInterface, Serializable, Cloneable
{
    private UnitInterface bornUnit = null;
    
    // This function implements rule #3: reproduction
    @Override
    @SuppressWarnings("unchecked")
    public void computeNextTurn(UnitInterface[] adjacentUnits)
    {        
        // Contains how many units of a given species are adjacent.
        HashMap<Species, ReproductionCounter> reproductionCounters = new HashMap<>();
        Species candidate = Species.INVALID;
        ReproductionCounter candidateCounter = new ReproductionCounter();
        reproductionCounters.put(Species.INVALID, candidateCounter);
        
        // Contains the required amount of units of a given species to 
        // give birth to a new unit of that species.
        HashMap<Species, RuleInterface<Integer> > reproductionSelectors = new HashMap<>();
        bornUnit = null;
        for (int i = 0; i < 8; i++)
        {
            UnitInterface current = adjacentUnits[i];
            
            if (current == null) // there is no adjacent unit in this direction
            {
                continue;
            }
            
            Integer oppositeDir = UnitInterface.getOppositeDirection(i);
            if (!current.reproduce(oppositeDir)) // this unity doesn't reproduce from this direction
            {
                continue;
            }
            
            Species species = current.getSpecies();
            // Add new species to the map as we find them in
            // nearby cells
            if(reproductionCounters.keySet().contains(species))
                reproductionCounters.get(species).increment(current.getPlayerID());
            else
            {
                reproductionCounters.put(species, new ReproductionCounter(current.getPlayerID()));
                reproductionSelectors.put(species, current.getReproductionSelector());
            }
        }
        
        // Choose the candidate species to generate based on the reproduction
        // counters taken above and thei order in the Species enum
        for (Species current : reproductionCounters.keySet())
        {
            if (current == Species.INVALID) continue;
            
            ReproductionCounter currentCounter = reproductionCounters.get(current);
            RuleInterface<Integer> selector = reproductionSelectors.get(current);
            
            if (currentCounter.getCount() == candidateCounter.getCount())
            {
                if (current.ordinal() < candidate.ordinal() &
                        selector.test(currentCounter.getCount()))
                {
                    candidate = current;
                    candidateCounter = currentCounter;
                }
            }
            else if (currentCounter.getCount() > candidateCounter.getCount() &
                    selector.test(currentCounter.getCount()))
            {
                candidate = current;
                candidateCounter = currentCounter;
            }
        }
        
        if (candidate == Species.INVALID)
        {
            // neighboring units do not satisfy reproduction requirements
            return;
        }
        
        // If we have exactly as many units are necessary for reproduction,
        // we instantiate a new unit and store it in bornUnit.
        Class<UnitInterface> unitClass;
        unitClass = candidate.getUnitClass();
        try
        {
            bornUnit = (UnitInterface) unitClass.getConstructor(Integer.class)
                    .newInstance(candidateCounter.getPlayerID());
        }
        catch (InvocationTargetException | InstantiationException | IllegalAccessException e)
        {
            Logger.getLogger(DeadUnit.class.getName()).log(Level.SEVERE, null, e);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(DeadUnit.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(DeadUnit.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    /**
     *
     * @return
     */
    public final UnitInterface getBornUnit()
    {
        return bornUnit;
    }
    
    @Override
    public void update()
    {
        bornUnit = null;
    }
    
    @Override
    public boolean isAlive()
    {
        return false;
    }
    
    // overrides

    @Override
    public int                      getPlayerID() { return -1; }
    @Override
    public boolean                  isCompetitive() { return false; }
    @Override
    public void                     setCompetitive(boolean val) {}
    @Override
    public boolean              reproduce(Integer a) {return false;}
    @Override
    public boolean              attack(Integer a) {return false;}
    @Override
    public void                 independentAction() {}
    @Override
    public State                getNextTurnState() {return State.INVALID; }
    @Override
    public State                getCurrentState() { return State.INVALID; }
    @Override
    public Species              getSpecies() { return Species.INVALID; }
    @Override
    public Set<Species>         getFriendlySpecies() { return new HashSet<>(); }
    @Override
    public Set<Species>         getHostileSpecies() { return new HashSet<>(); }
    @Override
    public RuleInterface<Integer>    getReproductionSelector() { return null; }
    @Override
    public Integer              getHealth() { return 0; }
    @Override
    public void                 incrementHealth(int increment) { }

    @Override
    public Object clone() {
        try {
            return (DeadUnit) super.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.DeadUnit.clone() failed idk");
            return this;
        }
    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.UnitInterface;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.Unit;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import com.giuseppelamalfa.gameofliferemastered.utils.Rule;

/**
 *
 * @author glitchedcode
 */
public class DeadUnit implements UnitInterface
{
    private UnitInterface bornUnit = null;
    
    // This function implements rule #3: reproduction
    @Override
    @SuppressWarnings("unchecked")
    public void computeNextTurn(UnitInterface[] adjacentUnits)
    {        
        // Contains how many units of a given species are adjacent.
        HashMap<Species, Integer> reproductionCounters = new HashMap<>();
        // Contains the required amount of units of a given species to 
        // give birth to a new unit of that species.
        HashMap<Species, Rule<Integer> > reproductionSelectors = new HashMap<>();
        reproductionCounters.put(Species.INVALID, 0);
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
            {
                reproductionCounters.put(species, 
                        reproductionCounters.get(species) + 1);
            }
            else
            {
                reproductionCounters.put(species, 1);
                reproductionSelectors.put(species, current.getReproductionSelector());
            }
        }
        
        Species candidate = Species.INVALID;
        int candidateCount = 0;

        // Choose the candidate species to generate based on the reproduction
        // counters taken above and thei order in the Species enum
        for (Species current : reproductionCounters.keySet())
        {
            if (current == Species.INVALID) continue;
            
            int currentCount = reproductionCounters.get(current);
            Rule<Integer> selector = reproductionSelectors.get(current);
            
            if (currentCount == candidateCount)
            {
                if (current.ordinal() < candidate.ordinal() &
                        selector.test(currentCount))
                {
                    candidate = current;
                    candidateCount = currentCount;
                }
            }
            else if (currentCount > candidateCount &
                    selector.test(currentCount))
            {
                candidate = current;
                candidateCount = currentCount;
            }
        }
        
        if (candidate == Species.INVALID)
        {
            // neighboring units do not satisfy reproduction requirements
            return;
        }
        
        // If we have exactly as many units are necessary for reproduction,
        // we instantiate a new unit and store it in bornUnit.
        Class<Unit> unitClass;
        unitClass = candidate.getUnitClass();
        try
        {
            bornUnit = (UnitInterface) unitClass.getConstructors()[0].newInstance();
        }
        catch (InvocationTargetException | InstantiationException | IllegalAccessException e)
        {
            System.out.println(e);
            bornUnit = null;
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
    
    // overrides
    
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
    public Rule<Integer>    getReproductionSelector() { return null; }
    @Override
    public Integer              getHealth() { return 0; }
    @Override
    public void                 incrementHealth(Integer increment) { }

}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 *
 * @author glitchedcode
 */
public class DeadUnit extends Unit 
{
    private UnitInterface bornUnit;
    
    public DeadUnit()
    {
        super();
        species = Species.INVALID;
        currentState = State.DEAD;
        nextTurnState = State.INVALID;
        minimumFriendly = -1;
        health = 0;
        
        bornUnit = null;
    }
    
    // This function implements rule #3: reproduction
    @Override
    @SuppressWarnings("unchecked")
    protected void boardStep(UnitInterface[] adjacentUnits)
    {        
        // Contains how many units of a given species are adjacent.
        HashMap<Species, Integer> reproductionCounters = new HashMap<>();
        // Contains the required amount of units of a given species to 
        // give birth to a new unit of that species.
        HashMap<Species, Integer> reproductionValues = new HashMap<>();
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
            
            species = current.getSpecies();
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
                reproductionValues.put(species, current.getMinimumFriendlyUnits() + 1);
            }
        }
        
        Species candidate = Species.INVALID;
        
        // Choose the candidate species to generate based on the reproduction
        // counters taken above and thei order in the Species enum
        for (Species current : reproductionCounters.keySet())
        {
            if (current == Species.INVALID) continue;
            
            int currentCount = reproductionCounters.get(current);
            int candidateCount = reproductionCounters.get(candidate);
            int reproductionRequisite = reproductionValues.get(current);
            
            if (currentCount == candidateCount)
            {
                if (current.ordinal() < candidate.ordinal() &
                        currentCount == reproductionRequisite)
                {
                    candidate = current;
                }
            }
            else if (currentCount > candidateCount &
                    currentCount == reproductionRequisite)
            {
                candidate = current;
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
    
    @Override
    protected void endStep()
    {
        // nothing
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
    public boolean reproduce(Integer adjacencyPosition)
    {
        return false;
    }
}
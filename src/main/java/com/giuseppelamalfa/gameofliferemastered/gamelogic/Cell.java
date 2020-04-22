/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.Unit;
import com.giuseppelamalfa.gameofliferemastered.utils.IntegerRangeSelector;
import com.giuseppelamalfa.gameofliferemastered.utils.IntegerSetSelector;

/**
 *
 * @author glitchedcode
 */
public class Cell extends Unit {
    
    public Cell()
    {
        super();
        species = Species.CELL;
        health = 1;
        friendlySpecies.add(species);
        
        friendlyCountSelector = new IntegerRangeSelector(2, 3);
        hostileCountSelector = new IntegerRangeSelector(0, 9);
        
        var selector = new IntegerSetSelector();
        selector.add(3);
        reproductionSelector = selector;
    }
    
}

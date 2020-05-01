/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.RuleInterface;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.IntegerRangeRule;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.IntegerSetRule;

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
        
        friendlyCountSelector = new IntegerRangeRule(2, 3);
        hostileCountSelector = new IntegerRangeRule(0, 9);
        
        IntegerSetRule selector = new IntegerSetRule();
        selector.add(3);
        reproductionSelector = selector;
    }
    
}

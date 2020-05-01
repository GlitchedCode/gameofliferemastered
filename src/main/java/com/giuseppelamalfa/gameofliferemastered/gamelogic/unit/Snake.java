/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.IntegerRangeRule;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.rule.IntegerSetRule;

/**
 *
 * @author glitchedcode
 */
public class Snake extends Unit
{
    public Snake()
    {
        super();
        
        species = Species.SNAKE;
        health = 1;
        friendlySpecies.add(species);
        
        friendlyCountSelector = new IntegerRangeRule(2,5);
        IntegerSetRule selector = new IntegerSetRule();
        selector.add(1);
        hostileCountSelector = new IntegerRangeRule(0, 9);
        
        selector = new IntegerSetRule();
        selector.add(4);
        selector.add(5);
        reproductionSelector = selector;
    }
}

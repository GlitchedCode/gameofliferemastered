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
        init();
    }
    
    public Snake(int playerID)
    {
        super(playerID);
        init();
    }
    
    private void init() {
        species = Species.SNAKE;
        health = 7;
        friendlySpecies.add(species);
        
        friendlyCountSelector = new IntegerRangeRule(1,5);
        IntegerSetRule selector = new IntegerSetRule();
        selector.add(1);
        hostileCountSelector = new IntegerRangeRule(0, 9);
        
        selector = new IntegerSetRule();
        selector.add(3);
        //selector.add(6);
        reproductionSelector = selector;
        
        nextTurnState = State.ALIVE_AGING;
    }
}

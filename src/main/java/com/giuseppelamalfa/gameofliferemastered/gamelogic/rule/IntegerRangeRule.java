/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.rule;

/**
 *
 * @author glitchedcode
 */
public class IntegerRangeRule implements RuleInterface<Integer>
{
    private final Integer min;
    private final Integer max;
    
    public IntegerRangeRule(Integer min, Integer max)
    {
        this.min = min;
        this.max = max;
    }
    
    @Override
    public boolean test(Integer value)
    {
        return min <= value & value <= max;
    }
}

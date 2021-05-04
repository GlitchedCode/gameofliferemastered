/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.rule;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author glitchedcode
 */
public class IntegerSetRule implements RuleInterface<Integer>, Serializable
{
    public final Set<Integer> acceptedValues = new HashSet<>();
    
    public IntegerSetRule(){}
    
    @Override
    public boolean test(Integer value)
    {
        return acceptedValues.contains(value);
    }
    
    public void add(Integer value)
    {
        acceptedValues.add(value);
    }
}

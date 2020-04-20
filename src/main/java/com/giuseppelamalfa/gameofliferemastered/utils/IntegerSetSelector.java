/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.utils;

import java.util.Set;

/**
 *
 * @author glitchedcode
 */
public class IntegerSetSelector implements Selector<Integer>
{
    public final Set<Integer> acceptedValues;
    
    public IntegerSetSelector(Set<Integer> set)
    {
        acceptedValues = set;
    }
    
    
    @Override
    public boolean test(Integer value)
    {
        return acceptedValues.contains(value);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.rule;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author glitchedcode
 */
public class IntegerSetRule implements RuleInterface<Integer>, Serializable {

    private final Set<Integer> acceptedValues;

    public IntegerSetRule(Collection<Integer> values) {
        acceptedValues = new HashSet<>(values);
    }
    
    public IntegerSetRule(Integer... values){
        acceptedValues = new HashSet<>();
        for(Integer val : values){
            acceptedValues.add(val);
        }
    }

    public IntegerSetRule() {
        acceptedValues = new HashSet<>();
    }

    @Override
    public boolean test(Integer value) {
        return acceptedValues.contains(value);
    }

    public void add(int value) {
        acceptedValues.add(value);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.rule;

/**
 *
 * @author glitchedcode
 * @param <T> Type to be tested
 */
public interface RuleInterface<T> {

    public boolean test(T value);
    
    public String getConstructionArgs();
}

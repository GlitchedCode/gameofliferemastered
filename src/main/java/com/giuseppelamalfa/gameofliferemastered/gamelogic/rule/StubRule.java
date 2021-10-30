/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.rule;

import java.io.Serializable;

/**
 *
 * @author glitchedcode
 */
public class StubRule<T> implements RuleInterface<T>, Serializable {

    boolean value;
    
    public StubRule(boolean value){
        this.value = value;
    }
    
    public StubRule(){
        this(false);
    }
    
    @Override
    public boolean test(T value) {
        return this.value;
    }
}

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
public class StubRule<T> implements RuleInterface<T> {

    @Override
    public boolean test(T value) {
        return false;
    }
}

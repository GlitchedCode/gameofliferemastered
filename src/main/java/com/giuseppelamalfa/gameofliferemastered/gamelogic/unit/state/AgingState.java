/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.state;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.UnitInterface;

/**
 *
 * @author glitchedcode
 */
public class AgingState implements StateInterface {

    @Override
    public void enter(UnitInterface unit) {
    }

    @Override
    public void exit(UnitInterface unit) {
    }

    @Override
    public boolean attackModifier(boolean speciesResult, Integer adjacencyPosition) {
        return speciesResult;
    }

    @Override
    public boolean reproductionModifier(boolean speciesResult, Integer adjacencyPosition) {
        return speciesResult;
    }

    @Override
    public void independentAction(UnitInterface unit) {
        unit.incrementHealth(-1);
    }

}

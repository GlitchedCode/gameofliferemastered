/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import java.io.Serializable;
import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.Unit;

/**
 *
 * @author glitchedcode
 */
public enum State implements Serializable {
    INVALID,
    DEAD,
    ALIVE,
    ALIVE_AGING {
        @Override
        public void independentAction(Unit unit) {
            unit.incrementHealth(-1);
        }
    };

    public void enter(Unit unit) {
    }

    public void exit(Unit unit) {
    }

    public boolean attackModifier(boolean speciesResult, Integer adjacencyPosition) {
        return speciesResult;
    }

    public boolean reproductionModifier(boolean speciesResult, Integer adjacencyPosition) {
        return speciesResult;
    }

    public void independentAction(Unit unit) {
    }

}

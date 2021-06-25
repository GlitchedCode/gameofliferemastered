/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.unit;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.UnitInterface;
import java.io.Serializable;

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
        public void independentAction(UnitInterface unit) {
            unit.incrementHealth(-1);
        }
    };

    public void enter(UnitInterface unit) {
    }

    public void exit(UnitInterface unit) {
    }

    public boolean attackModifier(boolean speciesResult, Integer adjacencyPosition) {
        return speciesResult;
    }

    public boolean reproductionModifier(boolean speciesResult, Integer adjacencyPosition) {
        return speciesResult;
    }

    public void independentAction(UnitInterface unit) {
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.state;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.UnitInterface;
import java.io.Serializable;

/**
 *
 * @author glitchedcode
 */
public enum State implements Serializable {
    INVALID(null), DEAD(null), ALIVE(null), ALIVE_AGING(new AgingStateInterface());
    private final StateInterface stateImplementationInterface;

    private State(StateInterface iface) {
        stateImplementationInterface = iface;
    }

    public final void enter(UnitInterface unit) {
        if (stateImplementationInterface != null) {
            stateImplementationInterface.enter(unit);
        }
    }

    public final void exit(UnitInterface unit) {
        if (stateImplementationInterface != null) {
            stateImplementationInterface.exit(unit);
        }
    }

    public final boolean attackModifier(boolean speciesResult, Integer adjacencyPosition) {
        if (stateImplementationInterface != null) {
            return stateImplementationInterface.attackModifier(speciesResult, adjacencyPosition);
        } else {
            return speciesResult;
        }
    }

    public final boolean reproductionModifier(boolean speciesResult, Integer adjacencyPosition) {
        if (stateImplementationInterface != null) {
            return stateImplementationInterface.reproductionModifier(speciesResult, adjacencyPosition);
        } else {
            return speciesResult;
        }
    }

    public final void independentAction(UnitInterface unit) {
        if (stateImplementationInterface != null) {
            stateImplementationInterface.independentAction(unit);
        }
    }
    
}

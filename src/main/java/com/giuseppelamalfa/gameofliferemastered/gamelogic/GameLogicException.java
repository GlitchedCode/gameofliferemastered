/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.LifeUnit;

/**
 *
 * @author glitchedcode
 */
public class GameLogicException extends Exception {

    private final LifeUnit invalidUnit;
    private final String reason;

    public GameLogicException(LifeUnit invalidUnit, String reason) {
        this.invalidUnit = invalidUnit;
        this.reason = reason;
    }

    @Override
    public String toString() {
        String ret = "InvalidStateException: ";
        ret += invalidUnit.getClass().toString() + " ";
        ret += invalidUnit.getHealth().toString() + "HP ";
        ret += " State: " + invalidUnit.getCurrentState().toString();
        ret += reason + "\n";
        return ret;
    }
}

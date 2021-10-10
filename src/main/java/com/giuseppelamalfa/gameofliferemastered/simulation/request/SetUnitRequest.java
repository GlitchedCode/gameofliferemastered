/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.simulation.request;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.Unit;

/**
 *
 * @author glitchedcode
 */
public class SetUnitRequest extends Request {

    public final Unit unit;
    public final int row;
    public final int col;

    public SetUnitRequest(int row, int col, Unit unit) {
        super(RequestType.SET_UNIT);
        this.row = row;
        this.col = col;
        this.unit = unit;
    }
}

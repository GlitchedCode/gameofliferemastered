/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.requests;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.UnitInterface;

/**
 *
 * @author glitchedcode
 */
public class SetUnitRequest extends Request {
    public UnitInterface unit;
    public int row;
    public int col;
    
    public SetUnitRequest(int row, int col, UnitInterface unit)
    {
        type = RequestType.SET_UNIT;
        this.row = row;
        this.col = col;
        this.unit = unit;
    }
}

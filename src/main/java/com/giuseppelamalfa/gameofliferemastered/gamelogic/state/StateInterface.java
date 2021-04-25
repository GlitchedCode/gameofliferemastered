/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.state;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.unit.UnitInterface;

/**
 *
 * @author glitchedcode
 */
public interface StateInterface
{
    public void         enter(UnitInterface unit);
    public void         exit(UnitInterface unit);
    
    public boolean      attackModifier(boolean speciesResult, Integer adjacencyPosition);
    public boolean      reproductionModifier(boolean speciesResult, Integer adjacencyPosition);
    public void         independentAction(UnitInterface unit);
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.grid;

import java.lang.reflect.Type;

/**
 *
 * @author glitchedcode
 */
public enum GameMode{
    SANDBOX(Grid.class),
    COMPETITIVE(CompetitiveGrid.class);
        
    public final Type type;
    GameMode(Type type) { this.type = type; } 
}

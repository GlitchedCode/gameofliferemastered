/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic;

/**
 *
 * @author glitchedcode
 */
public class Cell extends Unit {
    
    public Cell()
    {
        super();
        species = Species.CELL;
        currentState = State.INVALID;
        nextTurnState = State.ALIVE;
        health = 1;
        friendlySpecies.add(species);
        minimumFriendly = 2;
        maximumFriendly = 3;
    }
    
}

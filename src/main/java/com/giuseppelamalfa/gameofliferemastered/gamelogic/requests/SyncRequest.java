/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.requests;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.Grid;

/**
 *
 * @author glitchedcode
 */
public class SyncRequest extends Request {
    public Grid grid = null;
    
    public SyncRequest() {
        type = RequestType.SYNC;    
    }    
    
    public SyncRequest(Grid grid) {
        type = RequestType.SYNC;
        this.grid = grid;
    }    
}

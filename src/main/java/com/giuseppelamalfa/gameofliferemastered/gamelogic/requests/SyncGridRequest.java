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
public class SyncGridRequest extends Request {
    public Grid grid = null;
    
    public SyncGridRequest() {
        type = RequestType.SYNC_GRID;    
    }    
    
    public SyncGridRequest(Grid grid) {
        type = RequestType.SYNC_GRID;
        this.grid = grid;
    }    
}

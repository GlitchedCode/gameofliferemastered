/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.requests;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.grid.Grid;

/**
 *
 * @author glitchedcode
 */
public class SyncGridRequest extends Request {

    public final Grid grid;
    public final boolean skipTurn;

    public SyncGridRequest() {
        type = RequestType.SYNC_GRID;
        grid = null;
        skipTurn = false;
    }

    public SyncGridRequest(Grid grid) {
        type = RequestType.SYNC_GRID;
        this.grid = grid;
        skipTurn = false;
    }
    
    public SyncGridRequest(Grid grid, boolean skip) {
        type = RequestType.SYNC_GRID;
        this.grid = grid;
        skipTurn = skip;
    }
}

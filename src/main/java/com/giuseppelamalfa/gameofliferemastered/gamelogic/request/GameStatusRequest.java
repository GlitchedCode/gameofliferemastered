/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.request;

/**
 *
 * @author glitchedcode
 */
public class GameStatusRequest extends Request {

    public boolean running;
    public String status;

    public GameStatusRequest(boolean val) {
        super(RequestType.GAME_STATUS);
        running = val;
    }

    public GameStatusRequest(boolean val, String status) {
        super(RequestType.GAME_STATUS);
        running = val;
        this.status = status;
    }
}

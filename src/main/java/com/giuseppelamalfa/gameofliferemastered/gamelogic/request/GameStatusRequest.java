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

    public final boolean running;
    public final String status;

    public GameStatusRequest(boolean running) {
        super(RequestType.GAME_STATUS);
        this.running = running;
        status = null;
    }

    public GameStatusRequest(boolean running, String status) {
        super(RequestType.GAME_STATUS);
        this.running = running;
        this.status = status;
    }
}

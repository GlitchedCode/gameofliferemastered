/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.requests;

/**
 *
 * @author glitchedcode
 */
public class GameStatusRequest extends Request
{

    public boolean running;
    public String status;

    public GameStatusRequest(boolean val)
    {
        super(RequestType.PAUSE);
        running = val;
    }

    public GameStatusRequest(boolean val, String status)
    {
        super(RequestType.PAUSE);
            running = val;
        this.status = status;
    }
}

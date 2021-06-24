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
public class DisconnectRequest extends Request
{

    public String message;

    public DisconnectRequest(String message)
    {
        super(RequestType.DISCONNECT);
        this.message = message;
    }
}

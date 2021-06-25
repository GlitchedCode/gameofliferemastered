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
public class LogMessageRequest extends Request
{

    public final String message;

    public LogMessageRequest(String msg)
    {
        super(RequestType.LOG_MESSAGE);
        message = msg;
    }
}

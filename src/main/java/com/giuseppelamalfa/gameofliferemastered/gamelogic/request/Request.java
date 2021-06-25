/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.request;

import java.io.Serializable;

/**
 *
 * @author glitchedcode
 */
public abstract class Request implements Serializable {


    public final RequestType type;
    
    public Request(){
        type = RequestType.INVALID;
    }
    
    public Request(RequestType t){
        type = t;
    }
    
    public RequestType getType() throws InvalidRequestException {
        if (type == RequestType.INVALID) {
            throw new InvalidRequestException();
        }
        return type;
    }
}

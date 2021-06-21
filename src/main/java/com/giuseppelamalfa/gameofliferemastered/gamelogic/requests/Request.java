/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.requests;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 *
 * @author glitchedcode
 */
public abstract class Request implements Serializable {


    protected RequestType type = RequestType.INVALID;
    
    public RequestType getType() throws InvalidRequestException {
        if (type == RequestType.INVALID) {
            throw new InvalidRequestException();
        }
        return type;
    }
}

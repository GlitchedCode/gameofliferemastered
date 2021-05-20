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
    
    public enum RequestType{
        INVALID(null),
        LOG_MESSAGE(LogMessageRequest.class),
        SYNC_GRID(SyncGridRequest.class),
        UPDATE_PLAYER_DATA(UpdatePlayerDataRequest.class),
        DISCONNECT(DisconnectRequest.class),
        PAUSE(PauseRequest.class),
        SET_UNIT(SetUnitRequest.class);
        
        public final Type requestObjectType;
        
        private RequestType(Type type)
        {
            requestObjectType = type;
        }
    }
    
    protected RequestType type = RequestType.INVALID;;
    
    public RequestType getType() throws InvalidRequestException {
        if(type == RequestType.INVALID)
            throw new InvalidRequestException();
        return type; 
    }
}
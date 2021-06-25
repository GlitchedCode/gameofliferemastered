/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.request;

import java.lang.reflect.Type;

/**
 *
 * @author glitchedcode
 */
public enum RequestType {
    
    // procedure prototype:
    // void procedureName(Request req, Integer clientID)
    INVALID(null, ""),
    LOG_MESSAGE(LogMessageRequest.class, "handleLogMessageRequest"),
    SYNC_SPECIES_DATA(SyncSpeciesDataRequest.class, "handleSyncSpeciesDataRequest"),
    SYNC_GRID(SyncGridRequest.class, "handleSyncGridRequest"),
    UPDATE_PLAYER_DATA(UpdatePlayerDataRequest.class, "handleUpdatePlayerDataRequest"),
    DISCONNECT(DisconnectRequest.class, "handleDisconnectRequest"),
    PAUSE(GameStatusRequest.class, "handleGameStatusRequest"),
    SET_UNIT(SetUnitRequest.class, "handleSetUnitRequest");

    public final Type requestObjectType;
    public final String procedureName;

    private RequestType(Type type, String name) {
        requestObjectType = type;
        procedureName = name;
    }

}

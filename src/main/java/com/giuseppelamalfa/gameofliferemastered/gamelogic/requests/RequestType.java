/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.requests;

import java.lang.reflect.Type;

/**
 *
 * @author glitchedcode
 */
public enum RequestType {
    INVALID(null),
    LOG_MESSAGE(LogMessageRequest.class),
    SYNC_SPECIES_DATA(SyncSpeciesDataRequest.class),
    SYNC_GRID(SyncGridRequest.class),
    UPDATE_PLAYER_DATA(UpdatePlayerDataRequest.class),
    DISCONNECT(DisconnectRequest.class),
    PAUSE(GameStatusRequest.class),
    SET_UNIT(SetUnitRequest.class);

    public final Type requestObjectType;

    private RequestType(Type type) {
        requestObjectType = type;
    }

}

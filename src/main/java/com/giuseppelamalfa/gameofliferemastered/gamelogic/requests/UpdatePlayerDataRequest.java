/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.requests;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.PlayerData;

/**
 *
 * @author glitchedcode
 */
public class UpdatePlayerDataRequest extends Request {

    public final PlayerData playerData;
    public final boolean connected;
    public final boolean updateLocal;

    public UpdatePlayerDataRequest(PlayerData data, boolean connected) {
        type = RequestType.UPDATE_PLAYER_DATA;
        playerData = data;
        this.connected = connected;
        updateLocal = false;
    }

    public UpdatePlayerDataRequest(PlayerData data, boolean connected, boolean updateLocal) {
        type = RequestType.UPDATE_PLAYER_DATA;
        playerData = data;
        this.connected = connected;
        this.updateLocal = updateLocal;
    }
}

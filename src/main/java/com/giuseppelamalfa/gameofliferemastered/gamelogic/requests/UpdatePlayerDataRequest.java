/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.requests;

import com.giuseppelamalfa.gameofliferemastered.utils.PlayerData;

/**
 *
 * @author glitchedcode
 */
public class UpdatePlayerDataRequest extends Request {
    public PlayerData playerData;

    public UpdatePlayerDataRequest(){
        type = RequestType.UPDATE_PLAYER_DATA;
    }
}

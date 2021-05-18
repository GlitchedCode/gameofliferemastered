/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.requests;

import com.giuseppelamalfa.gameofliferemastered.gamelogic.PlayerData;
import java.util.HashMap;

/**
 *
 * @author glitchedcode
 */
public class SyncPlayerMapRequest extends Request {
    
    public final HashMap<Integer, PlayerData> playerMap;
    
    public SyncPlayerMapRequest(HashMap<Integer, PlayerData> map){
        type = RequestType.SYNC_PLAYER_MAP;
        playerMap = map;
    }
}

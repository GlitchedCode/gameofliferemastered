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
public class SyncSpeciesDataRequest extends Request {
    public final String jsonString;
    
    public SyncSpeciesDataRequest(String jsonString){
        type = RequestType.SYNC_SPECIES_DATA;
        this.jsonString = jsonString;
    }
}

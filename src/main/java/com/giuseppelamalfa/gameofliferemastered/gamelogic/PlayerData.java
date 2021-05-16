/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic;

import java.awt.Color;

/**
 *
 * @author glitchedcode
 */
public class PlayerData {
    
    public enum TeamColor{
        NONE(new Color(0,0,0,0)),
        YELLOW(new Color(255,255,108)),
        ORANGE(new Color(252,170,103)),
        RED(new Color(194,1,20)),
        PURPLE(new Color(57,0,153)),
        PINK(new Color(204,75,194)),
        BLUE(new Color(1,111,185)),
        GREEN(new Color(33,209,159)),
        DARKBLUE(new Color(10,16,69));
        
        Color color;
        
        TeamColor(Color val) { color = val; }
        
        public Color getAWTColor() { return color; }
        
    }
    
    public int playerID;
    public String playerName;
    public TeamColor color;
    
    public PlayerData(int playerID, String playerName, TeamColor color)
    {
        this.playerID = playerID;
        this.playerName = playerName;
        this.color = color;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic;

import java.awt.Color;
import java.io.Serializable;

/**
 *
 * @author glitchedcode
 */
public class PlayerData implements Serializable {

    public enum TeamColor implements Serializable {
        NONE(new Color(0, 0, 0, 0), new Color(255, 255, 255)),
        YELLOW(new Color(255, 255, 108), new Color(0, 0, 0)),
        ORANGE(new Color(252, 170, 103), new Color(0, 0, 0)),
        RED(new Color(194, 1, 20), new Color(255, 255, 255)),
        PURPLE(new Color(57, 0, 153), new Color(255, 255, 255)),
        PINK(new Color(204, 75, 194), new Color(0, 0, 0)),
        BLUE(new Color(1, 111, 185), new Color(255, 255, 255)),
        GREEN(new Color(33, 209, 159), new Color(0, 0, 0)),
        DARKBLUE(new Color(10, 16, 69), new Color(255, 255, 255));

        Color color;
        Color textColor;

        TeamColor(Color val, Color text) {
            color = val;
            textColor = text;
        }

        public Color getMainAWTColor() {
            return color;
        }

        public Color getTextAWTColor() {
            return textColor;
        }
    }

    public int ID = -1;
    public String name = null;
    public TeamColor color = TeamColor.NONE;
    public int score = 0;

    public PlayerData(String playerName, TeamColor color) {
        this.name = playerName;
        this.color = color;
    }

    public PlayerData(String playerName) {
        this.name = playerName;
    }

    public PlayerData() {
    }

    public PlayerData(PlayerData other) {
        ID = other.ID;
        name = other.name;
        color = other.color;
        score = other.score;
    }
}

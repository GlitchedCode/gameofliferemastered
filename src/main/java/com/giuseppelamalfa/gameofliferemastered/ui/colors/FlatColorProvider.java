/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.ui.colors;

import java.awt.Color;
import org.json.JSONObject;

/**
 *
 * @author glitchedcode
 */
public class FlatColorProvider extends ColorProvider {

    private final Color color;

    public FlatColorProvider(JSONObject obj) {
        Color col;
        
        try{
            col = new Color(Integer.decode(obj.getString("color")));
        } catch (Exception e) {
            try{
                col = new Color(obj.getInt("col"));
            }catch (Exception e2){
                System.out.println("error while reading color: " + e.toString());
                col = Color.BLACK;
            }
        }
        
        color = col;
    }
    
    public FlatColorProvider(Color arg) {
        color = arg;
    }

    @Override
    public Color mainColor() {
        return color;
    }

    @Override
    public Color currentColor() {
        return color;
    }

    @Override
    public void update(double delta) {
    }

    @Override
    public Type getType() {
        return Type.FLAT;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject ret = super.toJSONObject();
        ret.put("color", Integer.toUnsignedString(color.getRGB()));
        return ret;
    }
}

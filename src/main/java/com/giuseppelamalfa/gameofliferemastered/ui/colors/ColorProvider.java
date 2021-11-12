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
public abstract class ColorProvider {

    public enum Type {
        FLAT(FlatColorProvider.class),
        ORDERED_SET(OrderedSetColorProvider.class),
        RANDOM_SET(RandomSetColorProvider.class),
        ANIMATED_HSB(AnimatedHSBProvider.class);

        Type(Class<?> clazz) {
            this.clazz = clazz;
        }

        public ColorProvider constructFromJSON(JSONObject arg) throws Exception {
            return (ColorProvider) clazz.getConstructor(JSONObject.class).newInstance(arg);
        }

        public final Class<?> clazz;
    }

    public abstract Color mainColor();

    public abstract Color currentColor();

    public abstract void update(double delta);

    public abstract Type getType();

    public JSONObject toJSONObject() {
        JSONObject ret = new JSONObject();
        ret.put("type", getType());
        return ret;
    }
}

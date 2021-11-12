/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.ui.colors;

import java.awt.Color;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import org.json.JSONObject;

/**
 *
 * @author glitchedcode
 */
public class RandomSetColorProvider extends OrderedSetColorProvider {

    public RandomSetColorProvider(JSONObject obj) {
        super(obj);
    }

    public RandomSetColorProvider(Collection<Color> colors) {
        super(colors);
    }

    @Override
    public Type getType() {
        return Type.RANDOM_SET;
    }

    @Override
    protected void advanceIndex() {
        setCurrentIndex(ThreadLocalRandom.current().nextInt());
    }
}

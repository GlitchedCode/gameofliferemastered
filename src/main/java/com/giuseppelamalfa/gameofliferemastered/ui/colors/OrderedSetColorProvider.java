/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.ui.colors;

import com.giuseppelamalfa.gameofliferemastered.ui.clock.PollClock;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author glitchedcode
 */
public class OrderedSetColorProvider extends ColorProvider {

    private final ArrayList<Color> colors;

    private PollClock clock = new PollClock(1f);
    private int currentIndex = 0;

    public OrderedSetColorProvider(JSONObject obj) {
        colors = new ArrayList<>();

        JSONArray values = obj.getJSONArray("colors");
        for (int i = 0; i < values.length(); i++) {
            Color col;
            try {
                col = new Color(Integer.decode(values.getString(i)));
            } catch (Exception e) {
                try {
                    col = new Color(values.getInt(i));
                } catch (Exception e2) {
                    System.out.println("error while reading color: " + e.toString());
                    col = null;
                }
            }

            if (col != null) {
                colors.add(col);
            }
        }
    }

    public OrderedSetColorProvider(Collection<Color> colors) {
        this.colors = new ArrayList<>(colors);
    }

    public void setPeriod(double arg) {
        clock = new PollClock(arg);
    }

    @Override
    public Color mainColor() {
        return colors.get(0);
    }

    @Override
    public void update(double delta) {
        int ticks = clock.pollTicks(delta);
        for (int i = 0; i < ticks; i++) {
            advanceIndex();
        }
    }

    @Override
    public Color currentColor() {
        return colors.get(currentIndex);
    }

    @Override
    public Type getType() {
        return Type.ORDERED_SET;
    }

    protected final void setCurrentIndex(int arg) {
        currentIndex = Math.max(0, arg) % colors.size();
    }

    protected void advanceIndex() {
        setCurrentIndex(currentIndex + 1);
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject ret = super.toJSONObject();
        JSONArray colors = new JSONArray();
        for (Color c : this.colors) {
            colors.put(Integer.toUnsignedString(c.getRGB()));
        }
        ret.put("period", clock.period);
        ret.put("colors", colors);
        return ret;
    }
}

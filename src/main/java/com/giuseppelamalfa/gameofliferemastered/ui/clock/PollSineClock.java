/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.ui.clock;

import com.giuseppelamalfa.gameofliferemastered.ui.clock.PollClock;
import org.json.JSONObject;

/**
 *
 * @author glitchedcode
 */
public class PollSineClock extends PollClock {

    public final static double double_pi = Math.PI * 2;
    public final static double half_pi = Math.PI / 2;

    public PollSineClock(JSONObject obj) {
        this(obj.getDouble("period"));
    }
    
    public PollSineClock(double period) {
        super(period);
    }

    @Override
    public double pollElapsed(double delta) {
        return pollElapsedNormalized(delta) * period;
    }

    @Override
    public double pollElapsedNormalized(double delta) {
        return 0.5d + Math.sin((
                super.pollElapsedNormalized(delta) * double_pi) - half_pi
        ) / 2d;
    }
    
    @Override
    public Type getType() {
        return Type.SINE;
    }
}

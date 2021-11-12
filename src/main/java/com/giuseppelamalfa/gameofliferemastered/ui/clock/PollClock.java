/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.ui.clock;

import org.json.JSONObject;

/**
 *
 * @author glitchedcode
 */
public class PollClock {
    
    public enum Type{
        LINEAR(PollClock.class),
        SINE(PollSineClock.class);
        
        Type(Class<?> clazz) {
            this.clazz = clazz;
        }
        
        public PollClock constructFromJSON(JSONObject arg) throws Exception {
            return (PollClock) clazz.getConstructor(JSONObject.class).newInstance(arg);
        }
        
        public final Class<?> clazz;
    }
    
    public final double period;
    private double elapsed = 0d;
    
    public PollClock(JSONObject obj) {
        this(obj.getDouble("period"));
    }
    
    public PollClock(double period) {
        this.period = Math.max(period, 0d);
    }
    
    public int pollTicks(double delta) {
        if(period == 0d)
            return 1;
        
        elapsed += delta;
        int ret = (int)(elapsed / period);
        elapsed = elapsed % period;
        return ret;
    }
    
    public double pollElapsed(double delta) {
        if(period == 0d)
            return 0d;
        return elapsed = (elapsed + delta) % period;
    }
    
    public double pollElapsedNormalized(double delta) {
        if(period == 0d)
            return 0d;
        elapsed = (elapsed + delta) % period;
        
        return elapsed / period;
    }
    
    public Type getType() {
        return Type.LINEAR;
    }
    
    public JSONObject toJSONObject() {
        JSONObject ret = new JSONObject();
        ret.put("type", getType().toString());
        ret.put("period", period);
        return ret;
    }
}

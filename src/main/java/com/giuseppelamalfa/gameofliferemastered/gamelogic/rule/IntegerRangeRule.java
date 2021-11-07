/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.gamelogic.rule;

import java.io.Serializable;
import java.util.Collection;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author glitchedcode
 */
public class IntegerRangeRule implements RuleInterface<Integer>, Serializable {

    private int min;
    private int max;

    public IntegerRangeRule(Collection<Integer> values) {
        int loops = 0;
        for (Integer val : values) {
            if (loops == 2) {
                break;
            }
            if (loops == 0) {
                min = val;
            } else {
                max = val;
            }
            loops++;
        }
        if (loops < 2 | max < min) {
            throw new IllegalArgumentException();
        }
    }

    public IntegerRangeRule(int min, int max) {
        if (max < min) {
            throw new IllegalArgumentException();
        }
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean test(Integer value) {
        return min <= value & value <= max;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject ret = new JSONObject();
        ret.put("ruleClassName", "IntegerSetRule");
        JSONArray args = new JSONArray();
        args.put(min);
        args.put(max);
        ret.put("args", args);
        return ret;
    }

}

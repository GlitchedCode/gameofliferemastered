/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.ui.colors;

import com.giuseppelamalfa.gameofliferemastered.ui.clock.PollClock;
import java.awt.Color;
import org.json.JSONObject;

class NormalizedRange {

    final double start;
    final double end;
    final double diff;

    public NormalizedRange(JSONObject obj) {
        this(obj.getDouble("min"), obj.getDouble("max"));
    }

    public NormalizedRange(double min, double max) {
        this.start = Math.min(Math.max(min, 0d), 1d);
        this.end = Math.min(Math.max(max, 0d), 1d);
        diff = (this.end - this.start);
    }

    public double get(double arg) {
        arg = Math.min(Math.max(arg, 0d), 1d);
        return Math.abs(start + arg * diff);
    }

    public JSONObject toJSONObject() {
        JSONObject ret = new JSONObject();
        ret.put("min", start);
        ret.put("max", start);
        return ret;
    }
}

/**
 *
 * @author glitchedcode
 */
public class AnimatedHSBProvider extends ColorProvider {

    public final Color mainColor;

    private final NormalizedRange hRange;
    private final PollClock hClock;

    private final NormalizedRange sRange;
    private final PollClock sClock;

    private final NormalizedRange vRange;
    private final PollClock vClock;

    private Color currentColor;

    public AnimatedHSBProvider(JSONObject obj) {

        NormalizedRange _hRange, _sRange, _vRange;

        try {
            _hRange = new NormalizedRange(obj.getJSONObject("hRange"));
        } catch (Exception e) {
            _hRange = new NormalizedRange(0d, 0d);
        }
        try {
            _sRange = new NormalizedRange(obj.getJSONObject("sRange"));
        } catch (Exception e) {
            _sRange = new NormalizedRange(0d, 0d);
        }
        try {
            _vRange = new NormalizedRange(obj.getJSONObject("vRange"));
        } catch (Exception e) {
            _vRange = new NormalizedRange(0d, 0d);
        }

        hRange = _hRange;
        sRange = _sRange;
        vRange = _vRange;

        PollClock _hClock, _sClock, _vClock;

        try {
            JSONObject tmp = obj.getJSONObject("hClock");
            _hClock = PollClock.Type.valueOf(tmp.getString("type")).constructFromJSON(tmp);
        } catch (Exception e) {
            _hClock = new PollClock(0d);
        }
        try {
            JSONObject tmp = obj.getJSONObject("sClock");
            _sClock = PollClock.Type.valueOf(tmp.getString("type")).constructFromJSON(tmp);
        } catch (Exception e) {
            _sClock = new PollClock(0d);
        }
        try {
            JSONObject tmp = obj.getJSONObject("vClock");
            _vClock = PollClock.Type.valueOf(tmp.getString("type")).constructFromJSON(tmp);
        } catch (Exception e) {
            _vClock = new PollClock(0d);
        }

        hClock = _hClock;
        sClock = _sClock;
        vClock = _vClock;

        currentColor = mainColor = new Color(Color.HSBtoRGB((float) hRange.start, (float) sRange.start, (float) vRange.start));
    }

    public AnimatedHSBProvider(
            double minH, double maxH, double hPeriod,
            double minS, double maxS, double sPeriod,
            double minV, double maxV, double vPeriod
    ) throws Exception {

        if (minH < 0d | maxH > 1d
                | minS < 0d | maxS > 1d
                | minV < 0d | maxV > 1d
                | hPeriod < 0d | sPeriod < 0d | vPeriod < 0d) {
            throw new Exception("Invalid parameters");
        }

        hRange = new NormalizedRange(minH, maxH);
        sRange = new NormalizedRange(minS, maxS);
        vRange = new NormalizedRange(minV, maxV);

        hClock = new PollClock(hPeriod);
        sClock = new PollClock(sPeriod);
        vClock = new PollClock(vPeriod);

        currentColor = mainColor = new Color(Color.HSBtoRGB((float) hRange.start, (float) sRange.start, (float) vRange.start));
    }

    @Override
    public Color mainColor() {
        return mainColor;
    }

    @Override
    public void update(double delta) {
        float currentH = (float) hRange.get(hClock.pollElapsedNormalized(delta));
        float currentS = (float) sRange.get(sClock.pollElapsedNormalized(delta));
        float currentV = (float) vRange.get(vClock.pollElapsedNormalized(delta));
        currentColor = new Color(Color.HSBtoRGB(currentH, currentS, currentV));
    }

    @Override
    public Color currentColor() {
        return currentColor;
    }

    @Override

    public Type getType() {
        return Type.ANIMATED_HSB;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject ret = super.toJSONObject();

        ret.put("hRange", hRange.toJSONObject());
        ret.put("sRange", sRange.toJSONObject());
        ret.put("vRange", vRange.toJSONObject());

        ret.put("hClock", this.hClock.toJSONObject());
        ret.put("sClock", this.sClock.toJSONObject());
        ret.put("vClock", this.vClock.toJSONObject());

        return ret;
    }
}

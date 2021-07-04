/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.utils;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author glitchedcode
 */
public class TimerWrapper implements Serializable {

    private Timer t = new Timer();

    public TimerTask schedule(final Runnable r, long delay) {
        final TimerTask task = new TimerTask() {
            public void run() {
                r.run();
            }
        };
        t.schedule(task, delay);
        return task;
    }

    public TimerTask scheduleAtFixedRate(final Runnable r, long delay, long period) {
        final TimerTask task = new TimerTask() {
            public void run() {
                r.run();
            }
        };
        t.scheduleAtFixedRate(task, delay, period);
        return task;
    }

    public TimerTask scheduleAtFixedRate(final Runnable r, long delay, long period, long iterations) {
        final TimerTask task = new TimerTask() {
            public void run() {
                r.run();
            }
        };
        t.scheduleAtFixedRate(task, delay, period);
        return task;
    }

    public void cancel() {
        t.cancel();
        t = new Timer();
    }
}

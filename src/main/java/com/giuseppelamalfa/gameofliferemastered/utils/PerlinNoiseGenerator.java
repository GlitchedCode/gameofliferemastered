/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.giuseppelamalfa.gameofliferemastered.utils;

import java.util.Random;

/**
 *
 * @author glitchedcode
 */
public class PerlinNoiseGenerator {

    private Random rng;

    private double seed1d[];
    private double perlin1d[];

    private double seed2d[][];
    private double perlin2d[][];

    private int width;
    private int height;

    public PerlinNoiseGenerator(int width, int height) {
        this(width, height, new Random().nextLong());
    }

    public PerlinNoiseGenerator(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        rng = new Random(seed);

        seed1d = new double[width];
        perlin1d = new double[height];

        seed2d = new double[width][height];
        perlin2d = new double[width][height];
    }

}

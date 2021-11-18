/*
 * This implementation of the simplex noise algorithm for 2D
 * is based on public domain code by Stefan Gustavson, last updated
 * on 2012-03-09.
 */
package com.giuseppelamalfa.gameofliferemastered.utils;

import java.util.Random;

// Inner class to speed upp gradient computations
// (In Java, array access is a lot slower than member access)
public class NoiseGenerator {

    private final Random rng;

    private final short seed[];
    private final short seedMod12[];
    private final int seedLen;

    private final int width;
    private final int height;

    public NoiseGenerator(int width, int height) {
        this(width, height, new Random());
    }

    public NoiseGenerator(int width, int height, Random rng) {
        this.width = width;
        this.height = height;
        this.rng = rng;

        seedLen = width > height ? width : height;
        seed = new short[seedLen];
        seedMod12 = new short[seedLen];
        regenerate();
    }

    public void regenerate() {
        regenerate(-1);
    }

    public void regenerate(long val) {
        if (val != -1) {
            rng.setSeed(val);
        }

        for (int c = 0; c < seedLen; c++) {
            seed[c] = (short) Math.abs(rng.nextInt() & 0x7FFF);
            seedMod12[c] = (short) (seed[c] % 12); 
        }
    }

    private static int fastfloor(double x) {
        int xi = (int) x;
        return x < xi ? xi - 1 : xi;
    }

    private static double dot(Grad g, double x, double y) {
        return g.x * x + g.y * y;
    }

    // 2D skewing factors
    private static final double F2 = 0.5 * (Math.sqrt(3.0) - 1.0);
    private static final double G2 = (3.0 - Math.sqrt(3.0)) / 6.0;

    public double noise(double x, double y) {
        // Noise from the other three corners
        double n0, n1, n2;

        // Skew input space to simplex cell space.
        double skew = (x + y) * F2;
        int i = fastfloor(x + skew);
        int j = fastfloor(y + skew);

        // Unskew cell origin back to input space.
        double t = (i + j) * G2;
        double x0 = i - t;
        double y0 = j - t;

        // Distance from cell origin
        double distX = x - x0;
        double distY = y - y0;

        // For the 2D case, the simplex shape is an equilateral triangle.
        // Determine which simplex we are in.
        int i1, j1; // offset for middle corner of simplex in simplex space
        if (distX > distY) {
            // lower triangle, XY order: (0,0)->(1,0)->(1,1)
            i1 = 1;
            j1 = 0;
        } else {
            // lower triangle, XY order: (0,0)->(1,0)->(1,1)
            i1 = 0;
            j1 = 1;
        }

        // Offsets for middle corner in (x,y) unskewed coords
        double x1 = distX - i1 + G2;
        double y1 = distY - j1 + G2;
        // Offsets for last corner in (x,y) unskewed coords
        double x2 = distX - 1.0 + 2.0 * G2;
        double y2 = distY - 1.0 + 2.0 * G2;

        // Work out the hashed gradient indices of the three simplex corners
        int ii = i % seedLen;
        int jj = j % seedLen;
        int gi0 = seedMod12[(ii + seed[jj % seedLen]) % seedLen];
        int gi1 = seedMod12[(ii + i1 + seed[(jj + j1) % seedLen]) % seedLen];
        int gi2 = seedMod12[(ii + 1 + seed[(jj + 1) % seedLen]) % seedLen];

        // Calculate the contribution from the three corners
        double t0 = 0.5 - distX * distX - distY * distY;
        if (t0 < 0) {
            n0 = 0.0;
        } else {
            t0 *= t0;
            n0 = t0 * t0 * dot(grad3[gi0], distX, distY);  // (x,y) of grad3 used for 2D gradient
        }
        double t1 = 0.5 - x1 * x1 - y1 * y1;
        if (t1 < 0) {
            n1 = 0.0;
        } else {
            t1 *= t1;
            n1 = t1 * t1 * dot(grad3[gi1], x1, y1);
        }
        double t2 = 0.5 - x2 * x2 - y2 * y2;
        if (t2 < 0) {
            n2 = 0.0;
        } else {
            t2 *= t2;
            n2 = t2 * t2 * dot(grad3[gi2], x2, y2);
        }
        // Add contributions from each corner to get the final noise value.
        // The result is scaled to return values in the interval [0,1].
        return 35.0 * (n0 + n1 + n2) + 0.5;
    }

    private static Grad grad3[] = {new Grad(1, 1, 0), new Grad(-1, 1, 0), new Grad(1, -1, 0), new Grad(-1, -1, 0),
        new Grad(1, 0, 1), new Grad(-1, 0, 1), new Grad(1, 0, -1), new Grad(-1, 0, -1),
        new Grad(0, 1, 1), new Grad(0, -1, 1), new Grad(0, 1, -1), new Grad(0, -1, -1)};

    private static class Grad {

        double x, y, z, w;

        Grad(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        Grad(double x, double y, double z, double w) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }
    }
}

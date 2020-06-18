package com.mygdx.game.courses;

import com.mygdx.game.parser.BiLinearArrayFunction2d;
import com.mygdx.game.parser.Function2d;

import java.util.Random;

public class FractalGenerator {

    private Random random;

    public FractalGenerator(long seed) {
        this(new Random(seed));
    }
    public FractalGenerator(Random random) {
        this.random = random;
    }

    public BiLinearArrayFunction2d biLinearFractal(int desired_origin_size, int smoothing_factor, double roughness, double real_size, double minimum_value, double maximum_value, Function2d out_of_bounds) {
        double[][] fractal = smoothenedFractalMap(desired_origin_size, smoothing_factor, roughness);
        applyRangeToMatrix(fractal, minimum_value, maximum_value);
        return new BiLinearArrayFunction2d(fractal, out_of_bounds, real_size, real_size);
    }

    /**
     * @param desired_size the desired length of the array (the array will always be a square array)
     * @param smoothing_factor approximately the multiplier used to go from fractal array to returned array, starts at 1,
     *                         higher values make sure a smaller fractal array is used, which affects the amount of detail
     * @param roughness affects the amount of distortion in the source fractal map
     * @return a stretched version of a fractal map with the specified roughness. All values lie between 0 and 1.
     */
    public double[][] smoothenedFractalMap(final int desired_size, int smoothing_factor, final double roughness) {
        if (smoothing_factor < 1) smoothing_factor = 1;
        int small_size_desired = (desired_size / smoothing_factor);
        int detail = MapGenUtils.approximate_required_detail(small_size_desired);
        double[][] fractal_h = fractalMap(detail, roughness);
        return enlargeMatrix(fractal_h, smoothing_factor);
    }

    /** Approaches the scaling of the size of a matrix m by the given factor and interpolates values linearly
     * (a 2x2 * 2 gives a 3x3, a 100x100 * 2 gives a 199x199) */
    public static double[][] enlargeMatrix(double[][] m, int factor) {
        factor = factor - 1;
        int smallW = m.length, smallH = m[0].length;
        int bigW = smallW + (smallW-1)*factor;
        int bigH = smallH + (smallH-1)*factor;
        double[][] result = new double[bigW][bigH];
        int l = factor + 1;
        for (int i=0; i < smallW; i++) {
            for (int j=0; j < smallH; j++) {
                result[i*l][j*l] = m[i][j];
                if (i < smallW-1) {
                    for (int k=1; k < l; k++) {
                        result[i*l+k][j*l] = ((l-k)*m[i][j] + k*m[i+1][j])/l;}}
                if (j < smallH-1) {
                    for (int k=1; k < l; k++) {
                        result[i*l][j*l+k] = ((l-k)*m[i][j] + k*m[i][j+1])/l;}}}}
        for (int i=0; i < smallW; i++) {
            for (int j=0; j < smallH; j++) {
                if (i < smallW-1 && j < smallH-1) {
                    for (int k=1; k < l; k++) {
                        for (int p=1; p < l; p++) {
                            result[i*l+k][j*l+p] = ((l-p)*result[i*l+k][j*l] + p*result[i*l+k][j*l+l] + (l-k)*result[i*l][j*l+p] + k*result[i*l+l][j*l+p])/(2*l);}}}
            }} return result;
    }

    /**
     * linearly applies a range to an array only containing values from 0 to 1
     * @param m an array only containing values between 0 and 1
     * @param min all values of 0 will be set to min, all above 0 will be above min
     * @param max all values of 1 will be set to max, all below 1 will be below max
     */
    public static void applyRangeToMatrix(double[][] m, double min, double max) {
        for (int i=0; i < m.length; i++)
            for (int j=0; j < m[i].length; j++) m[i][j] = min + m[i][j]*(max - min);
    }

    /**
     * @param detail the log2 of the size of the returned array, the bigger this number, the higher the amount of unique datapoints
     * @param roughness a factor that influences the amount of distortion between unique datapoints, typically between 0 and 1
     * @return an array of size 2^detail with values between 0 and 1 where the data is all generated using fractals
     */
    public double[][] fractalMap(final int detail, final double roughness) {
        int divisions = 1 << detail;
        double[][] heatmap = new double[divisions+1][divisions+1];
        heatmap[0][0] = rnd();
        heatmap[0][divisions] = rnd();
        heatmap[divisions][0] = rnd();
        heatmap[divisions][divisions] = rnd();
        double rough = roughness;
        for (int i = 0; i < detail; ++ i) {
            int r = 1 << (detail - i), s = r >> 1;
            for (int j = 0; j < divisions; j += r)
                for (int k = 0; k < divisions; k += r)
                    heatmap = diamond (heatmap,j, k, r, rough);
            if (s > 0)
                for (int j = 0; j <= divisions; j += s)
                    for (int k = (j + s) % r; k <= divisions; k += r)
                        heatmap = square (heatmap, divisions,j - s, k - s, r, rough);
            rough *= roughness;
        }
        double min = heatmap[0][0]; double max = min;
        for (int i = 0; i <= divisions; ++ i)
            for (int j = 0; j <= divisions; ++ j)
                if (heatmap[i][j] < min) min = heatmap[i][j];
                else if (heatmap[i][j] > max) max = heatmap[i][j];
        for (int i=0; i < heatmap.length; i++) {
            for (int j=0; j < heatmap[0].length; j++) {
                double alt = heatmap[i][j];
                heatmap[i][j] = (alt - min) / (max - min);
            }
        }
        return heatmap;
    }

    private double[][] diamond (double[][] terrain, int x, int y, int side, double scale) {
        if (side > 1) {
            int half = side / 2;
            double avg = (terrain[x][y] + terrain[x + side][y] +
                    terrain[x + side][y + side] + terrain[x][y + side]) * 0.25;
            terrain[x + half][y + half] = avg + rnd () * scale;
        } return terrain;
    }

    private double[][] square (double[][] terrain, int divisions, int x, int y, int side, double scale) {
        int half = side / 2;
        double avg = 0.0, sum = 0.0;
        if (x >= 0)
        { avg += terrain[x][y + half]; sum += 1.0; }
        if (y >= 0)
        { avg += terrain[x + half][y]; sum += 1.0; }
        if (x + side <= divisions)
        { avg += terrain[x + side][y + half]; sum += 1.0; }
        if (y + side <= divisions)
        { avg += terrain[x + half][y + side]; sum += 1.0; }
        terrain[x + half][y + half] = avg / sum + rnd () * scale;
        return terrain;
    }

    private double rnd () {
        return 2. * random.nextDouble () - 1.0;
    }

}

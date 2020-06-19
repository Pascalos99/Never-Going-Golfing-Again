package com.mygdx.game.courses;

import java.util.Random;

public class FractalInfo {
    public long seed;
    public double roughness, minimum, maximum;
    public String resolution_setting, smoothness_setting, interpolation_setting;
    public FractalInfo(long seed, double roughness, String resolution, String smoothness, String interpolation, double min, double max) {
        this.seed = seed;
        this.roughness = roughness;
        resolution_setting = resolution;
        smoothness_setting = smoothness;
        interpolation_setting = interpolation;
        minimum = min; maximum = max;
    }
    public static FractalInfoBuilder getEmpty() {
        return new FractalInfoBuilder();
    }

    static class FractalInfoBuilder {
        public long seed = new Random(System.currentTimeMillis()).nextLong();
        public double roughness = 0.5, minimum = -5, maximum = 15;
        public String resolution_setting = "Medium", smoothness_setting = "None", interpolation_setting = "bi-cubic";
        public void addSeed(long seed) { this.seed = seed; }
        public void addRoughness(double roughness) { this.roughness = roughness; }
        public void addResolution(String resolution) { resolution_setting = resolution; }
        public void addSmoothness(String smoothness) { smoothness_setting = smoothness; }
        public void addInterpolation(String interpolation) { interpolation_setting = interpolation; }
        public void addMin(double min) { minimum = min; }
        public void addMax(double max) { maximum = max; }
        public FractalInfo get() {
            return new FractalInfo(seed, roughness, resolution_setting, smoothness_setting, interpolation_setting, minimum, maximum);
        }
    }
}

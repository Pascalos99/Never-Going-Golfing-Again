package com.mygdx.game.courses;

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
}

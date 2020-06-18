package com.mygdx.game.parser;

import com.mygdx.game.utils.Variables;
import com.mygdx.game.utils.Vector2d;

public class BiLinearArrayFunction2d extends ArrayFunction2d {

    public BiLinearArrayFunction2d(double[][] array, Function2d out_of_bounds_value) {
        this(array, out_of_bounds_value, array.length, array[0].length);
    }

    public BiLinearArrayFunction2d(double[][] _original, Function2d out_of_bounds_value, double real_width, double real_height) {
        super(_original, real_width, real_height, out_of_bounds_value);
    }

    @Override
    public Vector2d gradientInBounds(double x, double y) {
        double p = floor(x), q= floor(x+1);
        double r = floor(y), s = floor(y+1);
        double T = array()[floor(x)][floor(y)];
        double U = array()[floor(x)][floor(y+1)];
        double V = array()[floor(x+1)][floor(y)];
        double W = array()[floor(x+1)][floor(y+1)];
        // Let's just trust Wolfram Alpha on this one...
        return new Vector2d(
                (r - y) * (U - W) + (s - y) * (V - T),
                p * (V - W) + q * (U - T) + x * (T - U - V + W)
        );
    }

    @Override
    public double evaluateInBounds(double x, double y) {
        double x1 = floor(x), x2 = floor(x+1);
        double y1 = floor(y), y2 = floor(y+1);
        double Q11 = array()[floor(x)][floor(y)];
        double Q12 = array()[floor(x)][floor(y+1)];
        double Q21 = array()[floor(x+1)][floor(y)];
        double Q22 = array()[floor(x+1)][floor(y+1)];
        double fx1 = (x2 - x) * Q11 + (x - x1) * Q21;
        double fx2 = (x2 - x) * Q12 + (x - x1) * Q22;

        return (y2 - y) * fx1 + (y - y1) * fx2;
    }

}

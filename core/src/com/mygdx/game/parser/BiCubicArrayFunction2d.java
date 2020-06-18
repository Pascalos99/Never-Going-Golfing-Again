package com.mygdx.game.parser;

import com.mygdx.game.utils.Vector2d;

public class BiCubicArrayFunction2d extends ArrayFunction2d {

    public BiCubicArrayFunction2d(double[][] array, Function2d out_of_bounds_value) {
        this(array, out_of_bounds_value, array.length, array[0].length);
    }
    public BiCubicArrayFunction2d(double[][] array, Function2d out_of_bounds_value, double real_width, double real_height) {
        super(array, real_width, real_height, out_of_bounds_value);
    }

    @Override
    public double evaluateInBounds(double x, double y) {
        return 0;
    }

    @Override
    public Vector2d gradientInBounds(double x, double y) {
        return null;
    }
}

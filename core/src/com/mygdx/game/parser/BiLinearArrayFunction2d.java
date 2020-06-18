package com.mygdx.game.parser;

import com.mygdx.game.utils.Variables;
import com.mygdx.game.utils.Vector2d;

public class BiLinearArrayFunction2d implements ArrayFunction2d {

    private double[][] original;
    private double[][] array;
    private Function2d out_of_bounds;

    private double real_width, real_height;
    private double shift_x, shift_y;

    public BiLinearArrayFunction2d(double[][] array, Function2d out_of_bounds_value) {
        this(array, out_of_bounds_value, array.length, array[0].length);
    }

    public BiLinearArrayFunction2d(double[][] _original, Function2d out_of_bounds_value, double real_width, double real_height) {
        this.real_width = real_width;
        this.real_height = real_height;
        original = new double[_original.length][_original[0].length];
        for (int i=0; i < original.length; i++)
            for (int j=0; j < original[i].length; j++) original[i][j] = _original[i][j];
        out_of_bounds = out_of_bounds_value;
        array = new double[original.length + 2][original[0].length + 2];
        for (int i=0; i < array.length; i++)
            for (int j=0; j < array[i].length; j++)
                if (i==0 || j==0 || i == array.length-1 || j == array.length-1) array[i][j] = out_of_bounds_value.evaluate(i-1, j-1);
                else array[i][j] = original[i-1][j-1];
    }

    public void setShift(double x, double y) {
        shift_x = x; shift_y = y;
    }
    public void setShift(Vector2d shift) {
        setShift(shift.get_x(), shift.get_y());
    }

    @Override
    public Vector2d gradient(double _x, double _y) {
        double x = ((_x - shift_x) / real_width) * array.length;
        double y = ((_y - shift_y) / real_height) * array[0].length;
        if (x >= array.length - 2|| y >= array.length - 2 || x < -1 || y < -1) return out_of_bounds.gradient(x, y);
        x++; y++;
        double p = floor(x), q= floor(x+1);
        double r = floor(y), s = floor(y+1);
        double T = array[floor(x)][floor(y)];
        double U = array[floor(x)][floor(y+1)];
        double V = array[floor(x+1)][floor(y)];
        double W = array[floor(x+1)][floor(y+1)];

        return new Vector2d(
                (r - y) * (U - W) + (s - y) * (V - T),
                p * (V - W) + q * (U - T) + x * (T - U - V + W)
        ); // I still trust WolframAlpha on this one...
    }

    @Override
    public double evaluate(double _x, double _y) {
        double x = ((_x - shift_x) / real_width) * array.length;
        double y = ((_y - shift_y) / real_height) * array[0].length;
        if (x >= array.length - 2|| y >= array.length - 2 || x < -1 || y < -1) return out_of_bounds.evaluate(x, y);
        x++; y++;
        double x1 = floor(x), x2 = floor(x+1);
        double y1 = floor(y), y2 = floor(y+1);
        double Q11 = array[floor(x)][floor(y)];
        double Q12 = array[floor(x)][floor(y+1)];
        double Q21 = array[floor(x+1)][floor(y)];
        double Q22 = array[floor(x+1)][floor(y+1)];
        double fx1 = (x2 - x) * Q11 + (x - x1) * Q21;
        double fx2 = (x2 - x) * Q12 + (x - x1) * Q22;

        return (y2 - y) * fx1 + (y - y1) * fx2;
    }

    @Override
    public double[][] getArray() {
        return original;
    }

    @Override
    public Function2d getOutOfBoundsFunction() {
        return out_of_bounds;
    }

    private static int floor(double x) {
        return (int)x;
    }

}

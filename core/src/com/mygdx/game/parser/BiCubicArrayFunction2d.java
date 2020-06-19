package com.mygdx.game.parser;

import com.mygdx.game.utils.Vector2d;

public class BiCubicArrayFunction2d extends ArrayFunction2d {

    private static double[][] thank = {
            {1 ,  0,  0,  0},
            {0 ,  0,  1,  0},
            {-3,  3, -2, -1},
            {2 , -2,  1,  1}
    };
    private static double[][] wikipedia = {
            {1 ,  0, -3,  2},
            {0 ,  0,  3, -2},
            {0 ,  1, -2,  1},
            {0 ,  0, -1,  1}
    };

    private double[][][][] master_A;

    public BiCubicArrayFunction2d(double[][] array, Function2d out_of_bounds_value) {
        this(array, out_of_bounds_value, array.length, array[0].length);
    }
    public BiCubicArrayFunction2d(double[][] _array, Function2d out_of_bounds_value, double real_width, double real_height) {
        super(_array, real_width, real_height, out_of_bounds_value);
        double[][] m = array();
        master_A = new double[m.length - 1][m[0].length - 1][][];
        // do not have to define master_A for i=0 and j=0
        double dx = gridX(), dy = gridY();
        for (int i = 0; i < master_A.length; i++)
            for (int j = 0; j < master_A[i].length; j++) {
                int I = i + 1, J = j + 1;
                double[][] cornerValues = {
                        {m[i][j], m[i][J], dy * approxPartialY(i, j), dy * approxPartialY(i, J)},
                        {m[I][j], m[I][J], dy * approxPartialY(I, j), dy * approxPartialY(I, J)},
                        {dx * approxPartialX(i, j), dx * approxPartialX(i, J), dx*dy * approxCrossXY(i, j), dx*dy * approxCrossXY(i, J)},
                        {dx * approxPartialX(I, j), dx * approxPartialX(I, J), dx*dy * approxCrossXY(I, j), dx*dy * approxCrossXY(I, J)}
                };
                master_A[i][j] = generateA(cornerValues);
            }
    }

    private double[][] generateA(double[][] cornerValues) {
        return multiplyMatrices(multiplyMatrices(thank, cornerValues), wikipedia);
    }

    @Override
    public double evaluateInBounds(double x, double y) {
        if (x == floor(x) && y == floor(y)) return array()[floor(x)][floor(y)];
        return evaluate(master_A[floor(x)][floor(y)], x, y);
    }

    @Override
    public Vector2d gradientInBounds(double x, double y) {
        return gradient(master_A[floor(x)][floor(y)], x, y);
    }

    private double approxPartialX(int x, int y) {
        double[][] m = array();
        if (x <= 1) return (-3*m[x][y] + 4*m[x+1][y] - m[x+2][y])/2;
        if (x >= m.length-1) return (-3*m[x][y] + 4*m[x-1][y] - m[x-2][y])/2;
        return (m[x+1][y] - m[x-1][y])/2;
    }
    private double approxPartialY(int x, int y) {
        double[][] m = array();
        if (y <= 1) return (-3*m[x][y] + 4*m[x][y+1] - m[x][y+2])/2;
        if (y >= m[0].length-1) return (-3*m[x][y] + 4*m[x][y-1] - m[x][y-2])/2;
        return (m[x][y+1] - m[x][y-1])/2;
    }
    private double approxCrossXY(int x, int y) {
        double[][] m = array();
        int h = 1, k = 1;
        boolean edge_x = false, edge_y = false;
        if (x <= 1) edge_x = true;
        if (x >= m.length-1) {edge_x = true; h = -1;}
        if (y <= 1) edge_y = true;
        if (y >= m[0].length-1) {edge_y = true; k = -1;}
        if (edge_y) return (-3*approxPartialX(x,y) + 4*approxPartialX(x, y+k) - approxPartialX(x,y+k+k))/2;
        if (edge_x) return (-3*approxPartialY(x,y) + 4*approxPartialY(x+h,y) - approxPartialY(x+h+h,y))/2;
        return (m[x+h][y+h] - m[x+h][y] - m[x][y+h] + m[x][y])/4;
    }

    private static double evaluate(double[][] A, double _x, double _y) {
        double sum = 0;
        double x = (_x - floor(_x)) / (floor(_x + 1) - floor(_x));
        double y = (_y - floor(_y)) / (floor(_y + 1) - floor(_y));
        for (int i=0; i < A.length; i++) {
            for (int j=0; j < A[i].length; j++)
                sum += A[i][j] * Math.pow(x, i) * Math.pow(y, j); }
        return sum;
    }

    private static Vector2d gradient(double[][] A, double _x, double _y) {
        double sum_x = 0;
        double x = (_x - floor(_x)) / (floor(_x + 1) - floor(_x));
        double y = (_y - floor(_y)) / (floor(_y + 1) - floor(_y));
        for (int i=1; i < A.length; i++) {
            for (int j=0; j < A[i].length; j++)
                sum_x += A[i][j] * i * Math.pow(x, i-1) * Math.pow(y, j); }
        double sum_y = 0;
        for (int i=0; i < A.length; i++) {
            for (int j=1; j < A[i].length; j++)
                sum_y += A[i][j] * Math.pow(x, i) * j * Math.pow(y, j-1); }
        return new Vector2d(sum_x, sum_y);
    }


    private static double[][] multiplyMatrices(double[][] a, double[][] b) {
        int rowsA = a.length;
        int cola = a[0].length;
        int colsB = b[0].length;
        double[][] result = new double[rowsA][colsB];
        for (int i=0; i < rowsA; i++)
            for (int j=0; j < colsB; j++)
                for (int z=0; z < cola; z++) // oh no, plagiarism from myself XD
                    result[i][j] += a[i][z] * b[z][j]; // I wrote this for CS1 :)
        return result;
    }

}

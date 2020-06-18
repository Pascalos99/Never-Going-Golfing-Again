package com.mygdx.game.parser;

import com.mygdx.game.utils.Vector2d;

import java.lang.reflect.Array;

public abstract class ArrayFunction2d implements Function2d {

	private final double[][] original;
	private final double[][] array;
	private final Function2d out_of_bounds;

	private final double real_width, real_height;
	private double shift_x, shift_y;

	public ArrayFunction2d(double[][] _array, double real_width, double real_height, Function2d out_of_bounds_value) {
		this.real_width = real_width;
		this.real_height = real_height;
		original = deepCopy(_array);
		out_of_bounds = out_of_bounds_value;
		array = new double[original.length + 2][original[0].length + 2];
		for (int i=0; i < array.length; i++)
			for (int j=0; j < array[i].length; j++)
				if (i==0 || j==0 || i == array.length-1 || j == array.length-1) array[i][j] = out_of_bounds_value.evaluate(i-1, j-1);
				else array[i][j] = original[i-1][j-1];
	}

	@Override
	public final double evaluate(double _x, double _y) {
		double x = ((_x - shift_x) / real_width) * array.length;
		double y = ((_y - shift_y) / real_height) * array[0].length;
		if (x >= array.length - 2|| y >= array.length - 2 || x < -1 || y < -1) return out_of_bounds.evaluate(x, y);
		x++; y++;
		return evaluateInBounds(x, y);
	}

	@Override
	public final Vector2d gradient(double _x, double _y) {
		double x = ((_x - shift_x) / real_width) * array.length;
		double y = ((_y - shift_y) / real_height) * array[0].length;
		if (x >= array.length - 2|| y >= array.length - 2 || x < -1 || y < -1) return out_of_bounds.gradient(x, y);
		x++; y++;
		return gradientInBounds(x, y);
	}

	public abstract double evaluateInBounds(double x, double y);

	public abstract Vector2d gradientInBounds(double x, double y);

	protected final double[][] array() {
		return array;
	}
	final Function2d getOutOfBoundsFunction() {
		return out_of_bounds;
	}
	void setShift(double x, double y) {
		shift_x = x; shift_y = y;
	}
	public void setShift(Vector2d shift) {
		setShift(shift.get_x(), shift.get_y());
	}

	public static double[][] deepCopy(double[][] array) {
		double[][] result = new double[array.length][array[0].length];
		for (int i=0; i < result.length; i++) {
			for (int j=0; j < result[i].length; j++) result[i][j] = array[i][j]; }
		return result;
	}

	protected static int floor(double x) {
		return (int)x;
	}

}

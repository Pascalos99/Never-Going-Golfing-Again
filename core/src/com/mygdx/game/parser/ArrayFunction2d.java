package com.mygdx.game.parser;

public interface ArrayFunction2d extends Function2d {
	
	public double[][] getArray();
	public Function2d getOutOfBoundsFunction();

	public default int getCourseWidth() {
		return getArray().length;
	}

	public default int getCourseHeight() {
		return getArray()[0].length;
	}
	
}

package com.mygdx.game.parser;

import com.mygdx.game.utils.Vector2d;

public interface Function2d {

	default double evaluate(Vector2d p) {
		return evaluate(p.get_x(), p.get_y());
	}

	default Vector2d gradient(Vector2d p) {
		return gradient(p.get_x(), p.get_y());
	}

	Vector2d gradient(double x, double y);

	double evaluate(double x, double y);
	
	static Function2d getConstant(double value) {
		return new ConstantFunction(value);
	}

	default double directionalDerivative(double x, double y, Vector2d direction) {
		return direction.normalize().dot(gradient(x, y));
	}

	class ConstantFunction implements Function2d {
		public ConstantFunction(double constant) {
			value = constant;
		}
		public final double value;
		public double evaluate(double x, double y) {
			return value;
		}
		public Vector2d gradient(double x, double y) {
			return new Vector2d(0, 0);
		}
	}
}
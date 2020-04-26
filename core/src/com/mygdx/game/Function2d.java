package com.mygdx.game;

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
		return new Function2d() {
			public double evaluate(double x, double y) {
				return value;
			}
			public Vector2d gradient(double x, double y) {
				return new Vector2d(0, 0);
			}
		};
	}

	default Function2d getScaledBy(double scale) {
		Function2d from = this;
		return new Function2d() {
			@Override
			public Vector2d gradient(double x, double y) {
				return from.gradient(x, y).scale(scale);
			}

			@Override
			public double evaluate(double x, double y) {
				return from.evaluate(x, y) * scale;
			}
		};
	}
	
}
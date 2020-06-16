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
	
	static AtomFunction2d getConstant(double value) {
		return new AtomFunction2d(""+value);
	}

	default double directionalDerivative(double x, double y, Vector2d direction) {
		return direction.normalize().dot(gradient(x, y));
	}

	default Function2d raw_add(Function2d func) {
		return new AdditionFunction2d(this, func);
	}

	default Function2d raw_subtract(Function2d func) {
		return new AdditionFunction2d(this, new ScaledFunction2d(func, -1));
	}

	default Function2d sub(Function2d func) {
		return raw_subtract(func);
	}

	default Function2d add(Function2d func) {
		return raw_add(func);
	}

	default Function2d shift(Vector2d shift) {
		return new ShiftedFunction2d(this, shift);
	}

	default Function2d scale(double scaling) {
		return new ScaledFunction2d(this, scaling);
	}

	interface ModifiedFunction extends Function2d {
		public Function2d original();
	}

	class ShiftedFunction2d implements ModifiedFunction {

		public final Function2d original;
		public final Vector2d shift;
		public ShiftedFunction2d(Function2d original, Vector2d shift) {
			this.original = original;
			this.shift = shift;
		}
		@Override
		public Vector2d gradient(double x, double y) {
			return original.gradient(x-shift.get_x(), y-shift.get_y());
		}

		@Override
		public double evaluate(double x, double y) {
			return original.evaluate(x-shift.get_x(), y-shift.get_y());
		}

		public String toString() {
			return "Function2d of "+original+", shifted by "+shift;
		}

		@Override
		public Function2d original() {
			return original;
		}
	}

	class ScaledFunction2d implements ModifiedFunction {
		public final Function2d original;
		public final double factor;
		public ScaledFunction2d(Function2d original, double factor) {
			this.original = original;
			this.factor = factor;
		}
		@Override
		public Vector2d gradient(double x, double y) {
			return original.gradient(x, y);
		}

		@Override
		public double evaluate(double x, double y) {
			return original.evaluate(x, y) * factor;
		}

		public String toString() {
			return "Function2d of "+original+", multiplied by "+factor;
		}

		@Override
		public Function2d original() {
			return original;
		}
	}

	class AdditionFunction2d implements Function2d {
		public final Function2d a, b;
		public AdditionFunction2d(Function2d a, Function2d b) {
			this.a = a;
			this.b = b;
		}
		@Override
		public Vector2d gradient(double x, double y) {
			return a.gradient(x, y).add(b.gradient(x, y));
		}

		@Override
		public double evaluate(double x, double y) {
			return a.evaluate(x, y) + b.evaluate(x, y);
		}

		public String toString() {
			return "Function2d of "+a+" + "+b;
		}
	}
}
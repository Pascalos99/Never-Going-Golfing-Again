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
	
	static ConstantFunction getConstant(double value) {
		return new ConstantFunction(value);
	}

	default double directionalDerivative(double x, double y, Vector2d direction) {
		return direction.normalize().dot(gradient(x, y));
	}

	default Function2d raw_add(Function2d func) {
		return new Function2d(){

			@Override
			public Vector2d gradient(double x, double y) {
				return this.gradient(x, y).add(func.gradient(x, y));
			}

			@Override
			public double evaluate(double x, double y) {
				return this.evaluate(x, y) + func.evaluate(x, y);
			}

			public String toString() {
				return "Function2d of "+this+" + "+func;
			}

		};
	}

	default Function2d add(Function2d func) {
		return raw_add(func);
	}

	default Function2d raw_shift(Vector2d shift) {
		return new ShiftedFunction(this, shift);
	}

	default Function2d shift(Vector2d shift) {
		return raw_shift(shift);
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

		@Override
		public Function2d add(Function2d func) {
			if (func instanceof ConstantFunction)
				return Function2d.getConstant(value + ((ConstantFunction)func).value);
			else if (func instanceof AtomFunction2d)
				return func.add(this);
			return raw_add(func);
		}
		public String toString() {
			return "Constant function with value "+value;
		}
	}

	class ShiftedFunction implements Function2d {
		public final Function2d original;
		public final Vector2d shift;
		public ShiftedFunction(Function2d original, Vector2d shift) {
			this.original = original;
			this.shift = shift;
		}
		@Override
		public Vector2d gradient(double x, double y) {
			return original.gradient(x+shift.get_x(), y+shift.get_y());
		}

		@Override
		public double evaluate(double x, double y) {
			return original.evaluate(x+shift.get_x(), y+shift.get_y());
		}

		public String toString() {
			return "Function2d of "+original+", shifted by "+shift;
		}
	}
}
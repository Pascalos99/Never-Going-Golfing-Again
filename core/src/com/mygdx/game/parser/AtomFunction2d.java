package com.mygdx.game.parser;
import com.mygdx.game.utils.Vector2d;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AtomFunction2d implements Function2d {
	public final Atom function;
	public final Atom derivative_x;
	public final Atom derivative_y;
	Map<String, Double> map;

	public AtomFunction2d(String function) {
		this(FunctionParser.parse(function));
	}
	public AtomFunction2d(Atom function) {
		this.function = function.simplify();
		derivative_x = function.derivate("x").simplify();
		derivative_y = function.derivate("y").simplify();
		map = new HashMap<>();
	}

	@Override
	public Function2d add(Function2d func) {
		if (func instanceof AtomFunction2d) {
			SumAtom at = new SumAtom(this.function, ((AtomFunction2d) func).function);
			return new AtomFunction2d(at);
		}
		return raw_add(func);
	}

	public AtomFunction2d multiply(double factor) {
		return new AtomFunction2d(new ProductAtom(function, new ConstantAtom(factor)));
	}

	public static String randomPolynomial(long seed) {
		Random rand = new Random(seed);
		final double[] poly_f_mins = {0.02, 0.005, 0.001};
		final double[] poly_f_maxs = {2d, 0.5, 0.1};
		final double[] poly_chances_for_x = {0.8, 1, 0.7};
		final double[] poly_chances_for_y = {0.6, 1, 0.5};
		StringBuilder func = new StringBuilder();

		for (int i=0; i < poly_chances_for_x.length; i++) {
			double factor_x = 0;
			if (rand.nextDouble() < poly_chances_for_x[i])
				factor_x = (rand.nextBoolean() ? -1 : 1) * (rand.nextDouble() * (poly_f_maxs[i] - poly_f_mins[i]) + poly_f_mins[i]);
			if (factor_x != 0) func.append(String.format("%.3f * x^%d + ", factor_x, i+1));
		}
		for (int i=0; i < poly_chances_for_y.length; i++) {
			double factor_y = 0;
			if (rand.nextDouble() < poly_chances_for_y[i])
				factor_y = (rand.nextBoolean() ? -1 : 1) * (rand.nextDouble() * (poly_f_maxs[i] - poly_f_mins[i]) + poly_f_mins[i]);
			if (factor_y != 0) func.append(String.format("%.3f * y^%d + ", factor_y, i+1));
		}
		func.append(String.format("%.3f", rand.nextDouble() * 2 + 0.5));

		return func.toString();
	}

	@Override
	public synchronized Vector2d gradient(double x, double y) {
		map.put("x", x);
		map.put("y", y);
		return new Vector2d(derivative_x.apply(map), derivative_y.apply(map));
	}

	@Override
	public synchronized double evaluate(double x, double y) {
		map.put("x", x);
		map.put("y", y);
		return function.apply(map);
	}

	public String toString() {
		return function.toString();
	}

}

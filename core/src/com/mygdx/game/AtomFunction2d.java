package com.mygdx.game;
import java.util.HashMap;
import java.util.Map;

public class AtomFunction2d implements Function2d {
	Atom function;
	Atom derivative_x;
	Atom derivative_y;
	Map<String, Double> map;

	public AtomFunction2d(String function) {
		this(FunctionParser.parse(function));
	}
	public AtomFunction2d(Atom function) {
		this.function = function.simplify();
		derivative_x = function.derivate("x").simplify();
		derivative_y = function.derivate("y").simplify();
		map = new HashMap<String, Double>();
	}

	@Override
	public Vector2d gradient(double x, double y) {
		map.put("x", x);
		map.put("y", y);
		return new Vector2d(derivative_x.apply(map), derivative_y.apply(map));
	}

	@Override
	public double evaluate(double x, double y) {
		map.put("x", x);
		map.put("y", y);
		return function.apply(map);
	}

}

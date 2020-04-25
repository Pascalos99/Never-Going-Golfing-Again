package com.mygdx.game;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class Vector2d {
	private double x, y;
	public Vector2d(double x, double y) {
		this.x = x;
		this.y = y;
	}
	public double get_x() {
		return x;
	}
	public double get_y() {
		return y;
	}
	public double get_length(){
		return Math.sqrt(x * x + y * y);
	}
	/** @return {@code true} when a filled circle with the origin of this vector-point and the given radius would
	 *  partially or fully cover the square at (_x, _y) */
	public boolean is_contained_in(int _x, int _y, double radius) {
		radius += 0.00001;
		Ellipse2D circle = new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2);
		Rectangle square = new Rectangle(_x, _y, 1, 1);
		return circle.intersects(square);
	}

	public Vector2d add(Vector2d v){
		return add(v.get_x(), v.get_y());
	}

	public Vector2d add(double dx, double dy) {
		return new Vector2d(x + dx, y + dy);
	}

	public Vector2d sub(Vector2d v) { return add(-v.get_x(), -v.get_y()); }

	public Vector2d mul(Vector2d v) { return new Vector2d(get_x() * v.get_x(), get_y() * v.get_y()); }

	public Vector2d div(Vector2d v) { return new Vector2d(get_x() / v.get_x(), get_y() / v.get_y()); }

	public Vector2d abs() { return new Vector2d(Math.abs(get_x()), Math.abs(get_y())); }

	public Vector2d normalize() {
		double len = get_length();
		return new Vector2d(x / len, y / len);
	}

	public double dot(Vector2d o) {
		return x * o.x + y * o.y;
	}

	public String toString() {
		return String.format("(%.3f, %.3f)", x, y);
	}

	public Vector2d rotate(double radians){
		double _x = x * Math.cos(radians) - y * Math.sin(radians);
		double _y = x * Math.sin(radians) + y * Math.cos(radians);

		return new Vector2d(_x, _y);
	}

	public double distance(Vector2d o) {
		return Math.sqrt((o.x - x)*(o.x - x) + (o.y - y)*(o.y - y));
	}

}
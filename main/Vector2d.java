package main;

import java.awt.Rectangle;
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
	/** @return {@code true} when a filled circle with the origin of this vector-point and the given radius would
	 *  partially or fully cover the square at (_x, _y) */
	public boolean is_contained_in(int _x, int _y, double radius) {
		radius += 0.00001;
		Ellipse2D circle = new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2);
		Rectangle square = new Rectangle(_x, _y, 1, 1);
		return circle.intersects(square);
	}
	
	public String toString() {
		return String.format("(% f, % f)", x, y);
	}
}
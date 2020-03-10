package main;

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
	 *  partially or fully cover the given point (_x, _y) */
	public boolean is_contained_in(int _x, int _y, double radius) {
		// TODO this is not working as intended, someone please help
		if ((int)(x - radius) <= _x && _x <= (int)(x + radius)
		 && (int)(y - radius) <= _y && _y <= (int)(y + radius)) return true;
		return false;
	}
	
	/*
	 * TODO help:
	 * This code is to test the 'is_contained_in' method, I need that method to test whether a pixel would be a FLAG or not. 
	 * It also needs to reliably give me true when the radius is infinitely small (> 0) and the point is *within* the given pixel (_x, _y)
	 * This would be when the point itself lies within [_x, _y] inclusive and (_x+1, _y+1) exclusive.
	 * 
	public static void main(String[] args) {
		Vector2d vec = new Vector2d(5.5, 4.5);
		double radius = 0.01;
		for (int i=0; i < 10; i++) {
			for (int j=0; j < 10; j++) {
				if (i==0 && j==0) System.out.print(" - ");
				else if (i == 0 && j != 0) System.out.print(" "+j+" ");
				else if (i != 0 && j == 0) System.out.print(" "+i+" ");
				else if (vec.is_contained_in(j, i, radius)) System.out.print(" X ");
				else System.out.print(" O ");
			} System.out.println();
		}
	} */
}
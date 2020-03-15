package main;

public interface Function2d {
	
	/** input is in cm's and output is in cm's */
	public default double evaluate(Vector2d p) {
		return evaluate(p.get_x(), p.get_y());
	}
	
	/** input is in cm's and output is in cm's */
	public Vector2d gradient(Vector2d p);
	
	/**	input is in cm's and output is in cm's */
	public double evaluate(double x, double y);
	
	public static Function2d getConstant(double value) {
		return new Function2d() {
			public double evaluate(double x, double y) {
				return value;
			}
			public Vector2d gradient(Vector2d p) {
				return new Vector2d(0, 0);
			}
		};
	}
	
}
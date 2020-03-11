package main;

public interface Function2d {
	
	/** input is in cm's and output is in cm's */
	public double evaluate(Vector2d p);
	
	/** input is in cm's and output is in cm's */
	public Vector2d gradient(Vector2d p);
	
	/** This should be overriden if possible, because the used method here is not as efficient as it could be.<br>
	 * 	input is in cm's and output is in cm's */
	public default double evaluate(double x, double y) {
		return evaluate(new Vector2d(x, y));
	}
	
}
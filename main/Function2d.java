package main;

public interface Function2d {
	
	public double evaluate(Vector2d p);
	
	public Vector2d gradient(Vector2d p);
	
	/** This should be overriden if possible, because the used method here is not as efficient as it could be. */
	public default double evaluate(double x, double y) {
		return evaluate(new Vector2d(x, y));
	}
	
}
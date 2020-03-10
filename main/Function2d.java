package main;

public interface Function2d {
	
	public double evaluate(Vector2d p);
	
	public Vector2d gradient(Vector2d p);
	
	public double value(Vector2d v);
	
}
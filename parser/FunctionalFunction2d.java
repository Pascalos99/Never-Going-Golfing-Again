package parser;

@FunctionalInterface
public interface FunctionalFunction2d extends Function2d {
	
	public static final double h = 0.0001;
	
	public double evaluate(double x, double y);
	
	@Override
	public default Vector2d gradient(double x, double y) {
		double z = evaluate(x, y);
		return new Vector2d((evaluate(x + h, y) - z) / h, (evaluate(x, y + h) - z) / h);
	}
	
}

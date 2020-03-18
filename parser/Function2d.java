package parser;

public interface Function2d {
	
	public default double evaluate(Vector2d p) {
		return evaluate(p.get_x(), p.get_y());
	}
	
	public default Vector2d gradient(Vector2d p) {
		return gradient(p.get_x(), p.get_y());
	}
	
	public Vector2d gradient(double x, double y);
	
	public double evaluate(double x, double y);
	
	public static Function2d getConstant(double value) {
		return new Function2d() {
			public double evaluate(double x, double y) {
				return value;
			}
			public Vector2d gradient(double x, double y) {
				return new Vector2d(0, 0);
			}
		};
	}
	
}
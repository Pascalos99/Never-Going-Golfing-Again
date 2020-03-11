package golf_map_generator;

public final class Variables {
	/** Any pixel with a friction-coefficient greater than this value will be drawn as 'sand' */
	public static final double 	SAND_FRICTION = 5; // ? TODO decide unit of friction
	public static final double	MOUNTAIN_HEIGHT = 20 * 100; //cm
	public static final double 	HILL_HEIGHT = 10 * 100; //cm
	public static final double 	DEFAULT_FRICTION = 1; // ? TODO decide unit of friction
	public static final int 	DEFAULT_WIDTH = 20 * 100, DEFAULT_HEIGHT = 20 * 100; //cm
	public static final double	DEFAULT_HOLE_TOLERANCE = 0.02; //cm
}

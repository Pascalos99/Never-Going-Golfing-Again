package golf_map_generator;

public final class Variables {
	// MATERIAL SETTINGS
	public static final double 	SAND_FRICTION = 5; // ? TODO decide unit of friction
	public static final double	MOUNTAIN_HEIGHT = 1.4; //cm
	public static final double 	HILL_HEIGHT = 1; //cm
	
	// DEFAULT CONSTRUCTOR OF PUTTINGCOURSE SETTINGS
	public static final double 	DEFAULT_FRICTION = 1; // ? TODO decide unit of friction
	public static final int 	DEFAULT_WIDTH = 20 * 100; //cm
	public static final int 	DEFAULT_HEIGHT = 20 * 100; //cm
	public static final double	DEFAULT_HOLE_TOLERANCE = 0.02; //cm
	public static final double 	DEFAULT_MAXIMUM_VELOCITY = 20; // cm/s ? <- physics people have to decide this
	
	public static final String	FLAG_TEXTURE = ".\\textures\\Flag.png";
	public static final String	START_TEXTURE = ".\\textures\\Start.png";
	
	
	// MAP GENERATION SETTINGS
		// World-border settings
	public static final double 	OUT_OF_BOUNDS_FRICTION = 100000;
	public static final double 	OUT_OF_BOUNDS_HEIGHT = -1; // water surrounds the course
	
		// Settings for path generation
	public static final int 	NUM_FLAG_POS_TRIES = 2000;
	public static final double 	START_PLATFORM_RADIUS = 10;
	public static final double 	FLAG_PLATFORM_RADIUS = 10;
	public static final double 	PATH_RADIUS = 5;
	public static final double 	MIN_FLAG_DISTANCE = 0.5; // percentage of diagonal length of map
	public static final double	PATH_BUMPINESS = 0.2; // a value of 0 is completely smooth, anything above 1 is incredibly noisy (affects height)
	public static final double	PATH_ROUGHNESS = 1.2; // a value of 0 is completely smooth, anything above 1 is incredibly noisy (affects friction)
	
		// Value ranges for fractal generation
	public static final double 	MINIMUM_HEIGHT = -2;
	public static final double 	MAXIMUM_HEIGHT = 2;
	public static final double 	MINIMUM_FRICTION = -1;
	public static final double 	MAXIMUM_FRICTION = 6;
	
		// Settings for valid flag and start positions
	public static final double	MINIMUM_START_HEIGHT = 0; // inclusive
	public static final double	MAXIMUM_START_HEIGHT = 1; // exclusive
	public static final double	MINIMUM_FLAG_HEIGHT = 0; // inclusive
	public static final double	MAXIMUM_FLAG_HEIGHT = 1; // exclusive
	
	public static final double	MINIMUM_START_FRICTION = 0.00001; // inclusive
	public static final double	MAXIMUM_START_FRICTION = 5; // exclusive
	public static final double	MINIMUM_FLAG_FRICTION = 0.00001; // inclusive
	public static final double	MAXIMUM_FLAG_FRICTION = 5; // exclusive
}

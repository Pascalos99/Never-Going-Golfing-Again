package golf_map_generator;

import static golf_map_generator.Material.*;
import static golf_map_generator.Variables.*;
import static golf_map_generator.MapGenUtils.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import main.Function2d;
import main.Vector2d;

public class PuttingCourseGenerator {
	
	Random random;
	Range height_range;
	Range friction_range;
	boolean path_preference;
	
	public PuttingCourseGenerator(long seed, Range height, Range friction, boolean always_lay_paths) {
		random = new Random(seed);
		height_range = height;
		friction_range = friction;
		path_preference = always_lay_paths;
	}
	
	public PuttingCourseGenerator(long seed) {
		this(seed, new Range(MINIMUM_HEIGHT, MAXIMUM_HEIGHT), new Range(MINIMUM_FRICTION, MAXIMUM_FRICTION), true);
	}

	public static int evaluateMaterial(double x, double y, Function2d height, Function2d friction, Vector2d flag, Vector2d start, double hole_tolerance) {
		double z_value = height.evaluate(x, y);
		double f_value = friction.evaluate(x, y);
		if (distance(flag.get_x(), flag.get_y(), x, y) <= hole_tolerance) return FLAG.index;
		if (distance(start.get_x(), start.get_y(), x, y) <= 1) return STARTING_POINT.index;
		if (z_value < 0) return WATER.index;
		if (f_value <= 0) return ICE.index;
		if (f_value >= SAND_FRICTION) return SAND.index;
		if (z_value >= MOUNTAIN_HEIGHT) return MOUNTAIN.index;
		if (z_value >= HILL_HEIGHT) return HILL.index;
		return GRASS.index;
	}
	
	/**
	 * @param desired_size The algorithm will aim to create a course that is a square of this number as its sides (no guarantees sorry)<p>
	 * @param smoothing_factor a factor that determines how much the resulting fractalmap is enlarged to create the final coursemap (this will make the terrain smoother)
	 * <br>A smoothing_factor of 1 or lower means no smoothing/enlarging will occur.<p>
	 * @param roughness_height a factor that detetmines the extend of the inner randomness function in the height-fractal creation (a roughness of 0 will always create a perfectly
	 * smooth surface)<br>This is different from the smoothing_factor as a high roughness will also result in a large amount of tiny ugly deviations which can be mitigated with the
	 * smoothing_factor.<p>
	 * @param roughness_friction a factor that detetmines the extend of the inner randomness function in the friction-fractal creation (a roughness of 0 will always create a perfectly
	 * smooth surface)<br>This is different from the smoothing_factor as a high roughness will also result in a large amount of tiny ugly deviations which can be mitigated with the
	 * smoothing_factor.<p>
	 * @param hole_tolerance
	 * @param maximum_velocity
	 * @return
	 */
	public PuttingCourse fractalGeneratedCourse(int desired_size, int smoothing_factor, double roughness_height, double roughness_friction, double hole_tolerance, double maximum_velocity) {
		if (smoothing_factor < 1) smoothing_factor = 1;
		int small_size_desired = (desired_size / smoothing_factor);
		int detail = approximate_required_detail(small_size_desired);
		double[][] fractal_h = fractalMap(detail, roughness_height);
		double[][] fractal_f = fractalMap(detail, roughness_friction);
		applyRangeToMatrix(fractal_h, height_range);
		applyRangeToMatrix(fractal_f, friction_range);
		double[][] heightmap = enlargeMatrix(fractal_h, smoothing_factor);
		double[][] frictionmap = enlargeMatrix(fractal_f, smoothing_factor);
		Vector2d[] pos = determineFlagAndStartPositions(heightmap, frictionmap);
		Function2d height = functionFromArray(heightmap, Function2d.getConstant(OUT_OF_BOUNDS_HEIGHT));
		Function2d friction = functionFromArray(frictionmap, Function2d.getConstant(OUT_OF_BOUNDS_FRICTION));
		return new PuttingCourse(height, friction, heightmap.length, heightmap[0].length, pos[0], pos[1], hole_tolerance, maximum_velocity);
	}
	
	public PuttingCourse fractalGeneratedCourse(FractalGenerationSettings settings) {
		return fractalGeneratedCourse(settings.desired_size, settings.smoothing_factor, settings.roughness_height, settings.roughness_friction, settings.hole_tolerance, settings.maximum_velocity);
	}
	public PuttingCourse randomCourse(int desired_size, double hole_tolerance, double maximum_velocity) {
		return fractalGeneratedCourse(desired_size, 1, 0.4, 0.6, hole_tolerance, maximum_velocity);
	}
	
	/** Generates a course from a function.<br>This method also adjusts the course to be more playable. */
	public PuttingCourse functionGeneratedCourse(Function2d height, Function2d friction, int course_width_cm, int course_height_cm, double hole_tolerance, double maximum_velocity) {
		PuttingCourse result = new PuttingCourse(height, friction, course_width_cm, course_height_cm, new Vector2d(0, 0), new Vector2d(0, 0), hole_tolerance, maximum_velocity);
		double[][][] maps = generate_height_and_friction_maps(result);
		Vector2d[] pos = determineFlagAndStartPositions(maps[0], maps[1]);
		result.flag_position = pos[0];
		result.start_position = pos[1];
		result.height_function = functionFromArray(maps[0], height);
		result.friction_function = functionFromArray(maps[1], friction);
		return result;
	}
	
	/** Generates a course from a function as per the {@link PuttingCourse#PuttingCourse(Function2d, Function2d, int, int, Vector2d, Vector2d, double, double)} method, but with random
	 * inputs for the start and flag positions. */
	public PuttingCourse pureFunctionGeneratedCourse(Function2d height, Function2d friction, int course_width_cm, int course_height_cm, double hole_tolerance, double maximum_velocity) {
		return new PuttingCourse(height, friction, course_width_cm, course_height_cm,
				new Vector2d(random.nextDouble() * course_width_cm, random.nextDouble() * course_height_cm),
				new Vector2d(random.nextDouble() * course_width_cm, random.nextDouble() * course_height_cm),
			hole_tolerance, maximum_velocity);
	}
	
	private double[][][] generate_height_and_friction_maps(PuttingCourse course) {
		double[][][] result = new double[2][course.course_width][course.course_height];
		Function2d height = course.get_height();
		Function2d friction = course.get_friction();
		for (int x=0; x < result[0].length; x++)
			for (int y=0; y < result[0][x].length; y++) {
				result[0][x][y] = height.evaluate(x, y);
				result[1][x][y] = friction.evaluate(x, y);
			}
		return result;
	}
	
	private Vector2d[] determineFlagAndStartPositions(double[][] heightmap, double[][] frictionmap) {
		int flag_x = 0, flag_y = 0, start_x = 0, start_y = 0, width = heightmap.length, height = heightmap[0].length;
		double max_distance = distance(0, 0, width-1, height-1);
		ArrayList<Integer> flag_try_xy = new ArrayList<Integer>((width / 2) * (height / 2));
		ArrayList<Integer> start_try_xy = new ArrayList<Integer>((width / 2) * (height / 2));
		for (int x=width/10; x < width/2; x++)
			for (int y=height/10; y < height/2; y++) flag_try_xy.add(x*height + y);
		for (int x=width/2; x < (width*9)/10; x++)
			for (int y=height/2; y < (height*9)/10; y++) start_try_xy.add(x*height + y);
		Collections.shuffle(flag_try_xy, random);
		Collections.shuffle(start_try_xy, random);
		
		int okay_flag_pos = flag_try_xy.get(0);
		int okay_start_pos = start_try_xy.get(0);
		
		boolean pos_found = false;
		finding_pos: {
			int tries = 0;
			boolean start_left_upper = true;
			int nums = random.nextInt(2);
			if (nums==1) start_left_upper = false;
			while (nums == 0 || nums == 1) {
				for (int fli = 0; fli < flag_try_xy.size(); fli++) {
					for (int sti = 0; sti < start_try_xy.size(); sti++) {
						if (++tries >= NUM_FLAG_POS_TRIES) break finding_pos;
						int flag = flag_try_xy.get(fli);
						int start = start_try_xy.get(sti);
						flag_x = flag/height;
						flag_y = flag - flag_x * height;
						start_x = start/height;
						start_y = start - start_x * height;
						if (nums==1) { // this is so I can still try every quadrant if the first two quadrants fail
							flag_x = width - flag_x;
							start_x = width - start_x;
						}
						if (distance(flag_x, flag_y, start_x, start_y) < MIN_FLAG_DISTANCE * max_distance) continue;
						double flag_z = heightmap[flag_x][flag_y];
						double flag_f = frictionmap[flag_x][flag_y];
						double start_z = heightmap[start_x][start_y];
						double start_f = frictionmap[start_x][start_y];
						if (flag_z < MINIMUM_FLAG_HEIGHT || flag_z >= MAXIMUM_FLAG_HEIGHT ||
							flag_f < MINIMUM_FLAG_FRICTION || flag_f >= MAXIMUM_FLAG_FRICTION) { fli++; sti--; continue;}
						if (start_z < MINIMUM_START_HEIGHT || start_z >= MAXIMUM_START_HEIGHT ||
							start_f < MINIMUM_START_FRICTION || start_f >= MAXIMUM_START_FRICTION) { continue; }
						okay_flag_pos = flag;
						okay_start_pos = start;
						break finding_pos;
					}
				} if (start_left_upper) nums++; else nums--;
			}}
		if (!pos_found) {
			flag_x = okay_flag_pos / height;
			flag_y = okay_flag_pos - flag_x * height;
			start_x = okay_start_pos / height;
			start_y = okay_start_pos - start_x * height;
			createPath(start_x, start_y, flag_x, flag_y, heightmap, frictionmap);
		} else if (path_preference) {
			createPath(start_x, start_y, flag_x, flag_y, heightmap, frictionmap);
		}
		Vector2d flag = new Vector2d(flag_x + random.nextDouble(), flag_y + random.nextDouble());
		Vector2d start = new Vector2d(start_x + random.nextDouble(), start_y + random.nextDouble());
		return new Vector2d[] {flag, start};
	}
	
	private void createPath(int start_x, int start_y, int flag_x, int flag_y, double[][] heightmap, double[][] frictionmap) {
		double height = MINIMUM_FLAG_HEIGHT + (MAXIMUM_FLAG_HEIGHT - MINIMUM_FLAG_HEIGHT)/2;
		double friction = MINIMUM_FLAG_FRICTION + (MAXIMUM_FLAG_FRICTION - MINIMUM_FLAG_FRICTION)/2;
		double h_angle = 0;
		double f_angle = 0;
		MapGenUtils.brushPaint(flag_x, flag_y, height, FLAG_PLATFORM_RADIUS, heightmap);
		MapGenUtils.brushPaint(flag_x, flag_y, friction, FLAG_PLATFORM_RADIUS, frictionmap);
		MapGenUtils.brushPaint(start_x, start_y, height, START_PLATFORM_RADIUS, heightmap);
		MapGenUtils.brushPaint(start_x, start_y, friction, START_PLATFORM_RADIUS, frictionmap);
		double x_distance = flag_x - start_x;
		double y_distance = flag_y - start_y;
		double current_x = start_x;
		double current_y = start_y;
		while (modulus(x_distance) > FLAG_PLATFORM_RADIUS/4 || modulus(y_distance) > FLAG_PLATFORM_RADIUS/4) {
			double leftover_distance = distance(current_x, current_y, flag_x, flag_y);
			x_distance = flag_x - current_x;
			y_distance = flag_y - current_y;
			h_angle += modulus(random.nextDouble() * PATH_BUMPINESS); double adj_h = 1 + PATH_BUMPINESS * Math.sin(h_angle);
			f_angle += modulus(random.nextDouble() * PATH_ROUGHNESS); double adj_f = 1 + PATH_ROUGHNESS * Math.cos(f_angle);
			MapGenUtils.brushPaint((int)current_x, (int)current_y, height * adj_h, PATH_RADIUS, heightmap);
			MapGenUtils.brushPaint((int)current_x, (int)current_y, friction * adj_f, PATH_RADIUS, frictionmap);
			double ran_y = random.nextDouble(), ran_x = random.nextDouble();
			if (current_x < PATH_RADIUS) ran_x = modulus(ran_x);
			if (current_y < PATH_RADIUS) ran_y = modulus(ran_y);
			if (current_x > heightmap.length - PATH_RADIUS) ran_x = -modulus(ran_x);
			if (current_y > heightmap[0].length - PATH_RADIUS) ran_y = -modulus(ran_y);
			current_x += (x_distance / leftover_distance) * (PATH_RADIUS/2) + ran_x * PATH_RADIUS / 2;
			current_y += (y_distance / leftover_distance) * (PATH_RADIUS/2) + ran_y * PATH_RADIUS / 2;
		}
	}
	
	public void setPathPreference(boolean always_lay_paths) {
		path_preference = always_lay_paths;
	}
	
	public static Function2d functionFromArray(double[][] m, Function2d out_of_bounds_value) {
		return new Function2d() {
			double[][] array = m;
			Function2d function = out_of_bounds_value;
			@Override
			public Vector2d gradient(Vector2d p) {		
				double x = p.get_x();
				double y = p.get_y();
				if (x < 0 || x >= m.length - 1 || y < 0 || y >= m[0].length - 1) return function.gradient(p);
				double A = array[floor(x)][floor(y)];
				double C, D;
				if (floor(x+1) >= m.length) C = function.evaluate(floor(x+1), floor(y));
				else C = array[floor(x+1)][floor(y)];
				if (floor(y+1) >= m.length) D = function.evaluate(floor(x), floor(y+1));
				else D = array[floor(x)][floor(y+1)];
				
				return new Vector2d(C - A, D - A);
			}
			@Override
			public double evaluate(double x, double y) {
				if (x > array.length - 1 || y > array.length - 1 || x < 0 || y < 0) return function.evaluate(x, y);
				if ((float)x == (int)x && (float)y == (int)y) return array[(int)(float)x][(int)(float)y];
				double diff_x = x - floor(x);
				double diff_y = y - floor(y);
				double A = array[floor(x)][floor(y)];
				double B = array[floor(x+1)][floor(y+1)];
				return ((diff_x + diff_y)/2) * (B - A) + A;
			}
		};
	}
	
	private static int approximate_required_detail(int size) {
		int r = 0, t = size;
		while (t > 0) {
			r++;
			t >>= 1; }
		if (r==0) return 1;
		int lower = 1 << (r - 1);
		int higher = 1 << r;
		if (size - lower <= higher - size) return r - 1;
		return r;
	}
	
	public double[][] fractalMap(int detail, double roughness) {
		int divisions = 1 << detail;
		double[][] heatmap = new double[divisions+1][divisions+1];
		heatmap[0][0] = rnd();
		heatmap[0][divisions] = rnd();
		heatmap[divisions][0] = rnd();
		heatmap[divisions][divisions] = rnd();
		double rough = roughness;
		for (int i = 0; i < detail; ++ i) {
		      int r = 1 << (detail - i), s = r >> 1;
		      for (int j = 0; j < divisions; j += r)
		        for (int k = 0; k < divisions; k += r)
		          heatmap = diamond (heatmap,j, k, r, rough);
		      if (s > 0)
		        for (int j = 0; j <= divisions; j += s)
		          for (int k = (j + s) % r; k <= divisions; k += r)
		        	  heatmap = square (heatmap, divisions,j - s, k - s, r, rough);
		      rough *= roughness;
		}
		double min = heatmap[0][0]; double max = min;
	    for (int i = 0; i <= divisions; ++ i)
	      for (int j = 0; j <= divisions; ++ j)
	        if (heatmap[i][j] < min) min = heatmap[i][j];
	        else if (heatmap[i][j] > max) max = heatmap[i][j];
		for (int i=0; i < heatmap.length; i++) {
			for (int j=0; j < heatmap[0].length; j++) {
				double alt = heatmap[i][j];
				heatmap[i][j] = (alt - min) / (max - min);
			}
		}
		return heatmap;
	}
	
	private double[][] diamond (double[][] terrain, int x, int y, int side, double scale) {
	    if (side > 1) {
	      int half = side / 2;
	      double avg = (terrain[x][y] + terrain[x + side][y] +
	        terrain[x + side][y + side] + terrain[x][y + side]) * 0.25;
	      terrain[x + half][y + half] = avg + rnd () * scale;
	    } return terrain;
	}
	    
	private double[][] square (double[][] terrain, int divisions, int x, int y, int side, double scale) {
	    int half = side / 2;
	    double avg = 0.0, sum = 0.0;
	    if (x >= 0)
	    { avg += terrain[x][y + half]; sum += 1.0; }
	    if (y >= 0)
	    { avg += terrain[x + half][y]; sum += 1.0; }
	    if (x + side <= divisions)
	    { avg += terrain[x + side][y + half]; sum += 1.0; }
	    if (y + side <= divisions)
	    { avg += terrain[x + half][y + side]; sum += 1.0; }
	    terrain[x + half][y + half] = avg / sum + rnd () * scale;
	    return terrain;
	}
	private double rnd () {
		return 2. * random.nextDouble () - 1.0;
  	}
	
	static class FractalGenerationSettings {
		public final int desired_size, smoothing_factor;
		public final double roughness_height, roughness_friction, hole_tolerance, maximum_velocity;
		public FractalGenerationSettings(int desired_size, int smoothing_factor, double roughness_height, double roughness_friction, double hole_tolerance, double maximum_velocity) {
			this.desired_size = desired_size;
			this.smoothing_factor = smoothing_factor;
			this.roughness_height = roughness_height;
			this.roughness_friction = roughness_friction;
			this.hole_tolerance = hole_tolerance;
			this.maximum_velocity = maximum_velocity;
		}
	}
}

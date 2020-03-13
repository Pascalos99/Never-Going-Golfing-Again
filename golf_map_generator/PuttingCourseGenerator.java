package golf_map_generator;

import static golf_map_generator.Material.*;
import static golf_map_generator.Variables.*;
import static golf_map_generator.MapGenUtils.*;

import java.util.Random;

import main.Function2d;
import main.Vector2d;

public class PuttingCourseGenerator {
	
	Random random;
	Range height_range;
	Range friction_range;
	
	public PuttingCourseGenerator(long seed, Range height, Range friction) {
		random = new Random(seed);
		height_range = height;
		friction_range = friction;
	}

	/** This method will generate a material heat-map given a height and friction map.<br>
	 * It will therefor not be able to account for obstacles, as they cannot be gathered from this data.<br>
	 * Flag and start positions will also be inserted, given the hole_tolerance is > 0
	 * @return {@code null} if the heat-map sizes do not match up.
	 * @throws RuntimeException if the flag or start positions are not within the bounds of the height and friction maps. */
	public static int[][] generate_materials(double[][] height, double[][] friction, Vector2d flag, Vector2d start, double hole_tolerance) {
		if (height.length != friction.length || height[0].length != friction[0].length) return null;
		int[][] result = new int[height.length][height[0].length];
		boolean start_chosen = false, flag_chosen = false;
		for (int i=0; i < height.length; i++) {
			for (int j=0; j < height[i].length; j++) {
				double z_value = height[i][j];
				double f_value = friction[i][j];
				if (flag.is_contained_in(i, j, hole_tolerance)) { result[i][j] = FLAG.index; flag_chosen = true; }
				else if (start.is_contained_in(i, j, 0.01) && !start_chosen) {
					result[i][j] = STARTING_POINT.index;
					start_chosen = true; }
				else if (z_value < 0) result[i][j] = WATER.index;
				else if (f_value <= 0) result[i][j] = ICE.index;
				else if (f_value >= SAND_FRICTION) result[i][j] = SAND.index;
				else if (z_value >= MOUNTAIN_HEIGHT) result[i][j] = MOUNTAIN.index;
				else if (z_value >= HILL_HEIGHT) result[i][j] = HILL.index;
				else result[i][j] = GRASS.index;
			}
		}
		
		if (!start_chosen) throw new RuntimeException("Start is not contained within the map.");
		if (!flag_chosen) throw new RuntimeException("Flag is not contained within the map.");
		
		return result;
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
		applyRangeToMatrix(fractal_h, height_range); // TODO finallize a better range or make it parametric
		applyRangeToMatrix(fractal_f, friction_range); // TODO finallize a better range or make it parametric
		double[][] heightmap = enlargeMatrix(fractal_h, smoothing_factor);
		double[][] frictionmap = enlargeMatrix(fractal_f, smoothing_factor);
		Function2d height = functionFromArray(heightmap, OUT_OF_BOUNDS_HEIGHT);
		Function2d friction = functionFromArray(frictionmap, OUT_OF_BOUNDS_FRICTION);
		Vector2d[] pos = determineFlagAndStartPositions(heightmap, frictionmap);
		return new PuttingCourse(height, friction, heightmap.length, heightmap[0].length, pos[0], pos[1], hole_tolerance, maximum_velocity);
	}
	
	public PuttingCourse functionGeneratedCourse(Function2d height, Function2d friction, int course_width_cm, int course_height_cm, double hole_tolerance, double maximum_velocity) {
		PuttingCourse result = new PuttingCourse(height, friction, course_width_cm, course_height_cm, new Vector2d(0, 0), new Vector2d(0, 0), hole_tolerance, maximum_velocity);
		Vector2d[] pos = determineFlagAndStartPositions(result.height_map, result.friction_map);
		result.flag_position = pos[0];
		result.start_position = pos[1];
		return result;
	}
	
	private Vector2d[] determineFlagAndStartPositions(double[][] heightmap, double[][] frictionmap) {
		Vector2d flag = new Vector2d(random.nextInt(heightmap.length/2) + heightmap.length/3 + 0.5, random.nextInt(heightmap.length/2) + heightmap.length/3 + 0.5);
		Vector2d start = new Vector2d(heightmap.length/2,0);
		return new Vector2d[] {flag, start};
	}
	
	public static Function2d functionFromArray(double[][] m, double out_of_bounds_value) {
		return new Function2d() {
			double[][] array = m;
			double value = out_of_bounds_value;
			@Override
			public Vector2d gradient(Vector2d p) {
				// TODO improve and test this gradient				
				double x = p.get_x();
				double y = p.get_y();
				
				double A = array[floor(x)][floor(y)];
				double C = array[floor(x+1)][floor(y)];
				double D = array[floor(x)][floor(y+1)];
				
				return new Vector2d(C - A, D - A);
			}
			@Override
			public double evaluate(double x, double y) {
				if (x > array.length - 1 || y > array.length - 1 || x < 0 || y < 0) return value;
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
}

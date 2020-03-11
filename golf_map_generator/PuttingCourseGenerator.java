package golf_map_generator;

import static golf_map_generator.Material.*;
import static golf_map_generator.Variables.*;

import java.util.Random;

import main.Vector2d;

public class PuttingCourseGenerator {
	
	Random random;
	
	public PuttingCourseGenerator(long seed) {
		random = new Random(seed);
	}

	/** This method will generate a material heat-map given a height and friction map.<br>
	 * It will therefor not be able to account for obstacles, as they cannot be gathered from this data.<br>
	 * Flag and start positions will also be inserted, given the hole_tolerance is > 0
	 * @return {@code null} if the heat-map sizes do not match up. */
	public static int[][] generate_materials(double[][] height, double[][] friction, Vector2d flag, Vector2d start, double hole_tolerance) {
		if (height.length != friction.length || height[0].length != friction[0].length) return null;
		int[][] result = new int[height.length][height[0].length];
		for (int i=0; i < height.length; i++) {
			for (int j=0; j < height[i].length; j++) {
				double z_value = height[i][j];
				double f_value = friction[i][j];
				if (flag.is_contained_in(i, j, hole_tolerance)) result[i][j] = FLAG.index;
				else if (start.is_contained_in(i, j, 0.01)) result[i][j] = STARTING_POINT.index; // TODO perfect when the is_contained_in method functions properly
				else if (z_value < 0) result[i][j] = WATER.index;
				else if (f_value <= 0) result[i][j] = ICE.index;
				else if (f_value >= SAND_FRICTION) result[i][j] = SAND.index;
				else if (z_value >= MOUNTAIN_HEIGHT) result[i][j] = MOUNTAIN.index;
				else if (z_value >= HILL_HEIGHT) result[i][j] = HILL.index;
				else result[i][j] = GRASS.index;
			}
		}
		return result;
	}
	
}

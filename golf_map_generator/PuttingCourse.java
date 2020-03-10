package golf_map_generator;

import main.Function2d;
import main.Vector2d;

import static golf_map_generator.Material.*;
import static golf_map_generator.Variables.*;

import java.util.Arrays;

public class PuttingCourse {
	
	double[][] height_map;
	double[][] friction_map;
	int[][] material_map;
	
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
				else result[i][j] = GRASS.index;
			}
		}
		return result;
	}
	
	public PuttingCourse(Function2d height, Vector2d flag, Vector2d start) {
		//TODO implement this
	}
	
	public Function2d get_height() {
		//TODO implement this
		return null;
	}
	
	public Vector2d get_flag_position() {
		//TODO implement this
		return null;
	}
	
	public Vector2d get_start_position() {
		//TODO implement this
		return null;
	}
	
	/* public double get_friction_coefficient() { } */
	
	public double get_maximum_velocity() {
		//TODO implement this
		return 0;
	}
	
	public double get_hole_tolerance() {
		//TODO implement this
		return 0;
	}
	
}

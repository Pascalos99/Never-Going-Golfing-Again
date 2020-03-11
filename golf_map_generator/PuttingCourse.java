package golf_map_generator;

import main.Function2d;
import main.Vector2d;

import static golf_map_generator.Variables.*;

public class PuttingCourse {
	
	double[][] height_map;
	double[][] friction_map;
	int[][] material_map;
	
	Function2d height_function;
	
	public PuttingCourse(Function2d height, Vector2d flag, Vector2d start) {
		height_function = height;
		friction_map = MapGenUtils.fill(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_FRICTION);
		height_map = MapGenUtils.fill(DEFAULT_WIDTH, DEFAULT_HEIGHT, height);
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

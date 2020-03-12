package golf_map_generator;

import main.Function2d;
import main.Vector2d;

import static golf_map_generator.Variables.*;

public class PuttingCourse {
	
	double[][] height_map;
	double[][] friction_map;
	int[][] material_map;
	
	Function2d height_function;
	Function2d friction_function;
	
	Vector2d flag_position;
	Vector2d start_position;
	
	double hole_tolerance;
	double maximum_velocity;
	
	/**
	 * @param height
	 * @param friction
	 * @param course_width_cm
	 * @param course_height_cm
	 * @param flag
	 * @param start
	 * @param hole_tolerance
	 * @param maximum_velocity
	 */
	public PuttingCourse(Function2d height, Function2d friction, int course_width_cm, int course_height_cm, Vector2d flag, Vector2d start, double hole_tolerance, double maximum_velocity) {
		height_function = height;
		friction_function = friction;
		friction_map = MapGenUtils.fill(course_width_cm, course_height_cm, friction);
		height_map = MapGenUtils.fill(course_width_cm, course_height_cm, height);
		flag_position = flag;
		start_position = start;
		this.hole_tolerance = hole_tolerance;
		this.maximum_velocity = maximum_velocity;
		material_map = PuttingCourseGenerator.generate_materials(height_map, friction_map, flag_position, start_position, this.hole_tolerance);
	}
	
	public PuttingCourse(Function2d height, Vector2d flag, Vector2d start) {
		this(height, Function2d.getConstant(DEFAULT_FRICTION), DEFAULT_WIDTH, DEFAULT_HEIGHT, flag, start, DEFAULT_HOLE_TOLERANCE, DEFAULT_MAXIMUM_VELOCITY);
	}
	
	public Material getMaterialAt(int x, int y) {
		return Material.values()[material_map[x][y]];
	}
	/** A more efficient way of retrieving the height value at a given point.<br>Will work for integer points of 1x1 cm*/
	public double getHeightAt(int x, int y) {
		return height_map[x][y];
	}
	/** A more efficient way of retrieving the friction value at a given point.<br>Will work for integer points of 1x1 cm*/
	public double getFrictionAt(int x, int y) {
		return friction_map[x][y];
	}
	
	public int courseWidth() {
		return height_map.length;
	}
	public int courseHeight() {
		return height_map[0].length;
	}
	
	public Function2d get_height() {
		return height_function;
	}
	
	public Function2d get_friction() {
		return friction_function;
	}
	
	/* public double get_friction_coefficient() { } */
	
	public Vector2d get_flag_position() {
		return flag_position;
	}
	
	public Vector2d get_start_position() {
		return start_position;
	}
	
	public double get_maximum_velocity() {
		return maximum_velocity;
	}
	
	public double get_hole_tolerance() {
		return hole_tolerance;
	}
	
}

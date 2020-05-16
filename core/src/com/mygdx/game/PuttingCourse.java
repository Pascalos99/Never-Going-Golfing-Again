package com.mygdx.game;

import static com.mygdx.game.Variables.*;

public class PuttingCourse {

	public Function2d height_function;
	public Function2d friction_function;
	
	public Vector2d flag_position;
	public Vector2d start_position;

	public final double hole_tolerance;
	public final double maximum_velocity;
	public final double gravity;

	public final int course_width, course_height;
	
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
	public PuttingCourse(Function2d height, Function2d friction, int course_width_cm, int course_height_cm, Vector2d flag, Vector2d start, double hole_tolerance, double maximum_velocity, double gravity) {
		height_function = height;
		friction_function = friction;
		flag_position = flag;
		start_position = start;
		this.hole_tolerance = hole_tolerance;
		this.maximum_velocity = maximum_velocity;
		this.course_width = course_width_cm;
		this.course_height = course_height_cm;
		this.gravity = gravity;
	}
	
	public PuttingCourse(Function2d height, Vector2d flag, Vector2d start) {
		this(height, Function2d.getConstant(DEFAULT_FRICTION), DEFAULT_WIDTH, DEFAULT_HEIGHT, flag, start, DEFAULT_HOLE_TOLERANCE, DEFAULT_MAXIMUM_VELOCITY, DEFAULT_GRAVITY);
	}
	
	public Material getMaterialAt(double x, double y) {
		return Material.values()[MapGenUtils.evaluateMaterial(x, y, height_function, friction_function, flag_position, start_position, hole_tolerance)];
	}
	/** A more efficient way of retrieving the height value at a given point.<br>Will work for integer points of 1x1 cm*/
	public double getHeightAt(double x, double y) {
		return height_function.evaluate(x, y);
	}
	/** A more efficient way of retrieving the friction value at a given point.<br>Will work for integer points of 1x1 cm*/
	public double getFrictionAt(double x, double y) {
		return friction_function.evaluate(x, y);
	}
	
	public Vector2d getHeightGradientAt(double x, double y) {
		return height_function.gradient(x, y);
	}
	
	public Vector2d getFrictionGradientAt(double x, double y) {
		return friction_function.gradient(x, y);
	}
	
	public int courseWidth() {
		return course_width;
	}
	public int courseHeight() {
		return course_height;
	}
	
	public Function2d get_height() {
		return height_function;
	}
	
	public Function2d get_friction() {
		return friction_function;
	}
	
	public double get_friction_coefficient() {
		return friction_function.evaluate(0, 0);
	}
	
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
	
	public double get_gravity() {
		return gravity;
	}

}

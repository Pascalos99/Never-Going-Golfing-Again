package com.mygdx.game.courses;

import com.mygdx.game.utils.Vector2d;
import com.mygdx.game.parser.Function2d;

import static com.mygdx.game.utils.Variables.*;

public class PuttingCourse {

	public final Function2d height_function;
	public final Function2d friction_function;
	
	public final Vector2d flag_position;
	public final Vector2d start_position;

	public final double hole_tolerance;
	public final double maximum_velocity;
	public final double gravity;
	
	/**
	 * @param height
	 * @param friction
	 * @param flag
	 * @param start
	 * @param hole_tolerance
	 * @param maximum_velocity
	 */
	public PuttingCourse(Function2d height, Function2d friction, Vector2d flag, Vector2d start, double hole_tolerance, double maximum_velocity, double gravity) {
		height_function = height;
		friction_function = friction;
		flag_position = flag;
		start_position = start;
		this.hole_tolerance = hole_tolerance;
		this.maximum_velocity = maximum_velocity;
		this.gravity = gravity;
	}
	
	public PuttingCourse(Function2d height, Vector2d flag, Vector2d start) {
		this(height, Function2d.getConstant(DEFAULT_FRICTION), flag, start, DEFAULT_HOLE_TOLERANCE, DEFAULT_MAXIMUM_VELOCITY, DEFAULT_GRAVITY);
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

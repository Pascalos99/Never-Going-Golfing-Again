package com.mygdx.game.courses;

import com.mygdx.game.obstacles.Obstacle;
import com.mygdx.game.utils.Vector2d;
import com.mygdx.game.parser.Function2d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mygdx.game.utils.Variables.*;

public class PuttingCourse {

	public final Function2d height_function;
	public final Function2d friction_function;
	
	public final Vector2d flag_position;
	public final Vector2d start_position;

	public final double hole_tolerance;
	public final double maximum_velocity;
	public final double gravity;

	List<Obstacle> obstacles;
	
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
		obstacles = new ArrayList<>();
	}

	/** Identical to the call {@code height_function.evaluate(x, y)}*/
	public double getHeightAt(double x, double y) {
		return height_function.evaluate(x, y);
	}

	/** Identical to the call {@code friction_function.evaluate(x, y)}*/
	public double getFrictionAt(double x, double y) {
		return friction_function.evaluate(x, y);
	}
	
	public double get_friction_coefficient() {
		if (friction_function instanceof Function2d.ConstantFunction)
			return ((Function2d.ConstantFunction)friction_function).value;
		return friction_function.evaluate(0, 0);
	}

	/** @return an unmodifiable list of all obstacles in this course. */
	public List<Obstacle> getObstacles() {
		return Collections.unmodifiableList(obstacles);
	}

}

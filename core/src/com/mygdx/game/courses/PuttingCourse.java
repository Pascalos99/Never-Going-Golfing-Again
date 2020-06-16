package com.mygdx.game.courses;

import com.mygdx.game.obstacles.Obstacle;
import com.mygdx.game.parser.*;
import com.mygdx.game.utils.Vector2d;

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
	PuttingCourse(Function2d height, Function2d friction, Vector2d flag, Vector2d start, double hole_tolerance, double maximum_velocity, double gravity) {
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
		return friction_function.evaluate(0, 0);
	}

	private Function2d sandZero = null;
	public Function2d getSandZeroFunction() {
		if (sandZero == null) {
			SandFunction2d sand = extractSandFunction(friction_function);
			if (sand == null) sandZero = friction_function.sub(Function2d.getConstant(DEFAULT_FRICTION)).shift(WORLD_SHIFT);
			else sandZero = sand.sand.sub(sand.main).shift(WORLD_SHIFT);
		}
		return sandZero;
	}

	private static SandFunction2d extractSandFunction(Function2d func) {
		if (func instanceof SandFunction2d) return (SandFunction2d)func;
		else if (func instanceof AtomFunction2d) return null;
		else if (func instanceof ArrayFunction2d) return null;
		else if (func instanceof FunctionalFunction2d) return null;
		else if (func instanceof Function2d.ModifiedFunction)
			return extractSandFunction(((Function2d.ModifiedFunction) func).original());
		else if (func instanceof Function2d.AdditionFunction2d) {
			SandFunction2d a = extractSandFunction(((Function2d.AdditionFunction2d) func).a);
			if (a != null) return a;
			SandFunction2d b = extractSandFunction(((Function2d.AdditionFunction2d) func).b);
			if (b != null) return b;
		}
		return null;
	}

	/** @return an unmodifiable list of all obstacles in this course. */
	public List<Obstacle> getObstacles() {
		return Collections.unmodifiableList(obstacles);
	}

	public boolean isSandAt(double x, double y) {
		if (friction_function instanceof SandFunction2d) return ((SandFunction2d) friction_function).isSandAt(x, y);
		else return getFrictionAt(x, y) > DEFAULT_FRICTION;
	}

	public void updateGameAspects(GameInfo aspects) {
		aspects.friction = get_friction_coefficient();
		aspects.goalX = flag_position.get_x();
		aspects.goalY = flag_position.get_y();
		aspects.startX = start_position.get_x();
		aspects.startY = start_position.get_y();
		aspects.gravity = gravity;
		aspects.maxVelocity = maximum_velocity;
		aspects.tol = hole_tolerance;
	}

}

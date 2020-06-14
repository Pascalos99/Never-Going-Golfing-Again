package com.mygdx.game.courses;

import com.mygdx.game.obstacles.Obstacle;
import com.mygdx.game.parser.AtomFunction2d;
import com.mygdx.game.parser.Function2d;
import com.mygdx.game.utils.Vector2d;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingFormatArgumentException;

public class CourseBuilder {

    private Function2d height_function;
    private Function2d friction_function;

    private Vector2d start;
    private Vector2d goal;

    public Double hole_tolerance;
    public Double maximum_velocity;
    public Double gravity;

    private Vector2d shift;
    private List<Obstacle> obstacles;

    public CourseBuilder() {
        shift = Vector2d.ZERO;
        obstacles = new ArrayList<>();
    }
    public CourseBuilder(Function2d height, Function2d friction, Vector2d flag, Vector2d start, double hole_tolerance, double maximum_velocity, double gravity) {
        this();
        addHeight(height);
        addFriction(friction);
        setStartAndGoalPos(start, flag);
        setHoleTolerance(hole_tolerance);
        setMaximumVelocity(maximum_velocity);
        setGravity(gravity);
    }

    public CourseBuilder(GameInfo aspects) {
        this(new AtomFunction2d(aspects.getHeightFunction()),
                Function2d.getConstant(aspects.getFriction()), aspects.getGoal(), aspects.getStart(),
                aspects.getTolerance(), aspects.getMaxV(), aspects.getGravity());
    }

    public void addHeight(Function2d func) {
        if (height_function==null) height_function = func;
        else height_function = height_function.add(func);
    }
    public void addFriction(Function2d func) {
        if (friction_function==null) friction_function = func;
        else friction_function = friction_function.add(func);
    }

    public void setStartPos(Vector2d v) {
        start = v;
    }
    public void setGoalPos(Vector2d v) {
        goal = v;
    }
    public void setStartAndGoalPos(Vector2d start, Vector2d goal) {
        setStartPos(start);
        setGoalPos(goal);
    }
    public void addShift(Vector2d shift) {
        this.shift = this.shift.add(shift);
    }

    public void addObstacle(Obstacle obstacle) {
        obstacles.add(obstacle);
    }
    public void removeObstacle(Obstacle obstacle) {
        obstacles.remove(obstacle);
    }
    public void clearObstacles() {
        obstacles.clear();
    }

    public void setHoleTolerance(double value) {
        hole_tolerance = Double.valueOf(value);
    }
    public void setMaximumVelocity(double value) {
        maximum_velocity = Double.valueOf(value);
    }
    public void setGravity(double value) {
        gravity = Double.valueOf(value);
    }

    public void applyHeightScaling(double multiplicative_factor) {
        if (height_function instanceof AtomFunction2d)
            height_function = ((AtomFunction2d)height_function).multiply(multiplicative_factor);
        else height_function = height_function.scale(multiplicative_factor);
    }

    public PuttingCourse get() throws MissingFormatArgumentException {
        if (height_function == null || friction_function == null ||
            start == null || goal == null || hole_tolerance == null ||
            maximum_velocity == null || gravity == null)
            throw new MissingFormatArgumentException("Not all required arguments have been given a value");
        if (!shift.equals(Vector2d.ZERO)) {
            start = start.add(shift);
            goal = goal.add(shift);
            height_function = height_function.shift(shift);
            friction_function = friction_function.shift(shift);
            for (Obstacle o : obstacles) o.setAnchorPoint(o.getAnchorPoint().add(shift));
            shift = Vector2d.ZERO;
        }
        PuttingCourse course = new PuttingCourse(height_function, friction_function,
                goal, start, hole_tolerance, maximum_velocity, gravity);
        for (Obstacle o : obstacles) course.obstacles.add(o);
        return course;
    }

    // TODO add functionality of adding obstacles to the course
    // (add functionality of generating start and goal positions based on course)
    // (add functionality of generating path between start and goal)

}

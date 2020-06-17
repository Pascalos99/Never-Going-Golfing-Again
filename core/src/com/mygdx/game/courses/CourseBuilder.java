package com.mygdx.game.courses;

import com.mygdx.game.obstacles.Obstacle;
import com.mygdx.game.obstacles.Tree;
import com.mygdx.game.obstacles.Wall;
import com.mygdx.game.parser.AtomFunction2d;
import com.mygdx.game.parser.Function2d;
import com.mygdx.game.parser.SandFunction2d;
import com.mygdx.game.utils.Variables;
import com.mygdx.game.utils.Vector2d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.MissingFormatArgumentException;

public class CourseBuilder {

    Function2d height_function;
    Function2d friction_function;

    Vector2d start;
    Vector2d goal;

    Double hole_tolerance;
    Double maximum_velocity;
    Double gravity;

    Vector2d shift;
    List<Obstacle> obstacles;

    public CourseBuilder() {
        shift = Vector2d.ZERO;
        obstacles = new ArrayList<>();
    }

    public CourseBuilder(GameInfo aspects) {
        this();
        loadInfo(aspects);
    }

    public void loadInfo(GameInfo aspects) {
        addHeight(new AtomFunction2d(aspects.getHeightFunction()));
        setStartAndGoalPos(aspects.getStart(), aspects.getGoal());
        setHoleTolerance(aspects.getTolerance());
        setMaximumVelocity(aspects.getMaxV());
        setGravity(aspects.getGravity());
        setSandFunction(new AtomFunction2d(aspects.getSandFunction()), aspects.friction, aspects.sandFriciton);
    }

    public void addHeight(Function2d func) {
        if (height_function==null) height_function = func;
        else height_function = height_function.add(func);
        if (friction_function instanceof SandFunction2d) {
            SandFunction2d s = (SandFunction2d)friction_function;
            friction_function = new SandFunction2d(s.default_friction, s.sand_friction, height_function, s.sand);
        }
    }

    public void setSandFunction(Function2d func, double friction, double sand_friction) {
        Function2d main = height_function;
        if (height_function == null) main = Function2d.getConstant(0);
        friction_function = new SandFunction2d(friction, sand_friction, main, func);
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

    public void addObstacles(Collection<Obstacle> o) {
        obstacles.addAll(o);
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

    public void addTree(Vector2d position, double height, double radius) {
        addObstacle(new Tree(position, height, radius));
    }
    public void addSmallTree(Vector2d position) {
        Tree t = new Tree(position, Tree.HEIGHT_SMALL, Tree.HEIGHT_SMALL / Tree.H_R_RATIO);
        t.texture_used = Tree.TEXTURE_SMALL;
        addObstacle(t);
    }
    public void addMediumTree(Vector2d position) {
        Tree t = new Tree(position, Tree.HEIGHT_MEDIUM, Tree.HEIGHT_MEDIUM / Tree.H_R_RATIO);
        t.texture_used = Tree.TEXTURE_MEDIUM;
        addObstacle(t);
    }
    public void addLargeTree(Vector2d position) {
        Tree t = new Tree(position, Tree.HEIGHT_LARGE, Tree.HEIGHT_LARGE / Tree.H_R_RATIO);
        t.texture_used = Tree.TEXTURE_LARGE;
        addObstacle(t);
    }

    public void addWall(Vector2d from, Vector2d to, double thickness) {
        addObstacle(new Wall(from, to,thickness));
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

    boolean isEverythingSpecified() {
        return !(height_function == null || friction_function == null ||
                start == null || goal == null || hole_tolerance == null ||
                maximum_velocity == null || gravity == null);
    }

    void applyShift() {
        if (!shift.equals(Vector2d.ZERO)) {
            start = start.add(shift);
            goal = goal.add(shift);
            height_function = height_function.shift(shift);
            friction_function = friction_function.shift(shift);
            for (Obstacle o : obstacles) o.setAnchorPoint(o.getAnchorPoint().add(shift));
            shift = Vector2d.ZERO;
        }
    }

    public PuttingCourse getModified() throws MissingFormatArgumentException {
        addShift(Variables.WORLD_SHIFT);
        applyHeightScaling(1 / Variables.WORLD_SCALING);
        return get();
    }

    public PuttingCourse get() throws MissingFormatArgumentException {
        if (!isEverythingSpecified()) throw new MissingFormatArgumentException(
                "Not all required arguments have been given a value");
        applyShift();
        PuttingCourse course = new PuttingCourse(height_function, friction_function,
                goal, start, hole_tolerance, maximum_velocity, gravity);
        for (Obstacle o : obstacles) course.obstacles.add(o);
        return course;
    }

    /** modifying this list also modifies the original course builder, but not the resulting course(s). */
    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    // TODO add functionality of adding obstacles to the course
    // TODO move random course generation to this class
    // (add functionality of generating start and goal positions based on course)
    // (add functionality of generating path between start and goal)

}

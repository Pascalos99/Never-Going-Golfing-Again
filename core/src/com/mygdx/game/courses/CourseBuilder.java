package com.mygdx.game.courses;

import com.mygdx.game.obstacles.Obstacle;
import com.mygdx.game.obstacles.Tree;
import com.mygdx.game.obstacles.Wall;
import com.mygdx.game.parser.*;
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
        addHeight(aspects.getHeightFunction());
        setStartAndGoalPos(aspects.getStart(), aspects.getGoal());
        setHoleTolerance(aspects.getTolerance());
        setMaximumVelocity(aspects.getMaxV());
        setGravity(aspects.getGravity());
        setSandFunction(new AtomFunction2d(aspects.getSandFunction()), aspects.friction, aspects.sandFriciton);
    }

    public void addHeight(String raw_func) {
        if (raw_func.matches("\\s*fractal\\[?.*]?\\s*")) {
            double roughness = 0.5;
            BiLinearArrayFunction2d func = new FractalGenerator(System.currentTimeMillis()).biLinearFractal(
                4000, 1, roughness, Variables.BOUNDED_WORLD_SIZE + 1,
                    -10, 15, Variables.OUT_OF_BOUNDS_HEIGHT);
            func.setShift(Vector2d.ZERO.sub(Variables.WORLD_SHIFT));
            addHeight(func);
        }
        else addHeight(new AtomFunction2d(raw_func));
    }

    public void addHeight(Function2d func) {
        if (height_function==null) height_function = func;
        else height_function = height_function.add(func);
        updateSandFunction();
    }

    private void updateSandFunction() {
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

    public boolean isStartInWater() {
        return height_function.evaluate(start) < 0;
    }
    public boolean isFlagInWater() {
        return height_function.evaluate(goal) < 0;
    }
    public String getStartFlagWaterString() {
        if (!isFlagInWater() && !isStartInWater()) return "";
        return String.format("Warning:\nYour %s in water.\nYou should move the %s to\n make the game playable",
                isFlagInWater()&&(!isStartInWater())?"flag position is":(isFlagInWater()?"flag and start position are":"start position is"),
                isFlagInWater()&&(!isStartInWater())?"flag position":(isFlagInWater()?"flag and start position":"start position"));
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
        if (temp_wall != null) endWall();
        addObstacle(new Wall(from, to,thickness));
    }

    public TempWall temp_wall = null;

    public void startWall(Vector2d start, double thickness) {
        if (temp_wall != null) endWall();
        temp_wall = new TempWall(start, thickness);
    }
    public void updateWall(Vector2d temp_end, double thickness) {
        temp_wall.end = temp_end;
        temp_wall.thickness = thickness;
    }
    public void endWall(Vector2d end, double thickness) {
        updateWall(end, thickness); endWall();
    }
    public void endWall() {
        obstacles.add(temp_wall.get());
        temp_wall = null;
    }
    public void cancelWall() {
        temp_wall = null;
    }
    public boolean isBuildingWall() {
        return temp_wall != null;
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
        System.out.println(height_function);
        return course;
    }

    /** modifying this list also modifies the original course builder, but not the resulting course(s). */
    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    public void setFractalHeight(long seed, double roughness, String resolution_setting, String smoothness_setting, double min_value, double max_value) {
        int resolution = 4000;
        switch(resolution_setting) {
            case("Low"): resolution = 2000; break;
            case("High"): resolution = 6000; break;
        } int smoothness = 1;
        switch(smoothness_setting) {
            case("Low"): smoothness = 2; break;
            case("Medium"): smoothness = 4; break;
            case("High"): smoothness = 8; break;
        }
        height_function = new FractalGenerator(seed).biLinearFractal(resolution, smoothness, roughness,
                Variables.BOUNDED_WORLD_SIZE + 1, min_value, max_value, Variables.OUT_OF_BOUNDS_HEIGHT);
        ((ArrayFunction2d)height_function).setShift(Vector2d.ZERO.sub(Variables.WORLD_SHIFT));
        updateSandFunction();
    }

    // (add functionality of generating start and goal positions based on course)
    // (add functionality of generating path between start and goal)

}

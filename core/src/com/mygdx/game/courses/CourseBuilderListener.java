package com.mygdx.game.courses;

import com.mygdx.game.obstacles.Obstacle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CourseBuilderListener {
    CourseBuilder cb;
    public static final int UPDATE_HEIGHT = 0;
    public static final int UPDATE_FRICTION = 1;
    public static final int UPDATE_START = 2;
    public static final int UPDATE_GOAL = 3;
    public static final int UPDATE_SHIFT = 4;
    public static final int UPDATE_TEMP_WALL = 5;
    public static final int UPDATE_OBSTACLES = 6;
    public static final int ADD_OBSTACLE = 7;

    private boolean[] update_values;
    List<Obstacle> obstacles;

    public CourseBuilderListener() {
        this(false);
    }
    public CourseBuilderListener(boolean start_setting) {
        obstacles = new ArrayList<>();
        update_values = new boolean[8];
        setAll(start_setting);
    }

    public void setAll(boolean set) {
        for (int i=0; i < update_values.length; i++) update_values[i] = set;
    }
    public void reset() { setAll(false); }

    public void setCourse(CourseBuilder course) {
        cb = course;
    }
    public CourseBuilder getBuilder() {
        return cb;
    }
    void notify(int update_setting) {
        update_values[update_setting] = true;
    }
    public boolean peek(int update_setting) {
        return update_values[update_setting];
    }
    public boolean reset(int update_setting) {
        if (update_values[update_setting]) {
            update_values[update_setting] = false;
            return true;
        } return false;
    }
    public void set(int update_setting, boolean set) {
        update_values[update_setting] = set;
    }
    public List<Obstacle> consumeObstacles() {
        List<Obstacle> result = new ArrayList<>(obstacles);
        obstacles.clear();
        return result;
    }

    public String toString() {
        return "CBL: "+ Arrays.toString(update_values);
    }

}

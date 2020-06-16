package com.mygdx.game.courses;

import com.mygdx.game.utils.Vector2d;

import java.io.*;
import static com.mygdx.game.utils.Variables.*;

public class IO_course_module {

    private BufferedReader reader;

    public IO_course_module(String path) {
        try {
            reader = new BufferedReader(new FileReader(path));
            String[] pre = preprocess();
            readValues(pre);
        } catch (IOException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static String default_courses_path = "core//testCourses//";

    public static boolean isDefaultCourseName(String courseName) {
        File folder = new File(default_courses_path);
        File[] files = folder.listFiles();
        for (int i=0; i < files.length; i++) {
            if (files[i].getName().equals(courseName)) return true;
        } return false;
    }

    private String[] preprocess() throws IOException {
        String result = "";
        while (reader.ready())
            result += strip(reader.readLine()) + "\n";
        result = result.replaceAll("//.*\n?", ";");
        return result.split(" *[;\n] *");
    }

    private String strip(String str) {
        int i=0, j = str.length();
        while (str.substring(i, i+1).matches("\\s")) i++;
        while (str.substring(j-1, j).matches("\\s")) j--;
        return str.substring(i, j);
    }

    private void readValues(String[] pre) throws NumberFormatException, IllegalArgumentException, IllegalAccessException {
        for (int i=0; i < pre.length; i++) {
            if (pre[i].equals("")) continue;
            String[] setting = pre[i].split(" *= *");
            if (setting.length <= 1) continue;
            String name = setting[0]; String value = setting[1];
            if (name.equals("g")) g = Double.parseDouble(value);
            if (name.equals("m")) m = Double.parseDouble(value);
            if (name.equals("mu")) mu = Double.parseDouble(value);
            if (name.equals("mu_sand")) mu_sand = Double.parseDouble(value);
            if (name.equals("vmax")) vmax = Double.parseDouble(value);
            if (name.equals("tol")) tol = Double.parseDouble(value);
            if (name.equals("start")) start = parseVector2d(value);
            if (name.equals("goal")) goal = parseVector2d(value);
            if (name.equals("height")) height = value;
            if (name.equals("sand")) sand = value;
        }
    }

    private Vector2d parseVector2d(String input) {
        // something like "( 123 , 2 )" or "(1,2)" or "( 1, 002)", etc. (below)
        if (!input.matches("\\(\\s*\\d+(\\.\\d+)?\\s*,\\s*\\d+(\\.\\d+)?\\s*\\)")) throw new IllegalArgumentException("Argument is not a vector");
        String[] numbers = input.replaceAll("[\\(\\)\\s+]", "").split(",");
        return new Vector2d(Double.parseDouble(numbers[0]), Double.parseDouble(numbers[1]));
    }

    private double g = DEFAULT_GRAVITY;
    private double m = DEFAULT_MASS;
    private double mu = DEFAULT_FRICTION;
    private double mu_sand = DEFAULT_SAND_FRICTION;
    private double vmax = DEFAULT_MAXIMUM_VELOCITY;
    private double tol = DEFAULT_HOLE_TOLERANCE;
    private Vector2d start = new Vector2d(0, 0);
    private Vector2d goal = new Vector2d(5, 5);
    private String height = "sin(x) + cos(y)";
    private String sand = "sin(x-1) + cos(y-1)";

    public String toString() {
        return String.format("g = %s\nm = %s\nmu = %s\nmu_sand = %s\nvmax = %s\ntol = %s\nstart = %s\ngoal = %s\nheight = %s\nsand = %s",
                g, m, mu, mu_sand, vmax, tol, start, goal, height, sand);
    }

    public double getGravity(){
        return g;
    }

    public double getMassofBall(){
        return m;
    }

    public double getFrictionc(){
        return mu;
    }

    public double getSandFrictionc() { return mu_sand; }

    public double getMaxV(){
        return vmax;
    }

    public double getTolerance(){
        return tol;
    }

    public double getStartX(){
        return start.get_x();
    }

    public double getStartY(){
        return start.get_y();
    }

    public double getGoalX(){
        return goal.get_x();
    }
    public double getGoalY(){
        return goal.get_y();
    }

    public String getHeightFunction(){
        return height;
    }

    public String getSandFunction() { return sand;}

    public static String vector_to_string(Vector2d v) {
        return "("+v.get_x()+", "+v.get_y()+")";
    }

    public static void outputFile(File file, double g, double m, double mu, double mu_sand, double vmax, double tol, Vector2d start, Vector2d goal, String height, String sand) {
        String start_str = vector_to_string(start);
        String goal_str = vector_to_string(goal);
        PrintWriter writer;
        try {
            writer = new PrintWriter(new FileWriter(file));
            writer.format("g = %s; m = %s; mu = %s; mu_sand = %s; vmax = %s; tol = %s;\nstart = %s; goal = %s;\nheight = %s; sand = %s ",
                    g, m, mu, mu_sand, vmax, tol, start_str, goal_str, height, sand);
            writer.close();
        } catch (IOException e) {
            System.err.println("Given file is invalid");
            e.printStackTrace();
        }
    }

}

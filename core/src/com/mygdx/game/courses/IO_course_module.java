package com.mygdx.game.courses;

import com.mygdx.game.obstacles.Obstacle;
import com.mygdx.game.obstacles.Tree;
import com.mygdx.game.obstacles.Wall;
import com.mygdx.game.utils.Vector2d;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mygdx.game.utils.Variables.*;

public class IO_course_module {

    private BufferedReader reader;
    private String path;

    public IO_course_module(String path) {
        this.path = path;
        obstacles = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(path));
            List<String> clumped_pre = clumpCompounds(preprocess());
            readValues(clumped_pre);
        } catch (IOException | IllegalArgumentException e) {
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
        while (reader.ready()) {
            result += strip(reader.readLine()) + "\n"; // removing spaces at the begin and end of each line
        }
        result = result.replaceAll("//.*\n?", ";"); // removing all comments
        return result.split(" *[;\n] *"); // splitting per ';' and linebreak
    }

    private List<String> clumpCompounds(String[] pre) {
        List<String> result = new ArrayList<>(pre.length);
        boolean in_compound = false;
        String compound = "";
        for (int i=0; i < pre.length; i++) {
            String part = pre[i];
            boolean is_closing = part.contains("}");
            if (part.contains("{") && !is_closing) {
                if (in_compound) throw new IllegalArgumentException("File ("+path+") format invalid, compound {...} should not contain further compounds");
                in_compound = true;
                compound = pre[i];
            }
            else if (in_compound && !is_closing)
                compound += "; "+pre[i];
            else if (in_compound && is_closing) {
                compound += "; "+pre[i];
                in_compound = false;
                result.add(compound);
            } else result.add(pre[i]);
        }
        return result;
    }

    private String strip(String str) {
        int i=0, j = str.length();
        while (i < str.length() && str.substring(i, i+1).matches("\\s")) i++;
        while (j > 0 && str.substring(j-1, j).matches("\\s")) j--;
        return str.substring(i, j);
    }

    private void readValues(List<String> clumped_pre) throws IllegalArgumentException {
        FractalInfo.FractalInfoBuilder fri = FractalInfo.getEmpty();
        for (int i=0; i < clumped_pre.size(); i++) {
            if (clumped_pre.get(i).equals("")) continue;
            String[] setting = clumped_pre.get(i).split(" *= *", 2);
            if (setting.length <= 1) continue;
            String name = setting[0]; String value = setting[1];
            if (name.equals("g")) g = Double.parseDouble(value);
            if (name.equals("m")) m = Double.parseDouble(value);
            if (name.equals("mu")) mu = Double.parseDouble(value);
            if (name.equals("mu-sand")) mu_sand = Double.parseDouble(value);
            if (name.equals("vmax")) vmax = Double.parseDouble(value);
            if (name.equals("tol")) tol = Double.parseDouble(value);
            if (name.equals("start")) start = parseVector2d(value);
            if (name.equals("goal")) goal = parseVector2d(value);
            if (name.equals("height")) height = value;
            if (name.equals("sand")) sand = value;
            if (name.equals("tree")) obstacles.add(parseTree(value.replaceAll("(\\{[;\\s]*)|([;\\s]*})", "")));
            if (name.equals("wall")) obstacles.add(parseWall(value.replaceAll("(\\{[;\\s]*)|([;\\s]*})", "")));
            if (name.equals("using_fractals")) using_fractals = Boolean.parseBoolean(value);
            if (name.equals("seed")) fri.addSeed(Long.parseLong(value));
            if (name.equals("roughness")) fri.addRoughness(Double.parseDouble(value));
            if (name.equals("minimum")) fri.addMin(Double.parseDouble(value));
            if (name.equals("maximum")) fri.addMax(Double.parseDouble(value));
            if (name.equals("resolution")) fri.addResolution(value);
            if (name.equals("smoothness")) fri.addResolution(value);
            if (name.equals("interpolation")) fri.addResolution(value);
            if (name.equals("world-shift")) world_shift = parseVector2d(value);
        }
        if (using_fractals) fractal = fri.get();
    }

    private Obstacle parseTree(String compound) {
        String[] settings = compound.split("\\s*;\\s*");
        double h = Tree.HEIGHT_MEDIUM;
        double r = Tree.HEIGHT_MEDIUM / Tree.H_R_RATIO;
        Vector2d pos = Vector2d.ZERO;
        for (int i=0; i < settings.length; i++) {
            String[] setting = settings[i].split("\\s*=\\s*");
            if (setting[0].equals("pos")) pos = parseVector2d(setting[1]);
            if (setting[0].equals("h")) h = Double.parseDouble(setting[1]);
            if (setting[0].equals("r")) r = Double.parseDouble(setting[1]);
        }
        return new Tree(pos, h, r);
    }
    private Obstacle parseWall(String compound) {
        String[] settings = compound.split("\\s*;\\s*");
        Vector2d from = Vector2d.ZERO;
        Vector2d to = Vector2d.ZERO;
        double d = 0;
        for (int i=0; i < settings.length; i++) {
            String[] setting = settings[i].split("\\s*=\\s*");
            if (setting[0].equals("from")) from = parseVector2d(setting[1]);
            if (setting[0].equals("to")) to = parseVector2d(setting[1]);
            if (setting[0].equals("d")) d = Double.parseDouble(setting[1]);
        }
        return new Wall(from, to, d);
    }

    private Vector2d parseVector2d(String input) {
        // something like "( 123 , 2 )" or "(1,2)" or "( 1, 002)", etc. (below)
        if (!input.matches("\\(\\s*-?\\s*\\d+(\\.\\d+)?\\s*,\\s*-?\\s*\\d+(\\.\\d+)?\\s*\\)")) throw new IllegalArgumentException("Argument is not a vector");
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
    private boolean using_fractals = false;
    private FractalInfo fractal = null;
    private Vector2d world_shift = Vector2d.ZERO;

    private List<Obstacle> obstacles;

    public String toString() {
        return String.format("g = %s\nm = %s\nmu = %s\nmu-sand = %s\nvmax = %s\ntol = %s\nstart = %s\ngoal = %s\nheight = %s\nsand = %s\nobstacles = %s",
                g, m, mu, mu_sand, vmax, tol, start, goal, height, sand, obstacles);
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

    public List<Obstacle> getObstacles() { return obstacles; }

    public boolean useFractals() {
        return using_fractals;
    }
    public FractalInfo getFractalInfo() {
        return fractal;
    }

    public Vector2d getWorldShift() {
        return world_shift;
    }

    public static String vector_to_string(Vector2d v) {
        return String.format("(% .4f, % .4f)", v.get_x(), v.get_y());
    }

    private static String obstacle_to_string(Obstacle o) {
        if (o instanceof Wall) {
            Wall w = (Wall)o;
            return String.format("wall = { from = %s; to = %s; d = % .2f }", vector_to_string(w.getStart()), vector_to_string(w.getEnd()), w.getThickness());
        } else if (o instanceof Tree) {
            Tree t = (Tree)o;
            Vector2d pos = new Vector2d(t.getPhysicsPosition().get_x(), t.getPhysicsPosition().get_z());
            return String.format("tree = { pos = %s; h = % .2f; r = % .2f }", vector_to_string(pos), t.getHeight(), t.getRadius());
        }
        return "";
    }

    private static String toOutputString(GameInfo info) {
        String start_str = vector_to_string(info.getStart());
        String goal_str = vector_to_string(info.getGoal());
        String shift_str = vector_to_string(WORLD_SHIFT);
        StringBuilder sb = new StringBuilder(String.format("g = %s; m = %s; mu = %s; mu-sand = %s; vmax = %s; tol = %s;" +
                        "\nstart = %s; goal = %s; world-shift = %s;\nheight = %s; sand = %s\n" +
                        "using_fractals = %s;\n",
                info.gravity, info.ballMass, info.friction, info.sandFriciton, info.maxVelocity, info.tol, start_str, goal_str, shift_str,
                info.getHeightFunction(), info.getSandFunction(), info.use_fractals));
        if (info.use_fractals) {
            FractalInfo f = info.fractalInfo;
            sb.append(String.format("seed = %d; roughness = %f;\nminimum = %f; maximum = %f\n" +
                    "resolution = %s; smoothness = %s;\ninterpolation = %s;", f.seed, f.roughness,
                    f.minimum, f.maximum, f.resolution_setting, f.smoothness_setting, f.interpolation_setting));
        }
        return sb.toString();
    }

    private static void outputToFile(File file, String... str) {
        if (isDefaultCourseName(file.getName())) file = new File(default_courses_path+file.getName());
        PrintWriter writer;
        try {
            writer = new PrintWriter(new FileWriter(file));
            for (int i=0; i < str.length; i++) writer.println(str[i]);
            writer.close();
        } catch (IOException e) {
            System.err.println("Given file ("+file+") is invalid");
            e.printStackTrace();
        }
    }

    public static void outputFile(File file, GameInfo aspects, List<Obstacle> obstacles) {
        String[] str = new String[obstacles.size()+1];
        str[0]=toOutputString(aspects);
        for (int i=1; i < str.length; i++) str[i] = obstacle_to_string(obstacles.get(i-1));
        outputToFile(file, str);
    }

    public static void outputFile(File file, GameInfo info) {
        outputToFile(file, toOutputString(info));
    }

}

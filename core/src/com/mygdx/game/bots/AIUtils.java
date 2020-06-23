package com.mygdx.game.bots;

import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.Ball;
import com.mygdx.game.courses.PuttingCourse;
import com.mygdx.game.obstacles.Obstacle;
import com.mygdx.game.courses.PuttingCourse;
import com.mygdx.game.obstacles.Obstacle;
import com.mygdx.game.parser.AtomFunction2d;
import com.mygdx.game.parser.Function2d;
import com.mygdx.game.physics.PuttingCoursePhysics;
import com.mygdx.game.utils.Variables;
import com.mygdx.game.utils.Vector2d;
import com.mygdx.game.utils.Vector3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class AIUtils {
    public static final int OPTIMAL_RESOLUTION = 2000;
    public static final double ROOF = Double.POSITIVE_INFINITY;

    public static double linearInterpolate(double a, double b, double t){
        return a + (b - a) * t;
    }

    public static double[] linearSpacing(double start, double end, int length) {
        double[] result = new double[length];
        double interval = (end - start)/((double)length);
        for (int i=0; i < result.length; i++)
            result[i] = start + i * interval;
        return result;
    }

    public static Vector2d findLowestGradient(Function2d h, int parts){
        Vector2d xy = null;
        double total_gradient = 0;
        int steps = parts;

        for(double i = 0d; i < 1d; i += 1d/((double) steps)){
            double x = linearInterpolate(0, 50, i);

            for(double j = 0d; j < 1d; j += 1d/((double) steps)){
                double y = linearInterpolate(0, 50, j);
                double height = h.evaluate(x, y);

                if(height > 0){

                    if(xy == null){
                        xy = new Vector2d(x, y);
                        Vector2d gradient = h.gradient(x, y).abs();
                        total_gradient = gradient.get_x() + gradient.get_y();
                    }

                    else{
                        Vector2d new_xy = new Vector2d(x, y);
                        Vector2d new_gradient = h.gradient(x, y).abs();
                        double new_total_gradient = new_gradient.get_x() + new_gradient.get_y();

                        if(new_total_gradient < total_gradient){
                            xy = new_xy;
                            total_gradient = new_total_gradient;
                        }

                    }

                }

            }

        }

        return h.gradient(xy);
    }

    public static double unfoldDistance(Vector2d a, Vector2d b, Function2d h, int steps){
        double distance = 0d;
        Vector2d prev = a;

        for(double i = 1d/((double) steps); i <= 1d; i += 1d/((double) steps)){
            Vector2d curr = new Vector2d(
                    linearInterpolate(a.get_x(), b.get_x(), i),
                    linearInterpolate(a.get_y(), b.get_y(), i)
            );
            double prev_h = h.evaluate(prev);
            double curr_h = h.evaluate(curr);

            Vector3 curr_vec3 = new Vector3(((float)curr.get_x()), (float)curr_h, ((float)curr.get_y()));
            Vector3 prev_vec3 = new Vector3(((float)prev.get_x()), (float)prev_h, ((float)prev.get_y()));

            distance += curr_vec3.dst(prev_vec3);
            prev = curr;
        }

        return  distance;
    }

    public static boolean[][] convertPointsToArray(Collection<Vector2d> points, double world_bound, int resolution) {
        boolean[][] array = new boolean[resolution][resolution];
        for (Vector2d v : points) {
            double transmuted_x = (v.get_x() / world_bound) * (array.length - 1);
            double transmuted_y = (v.get_y() / world_bound) * (array[0].length - 1);
            int i = (int)Math.round(transmuted_x), j = (int)Math.round(transmuted_y);
            array[i][j] = true;
        }
        return array;
    }

    public static Vector2d convertIndexToReal(Vector2d index, int width, int height, double world_bound) {
        return new Vector2d(
                index.get_x() * world_bound / (width - 1),
                index.get_y() * world_bound / (height - 1));
    }
    public static Vector2d convertIndexToReal(Vector2d index, boolean[][] array, double world_bound) {
        return convertIndexToReal(index, array.length, array[0].length, world_bound);
    }

    public static Vector2d getClosestValidArrayIndex(Vector2d point, boolean[][] array, double world_bound) {
        double transmuted_x = (point.get_x() / world_bound) * (array.length - 1);
        double transmuted_y = (point.get_y() / world_bound) * (array[0].length - 1);
        int index_x = (int)Math.round(transmuted_x), index_y = (int)Math.round(transmuted_y);
        if (array[index_x][index_y]) return new Vector2d(index_x, index_y);
        int cx = (transmuted_x < index_x)?1:-1;
        int cy = (transmuted_y < index_y)?1:-1;
        int depth = 1;
        while (true) {
            int end = depth*2 + 1;
            for (int i=0; i < end; i++) {
                int x = index_x - depth*cx + cx*i;
                if (x < 0 || x >= array.length) continue;
                if (i==0 || i == end-1) {
                    for (int y=-depth * cy; y <= depth * cy; y += cy) {
                        int yy = index_y + y;
                        if (yy < 0 || yy >= array[x].length) continue;
                        if (array[x][yy]) return new Vector2d(x, yy);
                    }
                } else {
                    int y1 = index_y - depth*cy;
                    int y2 = index_y + depth*cy;
                    if (y1 >= 0 && y1 < array[x].length && array[x][y1]) return new Vector2d(x, y1);
                    if (y2 >= 0 && y2 < array[x].length && array[x][y2]) return new Vector2d(x, y2);
                }
            }
            depth++;
        }
    }

    public static List<Vector2d> getStationaryPoints(Function2d h, double gradient_cutoff, double world_bound, int resolution) {
        double square_gc = gradient_cutoff * gradient_cutoff;
        double increment = world_bound / ((double)resolution);
        List<Vector2d> points = new ArrayList<>();
        for (double x=0; x <= world_bound; x += increment) {
            for (double y=0; y <= world_bound; y += increment) {
                if (h.evaluate(x, y) > 0) {
                    Vector2d point = new Vector2d(x, y);
                    if (x == 0 || y == 0 || x >= world_bound || y >= world_bound) points.add(point);
                    else if (h.gradient(x, y).squared_length() < square_gc) points.add(point);
                }
            }
        }
        return points;
    }

    public static List<Vector2d> getPointsWithGradient(Function2d h, double squared_gradient_length, double tolerance, int parts){
        double total_gradient = squared_gradient_length;
        double increment = 1d / ((double)parts);
        List<Vector2d> points = new ArrayList<Vector2d>();

        for(double i = 0d; i <= 1d; i += increment) {
            double x = linearInterpolate(0, Variables.BOUNDED_WORLD_SIZE, i);

            for (double j = 0d; j <= 1d; j += increment) {
                double y = linearInterpolate(0, Variables.BOUNDED_WORLD_SIZE, j);
                double height = h.evaluate(x, y);

                if (height > 0) {
                    double total_local_gradient = h.gradient(x, y).squared_length();

                    if (Math.abs(total_gradient - total_local_gradient) <= tolerance)
                        points.add(new Vector2d(x, y));
                }
            }
        }

        return points;
    }

    public static double fluctuation(Function2d h, int parts){
        int steps = parts;
        double error = 0d;
        Vector2d old_gradient = h.gradient(0, 0);

        for(double i = 0d; i < 1d; i += 1d/((double) steps)) {
            double x = linearInterpolate(0, 50, i);

            for (double j = 0d; j < 1d; j += 1d / ((double) steps)) {
                double y = linearInterpolate(0, 50, j);
                double height = h.evaluate(x, y);

                if (height > 0) {
                    Vector2d gradient = h.gradient(x, y);
                    double total_gradient = gradient.abs().get_x() + gradient.abs().get_y();
                    double old_total_gradient = old_gradient.abs().get_x() + old_gradient.abs().get_y();
                    error += Math.abs(old_total_gradient - total_gradient);
                }

            }

        }

        return error / (steps * steps);
    }

    public static double[][] asGrid(Function2d h, int steps){
        double[][] heights = new double[steps][steps];

        for(double i = 0d; i < 1d; i += 1d/((double) steps)) {
            double x = linearInterpolate(0, 50, i);

            for (double j = 0d; j < 1d; j += 1d / ((double) steps)) {
                double y = linearInterpolate(0, 50, j);
                double height = h.evaluate(x, y);

                heights[(int)(i * steps)][(int)(j * steps)] = 0;

                if (height > 0) {
                    heights[(int)(i * steps)][(int)(j * steps)] = height;
                }
            }

        }

        return  heights;
    }

    public static double[][] asTiles(PuttingCourse world, int steps){
        double[][] heights = new double[steps][steps];

        for(double i = 0d; i < 1d; i += 1d/((double) steps)) {
            double x = linearInterpolate(0, 50, i);

            for (double j = 0d; j < 1d; j += 1d / ((double) steps)) {
                double y = linearInterpolate(0, 50, j);
                double height = evalHeightAt(world, x, y);

                heights[(int)(i * steps)][(int)(j * steps)] = 0;

                if (height > 0) {
                    heights[(int)(i * steps)][(int)(j * steps)] = height;
                }
            }

        }

        return  heights;
    }

    public static boolean isClearPath(Vector2d start, Vector2d end, Function2d h, int steps, double tolerance){

        for(double i = 1d/((double) steps); i <= 1d; i += 1d/((double) steps)){
            Vector2d current = new Vector2d(
                    linearInterpolate(start.get_x(), end.get_x(), i),
                    linearInterpolate(start.get_y(), end.get_y(), i)
            );

            if(h.evaluate(current) <= 0 && current.distance(end) > tolerance)
                return false;

        }

        return true;
    }

    public static double evalHeightAt(PuttingCourse world, double x, double y){

        for(Obstacle o : world.getObstacles()){

            if(o.isPositionInsideShape(x, y))
                return ROOF;

        }

        return world.height_function.evaluate(x, y);
    }

}

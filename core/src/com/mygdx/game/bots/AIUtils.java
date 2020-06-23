package com.mygdx.game.bots;

import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.Ball;
import com.mygdx.game.obstacles.Obstacle;
import com.mygdx.game.parser.Function2d;
import com.mygdx.game.physics.PuttingCoursePhysics;
import com.mygdx.game.utils.Vector2d;

import java.util.ArrayList;
import java.util.List;

public final class AIUtils {
    public static final int OPTIMAL_RESOLUTION = 2000;

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

    public static List<Vector2d> getPointsWithGradient(Function2d h, Vector2d gradient, double tolerance, int parts){
        double total_gradient = gradient.abs().get_x() + gradient.abs().get_y();
        int steps = parts;
        List<Vector2d> points = new ArrayList<Vector2d>();

        for(double i = 0d; i < 1d; i += 1d/((double) steps)) {
            double x = linearInterpolate(0, 50, i);

            for (double j = 0d; j < 1d; j += 1d / ((double) steps)) {
                double y = linearInterpolate(0, 50, j);
                double height = h.evaluate(x, y);

                if (height > 0) {
                    Vector2d local_gradient = h.gradient(x, y).abs();
                    double total_local_gradient = local_gradient.get_x() + local_gradient.get_y();

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

    public static boolean isWaterFreePath(Vector2d start, Vector2d normalized_direction, Function2d h, List<Obstacle> obstacles, int steps, double max_distance) {
        double square_distance_left_to_cover = max_distance * max_distance;
        int steps_left = steps;
        double distance_per_step = max_distance / steps;
        double dir_x = normalized_direction.get_x() * distance_per_step;
        double dir_z = normalized_direction.get_y() * distance_per_step;
        double x = start.get_x(), z = start.get_y();
        for (;steps_left > 0; steps_left--) {
            double y = h.evaluate(x, z);
            if (y < 0) return false;
            for (Obstacle o : obstacles) {
                if (o.getBoundingBox().contains(x, y, z)) return true; }
            x += dir_x; z += dir_z;
        } return true;
    }

    public static double evalHeightAt(double x, double y, Ball example){
        return 0;
    }

}

package com.mygdx.game;

import java.util.Collections;
import java.util.List;

import static com.mygdx.game.Variables.*;
import static com.mygdx.game.AIUtils.*;

public class AI_Fedora extends AI_controller {
    private double RESOLUTION_SLOPE = (1010d - 10d) / 0.0046d;
    private int explore_resolution;
    private int MAX_OPTIONS = 100;
    private  double VELOCITY_PARTITIONS = 20d;

    private double shot_angle, shot_speed;
    private List<Vector2d> points;

    private Vector2d old_ball_position;

    private static class Fedora {}
    public Fedora the_fedora;

    @Override
    public String getName() {
        return "Fedora Bot";
    }

    @Override
    public String getDescription() {
        return "Heuristic selection with route sampling AI.";
    }

    @Override
    public void calculate(Player player) {
        double error = fluctuation(WORLD.get_height(), 100);

        if(old_ball_position != null && old_ball_position.get_x() != player.getBall().x && old_ball_position.get_y() != player.getBall().y){
            points = null;
        }

        if(points == null){
            System.out.println("Cumulative Error: " + error);
            explore_resolution = (int) resolution(error);
            System.out.println("Calculated resolution is " + explore_resolution);

            if(explore_resolution > 2000){
                explore_resolution = 2000;
                System.out.println("Resolution trimmed down to " + 2000);
            }

            Vector2d lowest = findLowestGradient(WORLD.get_height(), explore_resolution);
            points = getPointsWithGradient(WORLD.get_height(), lowest, 0.01, explore_resolution);
            System.out.println("Fedora found " + points.size() + " options.");
        }

        Collections.sort(points, (a, b) -> {
            Vector2d ball_to_point_a = a.sub(new Vector2d(player.getBall().x, player.getBall().y)).normalize();
            Vector2d ball_to_point_b = b.sub(new Vector2d(player.getBall().x, player.getBall().y)).normalize();
            Vector2d ball_to_flag = WORLD.get_flag_position().sub(new Vector2d(player.getBall().x, player.getBall().y)).normalize();

            double dotp_a = ball_to_point_a.dot(ball_to_flag);
            double dotp_b = ball_to_point_b.dot(ball_to_flag);

            if(dotp_a > dotp_b)
                return 1;

            if(dotp_a < dotp_b)
                return -1;

            return 0;
        });

        if(points.size() > MAX_OPTIONS) {
            points.subList(0, points.size() - MAX_OPTIONS).clear();
            System.out.println("Options narrowed down to " + points.size());
        }

        Vector2d selection = null;
        double selection_ball_to_flag = 0d;
        double selection_speed = 0d;
        double selection_test = 0d;

        outer_for: for (int point_i = points.size() - 1; point_i >= 0; point_i--){
            Vector2d p = points.get(point_i);

            Vector2d player_to_point = p.sub(new Vector2d(player.getBall().x, player.getBall().y)).normalize();
            Vector2d player_to_flag = WORLD.get_flag_position().sub(new Vector2d(player.getBall().x, player.getBall().y)).normalize();

            double comp = player_to_point.dot(player_to_flag);
            System.out.println("Point (" + points.indexOf(p) + ")'s dot product is " + comp);

            for(double speed_i = MAX_SHOT_VELOCITY / VELOCITY_PARTITIONS; speed_i <= MAX_SHOT_VELOCITY; speed_i += MAX_SHOT_VELOCITY / VELOCITY_PARTITIONS) {
                Ball simulated_ball = player.getBall().simulateHit(player_to_point, speed_i, 8000, 0.01);
                double ball_to_flag = WORLD.get_flag_position().distance(new Vector2d(simulated_ball.x, simulated_ball.y));
                double real_ball_to_flag = WORLD.get_flag_position().distance(new Vector2d(player.getBall().x, player.getBall().y));

                System.out.println("\tTest speed=" + speed_i + " | Resulting distance=" + ball_to_flag + " | Travel distance=" + simulated_ball.travel_distance);
                System.out.println("\t\tDistance from ball to flag=" + real_ball_to_flag);
                System.out.println("\t\tIs touching flag=" + simulated_ball.isTouchingFlag());
                System.out.println("\t\tIs moving=" + simulated_ball.is_moving);

                double flag_test = 0d;

                if(simulated_ball.isTouchingFlag())
                    flag_test = 1d;

                double motion_test = 0d;

                if(simulated_ball.is_moving)
                    motion_test = 1d;

                double distance_test = ball_to_flag - WORLD.get_hole_tolerance();
                double travel_test = simulated_ball.travel_distance;

                if(travel_test == 0d)
                    continue;

                double w1 = 1d;
                double w2 = 0.5d;
                double w3 = 1d;
                double w4 = 0.1d;
                double total_test = (flag_test * w1 + comp * w2 - distance_test * w3) / (travel_test * w4);

                System.out.println("\t\tQualification=" + total_test);

                if(selection == null){
                    selection = p;
                    selection_ball_to_flag = ball_to_flag;
                    selection_speed = speed_i;
                    selection_test = total_test;
                }

                else if(selection_test <= total_test){
                    selection = p;
                    selection_ball_to_flag = ball_to_flag;
                    selection_speed = speed_i;
                    selection_test = total_test;
                }

            }

        }

        if(selection != null) {
            System.out.println("Selected point ID is " + points.indexOf(selection));
            points.remove(selection);

            if(points.isEmpty())
                points = null;

        }

        else if(selection == null){
            System.out.println("Doing direct shot.");
            selection = WORLD.get_flag_position();
        }

        if(selection_speed == 0d)
            selection_speed = MAX_SHOT_VELOCITY;

        System.out.println("Decided shot velocity is " + selection_speed);

        Vector2d direction = selection.sub(new Vector2d(player.getBall().x, player.getBall().y)).normalize();
        double shot_angle = direction.angle();
        System.out.println("Decided shot angle is " + shot_angle);
        setShotAngle(shot_angle);
        setShotVelocity(selection_speed);

        old_ball_position = new Vector2d(player.getBall().x, player.getBall().y);
    }

    private double resolution(double x){
        return RESOLUTION_SLOPE * x + 10d;
    }

}

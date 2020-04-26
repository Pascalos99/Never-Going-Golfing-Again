package com.mygdx.game;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.mygdx.game.Variables.*;
import static com.mygdx.game.AIUtils.*;

public class AI_Fedora implements AI_controller {
    private double RESOLUTION_SLOPE = (1010d - 10d) / 0.0046d;
    private int explore_resolution;

    private double shot_angle, shot_speed;
    private List<Vector2d> points;

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
    public void startCalculation(Player player) {

        if(points == null){
            double error = fluctuation(WORLD.get_height(), 100);
            System.out.println("Cumulative Error: " + error);
            explore_resolution = (int) resolution(error);
            System.out.println("Calculated resolution is " + explore_resolution);
            Vector2d lowest = findLowestGradient(WORLD.get_height(), explore_resolution);
            points = getPointsWithGradient(WORLD.get_height(), lowest, 0.01, explore_resolution);
            System.out.println("Fedora found " + points.size() + " options.");
        }

        Collections.sort(points, new Comparator<Vector2d>() {
            @Override
            public int compare(Vector2d a, Vector2d b) {
                Vector2d rel_a = a.sub(new Vector2d(player.getBall().x, player.getBall().y)).normalize();
                Vector2d rel_b = b.sub(new Vector2d(player.getBall().x, player.getBall().y)).normalize();
                Vector2d goal = WORLD.get_flag_position().sub(new Vector2d(player.getBall().x, player.getBall().y)).normalize();

                double comp_a = rel_a.dot(goal);
                double comp_b = rel_b.dot(goal);

                if(comp_a > comp_b)
                    return 1;

                if(comp_a < comp_b)
                    return -1;

                return 0;
            }
        });

        for(int i = 0; i < points.size() - 1; i++){
            Vector2d a_point = points.get(i);
            Vector2d b_point = points.get(i + 1);

            Vector2d rel_a = a_point.sub(new Vector2d(player.getBall().x, player.getBall().y)).normalize();
            Vector2d rel_b = b_point.sub(new Vector2d(player.getBall().x, player.getBall().y)).normalize();
            Vector2d goal = WORLD.get_flag_position().sub(new Vector2d(player.getBall().x, player.getBall().y)).normalize();

            double comp_a = rel_a.dot(goal);
            double comp_b = rel_b.dot(goal);

            if(Math.abs(comp_a - comp_b) < 0.5){

            }

        }


        Vector2d selection = null;
        double distance = 0d;
        double speed = 0d;

        for (Vector2d p : points){

                for(double i = MAX_SHOT_VELOCITY/20d; i <= 6d; i += MAX_SHOT_VELOCITY/20d) {
                    double new_distance = unfoldDistance(p, WORLD.get_flag_position(), WORLD.get_height(), 100);

                    if(selection == null){
                        selection = p;
                        distance = new_distance;
                        speed = i;
                    }

                    else if (new_distance < distance && successful(p, player.getBall(), i, new_distance)) {
                        distance = new_distance;
                        selection = p;
                        speed = i;
                    }

                }

        }

        selection = points.get(points.size() - 1);
        speed = MAX_SHOT_VELOCITY;

        if(selection != null)
            points.remove(selection);

        else if(selection == null || speed == 0d){
            selection = WORLD.get_flag_position();
            speed = MAX_SHOT_VELOCITY;
        }

        if(points.isEmpty())
            points = null;

        Vector2d direction = selection.sub(new Vector2d(player.getBall().x, player.getBall().y)).normalize();
        shot_angle = Math.atan2(direction.get_y(), direction.get_x());
        shot_speed = speed;
    }

    public boolean finishedCalculation() {
        return true;
    }

    @Override
    public double getShotAngle() {
        return shot_angle;
    }

    @Override
    public double getShotVelocity() {
        return shot_speed;
    }

    private boolean successful(Vector2d p, Ball b, double speed, double expected_distance){
        Vector2d relative = p.sub(new Vector2d(b.x, b.y)).normalize();
        Ball out = b.simulateHit(relative, speed, 8000, 0.01);

        if(out.isTouchingFlag())
            return true;

        if(!out.isStuck() && unfoldDistance(new Vector2d(out.x, out.y), WORLD.get_flag_position(), WORLD.get_height(), 100) - expected_distance < 1d)
            return true;

        return false;
    }

    private double resolution(double x){
        return RESOLUTION_SLOPE * x + 10d;
    }

}

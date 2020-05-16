package com.mygdx.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static com.mygdx.game.Variables.*;
import static com.mygdx.game.AIUtils.*;
import java.util.AbstractMap.SimpleEntry;

public class AI_Fedora extends AI_controller {
    private int VECTOR_COUNT = 1000;
    private double VELOCITY_PARTITIONS = 20d;
    private int MAX_TICKS = 8000;

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
        Vector2d real_ball_position = new Vector2d(player.getBall().x, player.getBall().y);

        if(old_ball_position != null && old_ball_position.get_x() != player.getBall().x && old_ball_position.get_y() != player.getBall().y){
            points = null;
        }

        if(points == null){
            points = new ArrayList<Vector2d>(VECTOR_COUNT);
            Vector2d anchor = new Vector2d(getWorld().get_flag_position().distance(real_ball_position), 0);

            for(int i = 0; i < VECTOR_COUNT; i++) {
                Vector2d ray = anchor.rotate(((double) (i)) * 2d * Math.PI / ((double) VECTOR_COUNT));

                if(isClearPath(real_ball_position, ray, getWorld().get_height(), OPTIMAL_RESOLUTION))
                    points.add(ray);

            }

        }

        Vector2d selection = null;
        double selection_speed = 0d;
        double selection_test = 0d;

        double selection_flag = 0d;
        double selection_dist = 0d;
        double selection_ticks = 0d;
        double selection_travel = 0d;

        outer_for: for (int point_i = points.size() - 1; point_i >= 0; point_i--){
            Vector2d p = points.get(point_i);
            Vector2d ball_to_point = p.sub(real_ball_position).normalize();

            for(double speed_i = MAX_SHOT_VELOCITY / VELOCITY_PARTITIONS; speed_i <= MAX_SHOT_VELOCITY; speed_i += MAX_SHOT_VELOCITY / VELOCITY_PARTITIONS) {
                Ball simulated_ball = player.getBall().simulateHit(ball_to_point, speed_i, MAX_TICKS, 0.01);
                Vector2d sim_ball_position = new Vector2d(simulated_ball.x, simulated_ball.y);

                double sim_ball_to_flag = getWorld().get_flag_position().distance(sim_ball_position);
                double real_ball_to_flag = getWorld().get_flag_position().distance(real_ball_position);

                double w1 = 1.5d;
                double w2 = 1d;
                double w3 = 1d / ((double)MAX_TICKS);
                double w4 = 1d;

                double flag = 0d;
                double old_dist = real_ball_to_flag;
                double new_dist = sim_ball_to_flag;
                double ticks = simulated_ball.ticks;
                double travel = simulated_ball.travel_distance;

                if(simulated_ball.isTouchingFlag())
                    flag = 1d;

                else if(simulated_ball.isStuck()) {
                    continue;
                }

                if(travel == 0d)
                    continue;

                double flag_test = flag * w1;
                double dist_test = (old_dist - new_dist) * w2;
                double tick_test = (((double)MAX_TICKS) - ticks) * w3;
                double trav_test = Math.sqrt(travel) * w4;

                double total_test = flag_test + dist_test + tick_test - trav_test;

                if(selection == null || selection_test <= total_test){
                    selection = p;
                    selection_speed = speed_i;
                    selection_test = total_test;

                    selection_flag = flag_test;
                    selection_dist = dist_test;
                    selection_ticks = tick_test;
                    selection_travel = trav_test;
                }

                System.out.println("\t(" + point_i + ")" + " Testing speed = " + speed_i + " | Testing angle = " + ball_to_point.angle());
                System.out.println("\t\tTest = " + total_test);
            }

        }

        if(selection != null) {
            System.out.println("Selected point ID is " + points.indexOf(selection));
            points.remove(selection);

            if(points.isEmpty())
                points = null;

        }

        else if(selection == null){
            System.out.println("Fedora did not find a solution. Doing direct shot.");
            selection = getWorld().get_flag_position();
            selection_speed = MAX_SHOT_VELOCITY;
        }

        System.out.println("Decided shot velocity is " + selection_speed);

        Vector2d direction = selection.sub(new Vector2d(player.getBall().x, player.getBall().y)).normalize();
        double shot_angle = direction.angle();
        System.out.println("Decided shot angle is " + shot_angle);
        System.out.println("\t\tflag = " + selection_flag);
        System.out.println("\t\tdistance = " + selection_dist);
        System.out.println("\t\tticks = " + selection_ticks);
        System.out.println("\t\ttravel = " + selection_travel);
        System.out.println("Final assesment is " + selection_test);

        old_ball_position = real_ball_position;

        setShotAngle(shot_angle);
        setShotVelocity(selection_speed);
    }

    @Override
    public void clear(){
        points = null;
    }

    class SimulationData{
        private Vector2d vector;
        private Ball simulated_ball;

        SimulationData(Vector2d vector, Ball simulated_ball){
            this.vector = vector;
            this.simulated_ball = simulated_ball;
        }

        public double distance_to_flag(){
            Vector2d ball_pos = new Vector2d(simulated_ball.x, simulated_ball.y);
            return getWorld().get_flag_position().distance(ball_pos);
        }

        public double touching_flag(){
            return 0d;
        }

        public double distance_improvement(){
            return 0d;
        }

        public double ticks(){
            return 0d;
        }

    }

}
package com.mygdx.game.bots;

import java.util.*;
import java.util.stream.Collectors;

import static com.mygdx.game.utils.Variables.*;
import static com.mygdx.game.bots.AIUtils.*;
import com.mygdx.game.Player;
import com.mygdx.game.utils.Vector2d;
import com.mygdx.game.Ball;
import java.util.AbstractMap.SimpleEntry;

public class AI_Fedora extends AI_controller {
    private int VECTOR_COUNT = 100;
    private double VELOCITY_PARTITIONS = 100d;
    private int MAX_TICKS = 8000;

    private List<TestDataHolder> tests;
    private Vector2d old_ball_position;
    private boolean do_not_play = false;

    private static class Fedora {}
    public Fedora the_fedora;

    private static class SunGlasses {}
    public SunGlasses the_shades;

    @Override
    public String getName() {
        return "Fedora Bot";
    }

    @Override
    public String getDescription() {
        return "Heuristic selection with route sampling.";
    }

    @Override
    public void calculate(Player player) {

        if(!do_not_play && (old_ball_position == null || (old_ball_position.get_x() != player.getBall().x && old_ball_position.get_y() != player.getBall().y))) {
            old_ball_position = new Vector2d(player.getBall().x, player.getBall().y);
            List<Vector2d> vectors = cast_vectors(player.getBall());
            tests = new ArrayList<TestDataHolder>(vectors.size());

            System.out.println(getWorld().get_flag_position());
            System.out.println("Vectors: " + vectors.size());

            for (Vector2d v : vectors) {
                TestDataHolder test_data = get_test_data(player.getBall(), v);

                if (test_data != null)
                    tests.add(test_data);

            }

            System.out.println("Tests: " + tests.size());

            if(!tests.isEmpty()) {
                TestDataHolder head = find_closest_to_flag(tests);
                System.out.println("Closest to flag: " + head.toString());

                if(some_are_touching_flag(tests))
                    tests = discard_some_tests(tests);

                sort_test_data(tests);
            }

            else{
                System.out.println("No solution found.");
                do_not_play = true;
            }

        }

        if(!tests.isEmpty()){
            TestDataHolder best = tests.get(0);
            tests.remove(0);

            setShotAngle(best.direction.angle());
            setShotVelocity(best.speed);
        }

    }

    public void clear(){
        old_ball_position = null;
        do_not_play = false;

        if(tests != null)
            tests.clear();

    }

    private List<Vector2d> cast_vectors(Ball ball){
        Vector2d real_ball_position = new Vector2d(ball.x, ball.y);
        List<Vector2d> points = new ArrayList<Vector2d>(VECTOR_COUNT);
        Vector2d anchor = new Vector2d(getWorld().get_flag_position().distance(real_ball_position), 0);

        for(int i = 0; i < VECTOR_COUNT; i++) {
            Vector2d ray = anchor.rotate(((double) (i)) * 2d * Math.PI / ((double) VECTOR_COUNT));

            if(isClearPath(real_ball_position, real_ball_position.add(ray), getWorld().get_height(), OPTIMAL_RESOLUTION, getWorld().get_hole_tolerance()))
                points.add(ray.normalize());

        }

        return points;
    }

    private TestDataHolder get_test_data(Ball ball, Vector2d direction){
        double VELOCITY_INCREASE = MAX_SHOT_VELOCITY / VELOCITY_PARTITIONS;
        TestDataHolder output = null;

        for(double speed_i = MAX_SHOT_VELOCITY; speed_i > 0; speed_i -= VELOCITY_INCREASE){
            Ball simulated_ball = ball.simulateHit(direction, speed_i, MAX_TICKS, DELTA);
            TestDataHolder test = new TestDataHolder(simulated_ball, direction, speed_i);

            if(output == null)
                output = test;

           if(simulated_ball.isStuck() && !simulated_ball.isTouchingFlag())
               continue;

           if((simulated_ball.isTouchingFlag() && !output.ball.isTouchingFlag()) || (test.distance_to_flag <= output.distance_to_flag))
               output = test;

        }

        return output;
    }


    private TestDataHolder find_closest_to_flag(List<TestDataHolder> tests){
        TestDataHolder head = null;

        for(TestDataHolder t : tests){

            if(head == null || t.distance_to_flag < head.distance_to_flag)
                head = t;

        }

        return head;
    }

    private List<TestDataHolder> filter_test_data(List<TestDataHolder> tests, TestDataHolder head, double tolerance){
        List<TestDataHolder> output = new ArrayList<TestDataHolder>(tests.size());

        for(TestDataHolder t : tests){

            if(Math.abs(head.distance_to_flag - t.distance_to_flag) <= tolerance)
                output.add(t);

        }

        return output;
    }

    private void sort_test_data(List<TestDataHolder> tests){
        tests.sort(Comparator.comparingDouble(TestDataHolder::measure));
    }

    private boolean some_are_touching_flag(List<TestDataHolder> tests){

        for(TestDataHolder t : tests){

            if(t.ball.isTouchingFlag())
                return true;

        }

        return false;
    }

    private List<TestDataHolder> discard_some_tests(List<TestDataHolder> tests){
        List<TestDataHolder> output = new ArrayList<TestDataHolder>(tests.size());

        for(TestDataHolder t : tests){

            if(t.ball.isTouchingFlag())
                output.add(t);

        }

        return output;
    }

    class TestDataHolder{
        public Vector2d direction;
        public double speed;
        public double distance_to_flag;
        public Ball ball;

        TestDataHolder(Ball ball, Vector2d direction, double speed){
            this.ball = ball;
            this.direction = direction;
            this.speed = speed;

            Vector2d ball_position = new Vector2d(ball.x, ball.y);
            this.distance_to_flag = ball_position.distance(getWorld().get_flag_position());
        }

        public double measure(){
            return this.distance_to_flag;
        }

        @Override
        public String toString() {
            return direction.toString() + " | " + "Speed(" + speed + ") | " + "Distance(" + distance_to_flag + ")" + " | " + ball.isTouchingFlag();
        }
    }

}
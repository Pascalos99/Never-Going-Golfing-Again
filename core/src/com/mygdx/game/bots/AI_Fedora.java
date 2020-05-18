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
    private int VECTOR_COUNT = 1000;
    private double VELOCITY_PARTITIONS = 100d;
    private int MAX_TICKS = 8000;
    private double DELTA = 0.001d;

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
        Vector2d direction = getWorld().get_flag_position().sub(new Vector2d(player.getBall().x, player.getBall().y)).normalize();
        System.out.println("Direction: " + direction.toString());

        double VELOCITY_INCREASE = MAX_SHOT_VELOCITY / VELOCITY_PARTITIONS;
        TestDataHolder output = null;

        for(double speed_i = VELOCITY_INCREASE; speed_i <= MAX_SHOT_VELOCITY; speed_i += VELOCITY_INCREASE){
            Ball simulated_ball = player.getBall().simulateHit(direction, speed_i, MAX_TICKS, DELTA);
            TestDataHolder test = new TestDataHolder(simulated_ball, direction, speed_i);

            if(simulated_ball.isTouchingFlag())
                System.out.println("Idiot! pick this one motherfucker!!!" + speed_i);

            if(output == null)
                output = test;

            else if(simulated_ball.isStuck() && !simulated_ball.isTouchingFlag())
                continue;

            else if(simulated_ball.isTouchingFlag())
                output = test;

            else if(test.distance_to_flag <= output.distance_to_flag)
                output = test;

        }

        if(output != null) {
            System.out.println("Output: " + output.toString());

            setShotVelocity(output.speed);
            setShotAngle(output.direction.angle());
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
            Vector2d ray = this.direction;
            Vector2d flag = getWorld().get_flag_position().sub(new Vector2d(this.ball.x, this.ball.y));

            return ray.normalize().dot(flag.normalize());
        }

        @Override
        public String toString() {
            return direction.toString() + " | " + "Speed(" + speed + ") | " + "Distance(" + distance_to_flag + ")" + " | " + ball.isTouchingFlag();
        }
    }

}
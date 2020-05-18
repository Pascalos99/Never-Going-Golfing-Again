package com.mygdx.game.bots;

import java.util.*;
import java.util.stream.Collectors;

import static com.mygdx.game.utils.Variables.*;
import static com.mygdx.game.bots.AIUtils.*;

import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.Player;
import com.mygdx.game.utils.Vector2d;
import com.mygdx.game.Ball;
import java.util.AbstractMap.SimpleEntry;

public class AI_Fedora extends AI_controller {
    private int VECTOR_COUNT = 360; //1000
    private double VELOCITY_PARTITIONS = 100d; //100
    private int MAX_TICKS = 8000;

    private List<TestDataHolder> tests;
    private Vector2d old_ball_position;

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

        if(old_ball_position == null || (old_ball_position.get_x() != player.getBall().x && old_ball_position.get_y() != player.getBall().y)) {
            double start = System.currentTimeMillis() * 0.001;

            old_ball_position = new Vector2d(player.getBall().x, player.getBall().y);
            List<Vector2d> vectors = cast_vectors(player.getBall());
            System.out.println("Using " + vectors.size() + " of " + VECTOR_COUNT + " vectors.");
            tests = new ArrayList<TestDataHolder>(vectors.size());

            for(Vector2d v : vectors) {
                TestDataHolder test = get_test_data(player.getBall(), v);

                if(test != null)
                    tests.add(test);

            }

            Collections.sort(tests, new Comparator<TestDataHolder>() {
                @Override
                public int compare(TestDataHolder a, TestDataHolder b) {

                    if(a.weight > b.weight)
                        return 1;

                    if(a.weight < b.weight)
                        return -1;

                    return 0;
                }
            });

            double end = System.currentTimeMillis() * 0.001;
            System.out.println("[FEDORA] Time span: " + (end - start));
        }

        if(!tests.isEmpty()){
            TestDataHolder select = tests.get(tests.size() - 1);
            tests.remove(tests.size() - 1);
            setShotVelocity(select.speed);
            setShotAngle(select.direction.angle());
            System.out.println("[FEDORA] Select: " + select.weight);
        }

        else{
            System.out.println("[FEDORA] No options left.");
        }

    }

    public void clear(){
        old_ball_position = null;
    }

    private List<Vector2d> cast_vectors(Ball ball){
        Vector2d real_ball_position = new Vector2d(ball.x, ball.y);
        List<Vector2d> points = new ArrayList<Vector2d>(VECTOR_COUNT);
        Vector2d to_flag = getWorld().get_flag_position().sub(real_ball_position).normalize();
        Vector2d anchor = (new Vector2d(getWorld().get_flag_position().distance(real_ball_position), 0)).rotate(to_flag.angle());

        for(int i = 0; i < VECTOR_COUNT; i++) {
            Vector2d ray = anchor.rotate(((double) (i)) * 2d * Math.PI / ((double) VECTOR_COUNT));

            if(
                    isClearPath(
                            real_ball_position,
                            real_ball_position.add(ray),
                            getWorld().get_height(),
                            OPTIMAL_RESOLUTION,
                            getWorld().get_hole_tolerance()
                    ) && (ray.normalize().dot(to_flag) > 0)
            )
                points.add(ray.normalize());

        }

        Collections.sort(points, new Comparator<Vector2d>() {
            @Override
            public int compare(Vector2d a, Vector2d b) {

                if(a.dot(to_flag) > b.dot(to_flag))
                    return 1;

                if(a.dot(to_flag) < b.dot(to_flag))
                    return -1;

                return 0;
            }
        });
        return points;
    }

    private TestDataHolder get_test_data(Ball ball, Vector2d direction){
        double VELOCITY_INCREASE = MAX_SHOT_VELOCITY / VELOCITY_PARTITIONS;
        TestDataHolder output = null;

        for(double speed_i = VELOCITY_INCREASE; speed_i <= MAX_SHOT_VELOCITY; speed_i += VELOCITY_INCREASE){
            Ball test_ball = ball.simulateHit(direction, speed_i, MAX_TICKS, DELTA);

            double displacement = (new Vector2d(test_ball.x, test_ball.y)).distance(new Vector2d(ball.x, ball.y));
            double old_distance_to_flag = getWorld().get_flag_position().distance(new Vector2d(ball.x, ball.y));
            double new_distance_to_flag = getWorld().get_flag_position().distance(new Vector2d(test_ball.x, test_ball.y));

            if(
                    (!test_ball.isTouchingFlag() && test_ball.isStuck())
                    || (displacement <= BALL_RADIUS)
            ){
                continue;
            }

            double displacement_test = -Math.pow(displacement - old_distance_to_flag, 2) + 1;
            double change_in_distance = -Math.pow(new_distance_to_flag, 2) + 1;
            double tick_test = 1 - (((double)test_ball.ticks) / ((double)MAX_TICKS));
            double flag_test = test_ball.isTouchingFlag()? 1:0;
            double travel_test = -Math.pow(test_ball.travel_distance - old_distance_to_flag, 2) + 1;
            double total = displacement_test + change_in_distance + tick_test + flag_test + travel_test;
            total = total / 5d;

            TestDataHolder test = new TestDataHolder(direction, speed_i, total);

            if(output == null || test.weight > output.weight) {
                output = test;

                if(output.weight == 1d)
                    return output;

            }

        }

        return output;
    }

    class TestDataHolder{
        public Vector2d direction;
        public double speed;
        public double weight;

        TestDataHolder(Vector2d direction, double speed, double weight){
            this.direction = direction;
            this.speed = speed;
            this.weight = weight;
        }

        public double getWeight(){
            return this.weight;
        }

    }

}
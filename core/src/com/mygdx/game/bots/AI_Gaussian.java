package com.mygdx.game.bots;

import com.mygdx.game.Ball;
import com.mygdx.game.Player;
import com.mygdx.game.utils.Vector2d;

import java.util.Random;

import static com.mygdx.game.utils.Variables.DELTA;

public class AI_Gaussian extends AI_controller {

    public static int ANGLE_PARTITION = 25;
    public static int SPEED_PARTITION = 200;
    public static int MAX_TICKS = 1000;
    public static double EAGERNESS_TO_EXPLORE = 1.5;
    public static Random random = new Random(System.currentTimeMillis());

    @Override
    public String getName() {
        return "Gaussian Bot";
    }

    @Override
    public String getDescription() {
        return "Simulates a partition of shots based on the gaussian distribution around the direct angle towards the goal";
    }

    @Override
    protected void calculate(Player player) {
        Vector2d currentPos = new Vector2d(player.getBall().x, player.getBall().y);
        Vector2d toFlag = getWorld().flag_position.sub(currentPos).normalize();
        double direct_angle = toFlag.angle();
        Ball best = null;
        double chosen_velocity = 0;
        double chosen_angle = direct_angle;
        double[] angle_partition = createAnglePartitions(currentPos);
        double vel_increase = getWorld().maximum_velocity / SPEED_PARTITION;
        shot_testing: for (int k=0; k < angle_partition.length; k++) {
            Vector2d direction = Vector2d.fromAngle(angle_partition[k] + direct_angle);
            for(double speed_i = vel_increase; speed_i <= getWorld().maximum_velocity; speed_i += vel_increase){
                Ball test_ball = player.getBall().simulateHit(direction, speed_i, MAX_TICKS, DELTA);

                if(best == null || (test_ball.ticks != MAX_TICKS && !test_ball.isStuck() && best.topDownPosition().distance
                        (getWorld().flag_position) > test_ball.
                        topDownPosition().distance(getWorld().flag_position))) {
                    best = test_ball;
                    chosen_velocity = speed_i;
                    chosen_angle = direct_angle + angle_partition[k];
                }

                if (test_ball.isTouchingFlag()) {
                    setShotVelocity(speed_i);
                    setShotAngle(direct_angle + angle_partition[k]);
                    break shot_testing;
                }
            }
        }
        setShotVelocity(chosen_velocity);
        setShotAngle(chosen_angle);

    }

    private double g(Vector2d c, Vector2d d) {
        double max_distance = getWorld().start_position.distance(d);
        double current_distance = c.distance(d);
        return EAGERNESS_TO_EXPLORE * current_distance / max_distance;
    }

    private double[] createAnglePartitions(Vector2d currentPos) {
        double[] angle_partition = new double[ANGLE_PARTITION];
        angle_partition[0] = 0;
        double sigma = g(currentPos, getWorld().flag_position);
        for (int i=1; i < angle_partition.length; i++) {
            double val = 2*Math.PI;
            int n = 0;
            while ((val > Math.PI || val < -Math.PI) && (n < 10000)) { val = random.nextGaussian() * sigma; n++; }
            angle_partition[i] = val;
        }
        return angle_partition;
    }

}

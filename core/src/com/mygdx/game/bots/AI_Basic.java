package com.mygdx.game.bots;

import com.mygdx.game.Ball;
import com.mygdx.game.Player;
import com.mygdx.game.utils.Vector2d;

import static com.mygdx.game.utils.Variables.DELTA;
import static com.mygdx.game.utils.Variables.MAX_SHOT_VELOCITY;

public class AI_Basic extends AI_controller {
    private double VELOCITY_PARTITIONS = 100d; //100
    private int MAX_TICKS = 500;
    double VELOCITY_INCREASE = MAX_SHOT_VELOCITY / VELOCITY_PARTITIONS;

    public String getName() {
        return "Basic Bot";
    }

    public String getDescription() {
        return "Point and shoot based AI";
    }

    public void calculate(Player player) {
        Vector2d currentPos = new Vector2d(player.getBall().x, player.getBall().y);
        Vector2d toFlag = getWorld().flag_position.sub(currentPos).normalize();
        double angle = toFlag.angle();
        double velocity = 0d;
        Ball best = player.getBall();

        for(double speed_i = VELOCITY_INCREASE; speed_i <= MAX_SHOT_VELOCITY; speed_i += VELOCITY_INCREASE){
            Ball test_ball = player.getBall().simulateHit(toFlag, speed_i, MAX_TICKS, DELTA);

            double old_distance = best.topDownPosition().distance(getWorld().get_flag_position());
            double new_distance = test_ball.topDownPosition().distance(getWorld().get_flag_position());

            if(
                    best == null
                    || old_distance > new_distance){
                best = test_ball;
                velocity = speed_i;
            }

        }

        setShotAngle(angle);
        setShotVelocity(velocity);
    }

}
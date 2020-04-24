package com.mygdx.game;

import java.util.ArrayList;
import java.util.List;

import static com.mygdx.game.Variables.*;

public class AI_Fedora implements AI_controller {
    private double TURN_STEP = 90d * Math.PI / 180d;

    @Override
    public String getTypeName() {
        return "Fedora Bot";
    }

    @Override
    public void calculate(Player player) {
        double x = player.getBall().x;
        double y = player.getBall().y;
        double fx = GAME_ASPECTS.goalX;
        double fy = GAME_ASPECTS.goalY;

        Vector2d relative = new Vector2d(fx - x, fy - y);

        Vector2d[] vecs = new Vector2d[4];

        for(int i = 0; i < 4; i++){
            vecs[i] = relative.rotate(TURN_STEP * i).normalize();
        }

        List<Ball> balls = new ArrayList<Ball>();

        for(Vector2d v : vecs){

            for(double speed = 0d; speed < GAME_ASPECTS.maxVelocity; speed += 0.24d){
                Ball ball = player.getBall().simulateHit(v, speed);

                if(!ball.isStuck())
                    balls.add(ball);

            }

        }

    }

    @Override
    public double getShotAngle() {
        return 0;
    }

    @Override
    public double getShotVelocity() {
        return 0;
    }

}

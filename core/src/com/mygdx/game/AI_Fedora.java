package com.mygdx.game;

import static com.mygdx.game.Variables.*;

public class AI_Fedora implements AI_controller {
    private int TOTAL_STEPS = 8;
    private double TURN_STEP = (360d * Math.PI / 180d) / (double)TOTAL_STEPS;
    private  double shot_angle, shot_speed;

    private static class Fedora {}
    public Fedora the_fedora;

    private Vector2d old_pos;
    private double angle_offset = 0.0;

    @Override
    public String getTypeName() {
        return "Fedora Bot";
    }

    @Override
    public void calculate(Player player) {
        boolean rechoose = rewinded(player.getBall());

        if(rechoose)
            angle_offset += 0.1;

        else
            angle_offset = 0.0;

        double x = player.getBall().x;
        double y = player.getBall().y;
        double fx = GAME_ASPECTS.goalX;
        double fy = GAME_ASPECTS.goalY;

        Vector2d relative = new Vector2d(fx - x, fy - y);

        Vector2d[] vecs = new Vector2d[TOTAL_STEPS];

        for(int i = 0; i < vecs.length; i++){
            vecs[i] = relative.rotate((TURN_STEP * i) + angle_offset).normalize();
        }

        Vector2d best_direction = null, old_to_flag = null;
        double best_speed = 0d;

        for(int i = 0; i < vecs.length; i++){
            Vector2d direction = vecs[i];

            for(double speed = 0.24d; speed < GAME_ASPECTS.maxVelocity; speed += 0.24d){
                old_pos = new Vector2d(player.getBall().x, player.getBall().y);
                Ball ball = player.getBall().simulateHit(direction, speed);
                Vector2d to_flag = WORLD.flag_position.sub(new Vector2d(ball.x, ball.y));

                if(!ball.isStuck()){

                    if(best_direction == null){
                        best_direction = direction;
                        old_to_flag = to_flag;
                        best_speed = speed;
                    }

                    else if(to_flag.get_length() < old_to_flag.get_length()){
                        best_direction = direction;
                        old_to_flag = to_flag;
                        best_speed = speed;
                    }

                }

            }

        }

        shot_angle = Math.atan2(best_direction.get_y(), best_direction.get_x());
        shot_speed = best_speed;
    }

    @Override
    public double getShotAngle() {
        return shot_angle;
    }

    @Override
    public double getShotVelocity() {
        return shot_speed;
    }

    private boolean rewinded(Ball ball){

        if(old_pos == null)
            return false;

        else{

            if(old_pos.get_x() == ball.x && old_pos.get_y() == ball.y)
                return true;

        }

        return false;
    }

}

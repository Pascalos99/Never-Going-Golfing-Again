package com.mygdx.game;

import java.util.List;

import static com.mygdx.game.Variables.*;
import static com.mygdx.game.AIUtils.*;

public class AI_Fedora implements AI_controller {
    private double shot_angle, shot_speed;
    private List<Vector2d> points;

    private static class Fedora {}
    public Fedora the_fedora;

    @Override
    public String getTypeName() {
        return "Fedora Bot";
    }

    @Override
    public void calculate(Player player) {

        if(points == null){
            Vector2d lowest = findLowestGradient(WORLD.get_height());
            points = getPointsWithGradient(WORLD.get_height(), lowest, 0.01);
        }

        Vector2d selection = null;
        double distance = 0d;
        double speed = 0d;

        for (Vector2d p : points){

                for(double i = 0d; i < 6d; i += MAX_SHOT_VELOCITY/20d) {
                    double new_distance = unfoldDistance(p, WORLD.get_flag_position(), WORLD.get_height(), 1000);

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

        points.remove(selection);

        if(points.isEmpty())
            points = null;

        Vector2d direction = selection.sub(new Vector2d(player.getBall().x, player.getBall().y)).normalize();
        shot_angle = Math.atan2(direction.get_y(), direction.get_x());
        shot_speed = speed;
    }

    @Override
    public double getShotAngle() {
        return shot_angle;
    }

    @Override
    public double getShotVelocity() {
        return shot_speed;
    }

    private  boolean successful(Vector2d p, Ball b, double speed, double expected_distance){
        Vector2d relative = p.sub(new Vector2d(b.x, b.y)).normalize();
        Ball out = b.simulateHit(relative, speed, 8000, 0.01);

        if(out.isTouchingFlag())
            return true;

        if(!out.isStuck() && unfoldDistance(new Vector2d(out.x, out.y), WORLD.get_flag_position(), WORLD.get_height(), 1000) - expected_distance < 1d)
            return true;

        return false;
    }

}

package com.mygdx.game;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;

import java.util.List;

import static com.mygdx.game.Variables.*;

public class Ball implements TopDownPhysicsObject {
    public Vector2d velocity;
    public double x, y, old_x, old_y, real_x, real_y, old_h, real_h;
    public double r;
    public double mass = 0.005;

    private ModelInstance model;
    public double pastX,pastY;
    public boolean is_moving = false;
    public Player owner;
    private int hit_count;

    private double slide_x, slide_y, slide_h;

    Ball(double radius, double x_pos, double y_pos, ModelInstance model, Player owner) {
        x = x_pos;
        y = y_pos;
        pastX=x;
        pastY=y;
        r = radius;
        velocity = new Vector2d(0, 0);
        hit_count = 0;
        this.model = model;
        this.owner = owner;
    }

    public void step(double delta, List<TopDownPhysicsObject> ents) {

        if(is_moving) {
            Function2d h = WORLD.get_height();
            double gravity = WORLD.get_gravity();
            double friction = WORLD.get_friction_coefficient() * 8;

            if(isOnWater()){
                friction = 6d;
            }

            Vector2d gradient = h.gradient(new Vector2d(x, y));
            double half_x = -mass * gravity * gradient.get_x();
            double half_y = -mass * gravity * gradient.get_y();

            if (velocity.get_length() > 0) {
                half_x -= mass * gravity * friction * velocity.get_x() / velocity.get_length();
                half_y -= mass * gravity * friction * velocity.get_y() / velocity.get_length();
            }

            Vector2d acceleration = new Vector2d(half_x, half_y);

            double velocity_x = velocity.get_x() + acceleration.get_x();
            double velocity_y = velocity.get_y() + acceleration.get_y();
            velocity = new Vector2d(velocity_x, velocity_y);

            if (velocity.get_length() < VELOCITY_CUTTOFF && gradient.get_length() < GRADIENT_CUTTOFF) {
                velocity = new Vector2d(0, 0);
                is_moving = false;
            }

            slide_x = velocity_x * delta;
            slide_y = velocity_y * delta;
            slide_h = h.evaluate(x + slide_x, y + slide_y) - h.evaluate(x, y);

            x += slide_x;
            y += slide_y;

            if (x < BALL_RADIUS) {

                if (velocity.get_length() < VELOCITY_CUTTOFF){
                    is_moving = false;
                    velocity = new Vector2d(0,0);
                }

                else {
                    x = (BALL_RADIUS + 0.001 / WORLD_SCALING);
                    velocity = (new Vector2d(-velocity.get_x(), velocity.get_y()));
                }

            }

            if (x > (50 / WORLD_SCALING - BALL_RADIUS)) {

                if (velocity.get_length() < VELOCITY_CUTTOFF){
                    is_moving = false;
                    velocity = new Vector2d(0,0);
                }

                else {
                    x = (49.99 / WORLD_SCALING - BALL_RADIUS);
                    velocity = (new Vector2d(-velocity.get_x(), velocity.get_y()));
                }

            }

            if (y < BALL_RADIUS) {

                if (velocity.get_length() < VELOCITY_CUTTOFF){
                    is_moving = false;
                    velocity = new Vector2d(0,0);
                }

                else {
                    y = (BALL_RADIUS + 0.001 / WORLD_SCALING);
                    velocity = (new Vector2d(velocity.get_x(), -velocity.get_y()));
                }

            }

            if (y > (50 / WORLD_SCALING - BALL_RADIUS)) {

                if (velocity.get_length() < VELOCITY_CUTTOFF){
                    is_moving = false;
                    velocity = new Vector2d(0,0);
                }

                else {
                    y = (49.99 / WORLD_SCALING - BALL_RADIUS);
                    velocity = (new Vector2d(velocity.get_x(), -velocity.get_y()));
                }

            }

        }

        else{
            slide_x = 0;
            slide_y = 0;
            slide_h = 0;
        }

    }


    public void addVelocity(Vector2d v) {
        is_moving = true;
        velocity = velocity.add(v);
    }

    public float toWorldScale(double n){
        return (float)(n * WORLD_SCALING);
    }

    public double fromWorldScale(float n){
        return  n / WORLD_SCALING;
    }

    @Override
    public Vector3d getPosition() {
        return new Vector3d(
                toWorldScale(x),
                WORLD.get_height().evaluate(x, y) + BALL_RADIUS * WORLD_SCALING,
        toWorldScale(y)
        );
    }

    @Override
    public ModelInstance getModel() {
        old_x = real_x;
        old_y = real_y;
        old_h = real_h;

        Vector3d real_pos = getPosition();
        real_x = real_pos.get_x();
        real_h = real_pos.get_y();
        real_y = real_pos.get_z();

        model.transform.setTranslation((float) real_x, (float) real_h, (float) real_y);

        Vector3 vector = new Vector3(
                (float) (real_x - old_x),
                (float) (real_h - old_h),
                (float) (real_y - old_y)
        ).add(owner.getCameraPosition());
        owner.setCameraPosition(vector);
        return model;
    }

    @Override
    public double getOrientation() {
        return 0;
    }

    public boolean isTouchingFlag() {
        Vector2d flag = WORLD.get_flag_position();
        Vector2d ballPos = new Vector2d(x,y);
        double _r = WORLD.get_hole_tolerance();
      //  if (Math.sqrt(((x - flag.get_x()) * (x - flag.get_x()) + (y - flag.get_y()) * (y - flag.get_y()))) < _r + r) {
        if(flag.distance(ballPos)<_r){
          return true;
        }

        return false;
    }

    public boolean isOnWater() {

        if (WORLD.getHeightAt(x, y) <= 0)
            return true;

        return false;
    }

    public void hit(Vector2d direction, double speed){
        addVelocity(new Vector2d(direction.get_x() * speed, direction.get_y() * speed));
        hit_count += 1;
    }

    public int getHits(){
        return hit_count;
    }

    public void recordPastPos(){
        pastX=x;
        pastY=y;
    }

    public void resetToPast(){
        x=pastX;
        y=pastY;
    }

}

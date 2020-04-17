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

    public boolean is_moving = false;
    public Player owner;
    private int hit_count;

    private double slide_x, slide_y, slide_h;

    Ball(double radius, double x_pos, double y_pos, ModelInstance model, Player owner) {
        x = x_pos;
        y = y_pos;
        r = radius;
        velocity = new Vector2d(0, 0);
        hit_count = 0;
        this.model = model;
        this.owner = owner;
    }

    public void step(double delta, PuttingCourse world, List<TopDownPhysicsObject> ents) {
        System.out.println("Velocity: " + velocity.toString() + " Length: " + velocity.get_length());
        if(is_moving) {
            Function2d h = world.get_height();
            double gravity = world.get_gravity();
            double friction = world.get_friction_coefficient();

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
    public Vector3d getPosition(PuttingCourse world) {
        return new Vector3d(
                toWorldScale(x),
                world.get_height().evaluate(new Vector2d(x, y)) + BALL_RADIUS * WORLD_SCALING,
                toWorldScale(y)
        );
    }

    @Override
    public ModelInstance getModel(PuttingCourse world) {
        old_x = real_x;
        old_y = real_y;
        old_h = real_h;

        real_x = toWorldScale(x);
        real_h = (float) world.get_height().evaluate(x, y) + BALL_RADIUS * WORLD_SCALING;
        real_y = toWorldScale(y);

        model.transform.setTranslation((float) real_x, (float) real_h, (float) real_y);

        /*owner.setCameraPosition(
                new Vector3(
                        (float) (real_x - old_x),
                        (float) (real_h - old_h),
                        (float) (real_y - old_y)
                ).add(owner.getCameraPosition())
        );*/

        owner.setCameraPosition(
                new Vector3(
                        (float) toWorldScale(slide_x),
                        (float) slide_h,
                        (float) toWorldScale(slide_y)
                ).add(owner.getCameraPosition())
        );

        return model;
    }

    @Override
    public double getOrientation() {
        return 0;
    }

    public boolean isTouchingFlag(PuttingCourse world) {
        Vector2d flag = world.get_flag_position();
        double _r = world.get_hole_tolerance();

        if (Math.sqrt(((x - flag.get_x()) * (x - flag.get_x()) + (y - flag.get_y()) * (y - flag.get_y()))) < _r + r) {
            return true;
        }

        return false;
    }

    public boolean isOnWater(PuttingCourse world) {

        if (world.getHeightAt(x, y) <= 0)
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

}

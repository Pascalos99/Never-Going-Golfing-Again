package com.mygdx.game;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;

import java.util.List;
import java.util.Vector;

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
            velocity = verlet(new Vector2d(x, y), velocity, delta);

            Function2d h = WORLD.get_height();
            Vector2d gradient = h.gradient(new Vector2d(x, y));

            if (velocity.get_length() < VELOCITY_CUTTOFF && gradient.get_length() < GRADIENT_CUTTOFF) {
                velocity = new Vector2d(0, 0);
                is_moving = false;
            }

            x += velocity.get_x();
            y += velocity.get_y();

            if (x < BALL_RADIUS) {

                if (velocity.get_length() < VELOCITY_CUTTOFF){
                    is_moving = false;
                    velocity = new Vector2d(0,0);
                }

                else {
                    x = (BALL_RADIUS + 0.001 / WORLD_SCALING);
                    velocity = (new Vector2d(-velocity.get_x()/2d, velocity.get_y()));
                }

            }

            if (x > (50 / WORLD_SCALING - BALL_RADIUS)) {

                if (velocity.get_length() < VELOCITY_CUTTOFF){
                    is_moving = false;
                    velocity = new Vector2d(0,0);
                }

                else {
                    x = (49.99 / WORLD_SCALING - BALL_RADIUS);
                    velocity = (new Vector2d(-velocity.get_x()/2d, velocity.get_y()));
                }

            }

            if (y < BALL_RADIUS) {

                if (velocity.get_length() < VELOCITY_CUTTOFF){
                    is_moving = false;
                    velocity = new Vector2d(0,0);
                }

                else {
                    y = (BALL_RADIUS + 0.001 / WORLD_SCALING);
                    velocity = (new Vector2d(velocity.get_x(), -velocity.get_y()/2d));
                }

            }

            if (y > (50 / WORLD_SCALING - BALL_RADIUS)) {

                if (velocity.get_length() < VELOCITY_CUTTOFF){
                    is_moving = false;
                    velocity = new Vector2d(0,0);
                }

                else {
                    y = (49.99 / WORLD_SCALING - BALL_RADIUS);
                    velocity = (new Vector2d(velocity.get_x(), -velocity.get_y()/2d));
                }

            }

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
        if (Math.sqrt(((x - flag.get_x()) * (x - flag.get_x()) + (y - flag.get_y()) * (y - flag.get_y()))) < _r + r) {
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
        addVelocity(correctHitVector(direction, speed));
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

    private Vector2d f(Vector2d pos, Vector2d vel){
        Function2d h = WORLD.get_height();
        double gravity = WORLD.get_gravity();
        double friction = getCorrectedFriction();

        if(isOnWater()){
            friction = 6d;
        }

        Vector2d gradient = h.gradient(pos);
        double half_x = -mass * gravity * gradient.get_x();
        double half_y = -mass * gravity * gradient.get_y();

        if (vel.get_length() > 0) {
            half_x -= mass * gravity * friction * vel.get_x() / vel.get_length();
            half_y -= mass * gravity * friction * vel.get_y() / vel.get_length();
        }

        Vector2d acceleration = new Vector2d(half_x, half_y);
        return acceleration;
    }

    private Vector2d euler(Vector2d pos, Vector2d vel, double h){
        Vector2d acc = f(pos, vel);

        double vel_x = vel.get_x() + h * acc.get_x();
        double vel_y = vel.get_y() + h * acc.get_y();

        return new Vector2d(vel_x, vel_y);
    }

    private Vector2d RungeKutta(Vector2d pos, Vector2d vel, double h){
        Vector2d k1 = f(pos, vel);
        Vector2d k2 = f(
                new Vector2d(pos.get_x() + h/2d, pos.get_y() + h/2d),
                new Vector2d(vel.get_x() + (h/2d) * k1.get_x(), vel.get_y() + (h/2d) * k1.get_y())
        );
        Vector2d k3 = f(
                new Vector2d(pos.get_x() + h/2d, pos.get_y() + h/2d),
                new Vector2d(vel.get_x() + (h/2d) * k2.get_x(), vel.get_y() + (h/2d) * k2.get_y())
        );
        Vector2d k4 = f(
                new Vector2d(pos.get_x() + h, pos.get_y() + h),
                new Vector2d(vel.get_x() + h * k3.get_x(), vel.get_y() + h * k3.get_y())
        );

        Vector2d G = new Vector2d(
                (k1.get_x() + 2*k2.get_x() + 2*k3.get_x() + k4.get_x()) / 6d,
                (k1.get_y() + 2*k2.get_y() + 2*k3.get_y() + k4.get_y()) / 6d
        );

        return new Vector2d(
               vel.get_x() + h*G.get_x(),
               vel.get_y() + h*G.get_y()
        );
    }

    private Vector2d verlet(Vector2d pos, Vector2d vel, double h){
        Vector2d k1 = f(pos, vel);
        Vector2d k2 = f(
                new Vector2d(pos.get_x() + h, pos.get_y() + h),
                vel
        );
        Vector2d acc = new Vector2d((k1.get_x() + k2.get_x())*h/2d, (k1.get_y() + k2.get_y())*h/2d);
        return new Vector2d(vel.get_x() + acc.get_x(), vel.get_y() + acc.get_y());
    }

    public double getCorrectedFriction(){
        // This is a provisional function, it will later need x, y coodinates
        // to work with variable friction, but the idea and use style will remain.

        return WORLD.get_friction_coefficient() * FRICTION_CORRECTION;
    }

    public Vector2d correctHitVector(Vector2d direction, double speed){
        return new Vector2d(direction.get_x() * speed * SPEED_CORRECTION, direction.get_y() * speed * SPEED_CORRECTION);
    }

}

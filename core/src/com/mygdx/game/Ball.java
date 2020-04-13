package com.mygdx.game;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import static com.mygdx.game.Variables.CAMERA;
import static com.mygdx.game.Variables.BALL_RADIUS;
import static com.mygdx.game.Variables.VELOCITY_CUTTOFF;
import static com.mygdx.game.Variables.GRADIENT_CUTTOFF;

import java.util.List;

public class Ball implements TopDownPhysicsObject {
    public Vector2d velocity;
    public double x, y;
    public float realX, realY, realZ;
    public static float worldScaling = 8.0f;
    public double r;
    private boolean isMoving = true;
    private ModelInstance model;

    private final boolean EXPERIMENTAL_CLIPPING_CORRECTION = false;

    Ball(double radius, double x_pos, double y_pos, ModelInstance model) {
        x = x_pos;
        y = y_pos;
        r = radius;
        velocity = new Vector2d(0, 0);
        this.model = model;
    }

    public void step(double delta, PuttingCourse world, List<TopDownPhysicsObject> ents) {
        Function2d h = world.get_height();
        double gravity = world.get_gravity();
//        double friction = world.get_friction_coefficient();//world.getFrictionAt(x, y);
        double friction = 1;//world.getFrictionAt(x, y);
        double mass = 1 / 8f;

        Vector2d gradients = h.gradient(new Vector2d(x, y));
        double half_x = -mass * gravity * gradients.get_x();
        double half_y = -mass * gravity * gradients.get_y();

        if (velocity.get_length() > 0) {
            half_x -= mass * gravity * friction * velocity.get_x() / velocity.get_length();
            half_y -= mass * gravity * friction * velocity.get_y() / velocity.get_length();
        }

        Vector2d acceleration = new Vector2d(half_x, half_y);

        velocity = new Vector2d(
                velocity.get_x() + acceleration.get_x() * delta,
                velocity.get_y() + acceleration.get_y() * delta);

        Vector2d gradient = world.height_function.gradient(x, y);

        if (velocity.get_length() < VELOCITY_CUTTOFF && gradient.get_length() < GRADIENT_CUTTOFF) {
            velocity = new Vector2d(0,0);
            isMoving = false;
        }

        x += velocity.get_x() * delta;
        y += velocity.get_y() * delta;
    }


    public void addVelocity(Vector2d v) {
        addVelocity(v.get_x(), v.get_y());
    }
    public void addVelocity(double dx, double dy) {
        isMoving = true;
        velocity = velocity.add(dx, dy);
    }

    public boolean isMoving() {
        return isMoving;
    }

    @Override
    public Vector3d getPosition(PuttingCourse world) {

        if (EXPERIMENTAL_CLIPPING_CORRECTION) {
            Function2d h = world.get_height();
            Vector2d gradients = h.gradient(new Vector2d(x, y));

            double mx = gradients.get_x();
            double my = gradients.get_y();
            double z = 1;

            Vector3d n = new Vector3d(-mx, 1, -my);
            double nx = n.get_x() / n.get_length();
            double ny = n.get_y() / n.get_length();
            double nz = n.get_z() / n.get_length();

            Vector3d pos = new Vector3d(x + nx * r, y + ny * r, h.evaluate(x, y) + nz * r);
            return pos;
        }

        return new Vector3d(x, y, (world.get_height().evaluate(new Vector2d(x, y)) + BALL_RADIUS*2.0));
    }

    @Override
    public ModelInstance getModel(PuttingCourse world) {
        float oldX=realX;
        float oldY=realY;
        float oldZ=realZ;
        Vector3d pos = getPosition(world);
        realX = (float) pos.get_x() * worldScaling;
        realY = (float) pos.get_z();
        realZ = (float) pos.get_y() * worldScaling;
        model.transform.setTranslation(realX, realY, realZ);

        CAMERA.translate(new Vector3(realX-oldX,realY-oldY,realZ-oldZ));
        CAMERA.lookAt(realX,realY,realZ);
        CAMERA.update();

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

    public void hit(double direction, double speed) {

    }

}

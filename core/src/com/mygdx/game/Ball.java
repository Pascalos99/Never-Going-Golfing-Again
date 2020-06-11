package com.mygdx.game;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.courses.PuttingCourse;
import com.mygdx.game.obstacles.AxisAllignedBoundingBox;
import com.mygdx.game.parser.Function2d;
import com.mygdx.game.physics.PhysicsEngine;
import com.mygdx.game.physics.PuttingCoursePhysics;
import com.mygdx.game.physics.TopDownPhysicsObject;
import com.mygdx.game.utils.Vector2d;
import com.mygdx.game.utils.Vector3d;

import java.util.List;

import static com.mygdx.game.utils.Variables.*;

public class Ball implements TopDownPhysicsObject {
    public Vector2d velocity;
    public double x, y, old_x, old_y, init_x, init_y;
    public double mass = 0.05;
    private ModelInstance model;
    public boolean is_moving = false;
    public Player owner;
    public int hit_count;
    public int turn_state;

    private double height_velocity;
    private int flight_state;
    private double height;

    private static int LAUNCH = 0;
    private static int ROLL = 2;

    private PuttingCourse world;
    private PuttingCoursePhysics engine;

    public double travel_distance;
    public double rolling_distance;
    public int ticks;
    public Vector3 frozen_direction;

    Ball(double x_pos, double y_pos, ModelInstance model, Player owner) {
        this.mass = GAME_ASPECTS.getMassofBall() * 0.001;

        x = x_pos;
        y = y_pos;
        velocity = new Vector2d(0, 0);
        hit_count = 0;
        this.model = model;
        this.owner = owner;
        init_x = x;
        init_y = y;
        old_x = x;
        old_y = y;
        turn_state = 0;

        flight_state = ROLL;
        height_velocity = 0;

        travel_distance = 0d;
        rolling_distance = 0d;
        ticks = 0;
    }

    public void step(double delta, List<TopDownPhysicsObject> ents) {

        if(is_moving) {
            Function2d h = world.get_height();
            Vector2d gradient = h.gradient(new Vector2d(x, y));
            double test_x = x;
            double test_y = y;
            ticks += 1;

            if(flight_state == ROLL) {
                switch (CURRENT_PHYSICS_SETTING) {
                    case Euler:
                        velocity = euler(new Vector2d(x, y), velocity, delta);
                        break;
                    case Verlet:
                        velocity = verlet(new Vector2d(x, y), velocity, delta);
                        break;
                    case Runge_Kutta:
                        velocity = runge_kutta(new Vector2d(x, y), velocity, delta);
                        break;
                }

                x += velocity.get_x();
                y += velocity.get_y();
                boolean fence_check = ballVsFenceCollision();
                height = h.evaluate(x, y);

                double height_difference = h.evaluate(x, y) - h.evaluate(test_x, test_y);
                height_velocity = height_difference + delta * (-mass * flightGravity());

                frozen_direction = new Vector3((float)velocity.get_x(), (float)height_velocity, (float)velocity.get_y());

                travel_distance += (new Vector3((float)velocity.get_x(), 0f, (float)velocity.get_y())).len();
                rolling_distance += (new Vector3((float)velocity.get_x(), (float)height_velocity, (float)velocity.get_y())).len();

                if(fence_check && velocity.get_length() < VELOCITY_CUTTOFF){
                    is_moving = false;
                    velocity = new Vector2d(0,0);
                }

                else if(velocity.get_length() < VELOCITY_CUTTOFF && gradient.get_length() < GRADIENT_CUTTOFF){
                    is_moving = false;
                    velocity = new Vector2d(0, 0);
                }

                if (ALLOW_FLIGHT) {

                    if (height_velocity > 0 && gradientTest(h, new Vector2d(test_x, test_y), new Vector2d(x, y)) && velocity.get_length() > 0d)
                        flight_state = LAUNCH;

                }

            }

            else if(flight_state == LAUNCH){
                double vel_x = velocity.get_x();
                double vel_y = velocity.get_y();

                vel_x = vel_x + delta * (-vel_x * AIR_FRICTION);
                vel_y = vel_y + delta * (-vel_y * AIR_FRICTION);
                velocity = new Vector2d(vel_x, vel_y);

                x += velocity.get_x();
                y += velocity.get_y();
                ballVsFenceCollision();

                height_velocity = height_velocity + delta * (-mass * flightGravity());
                height += height_velocity;

                if(height <= h.evaluate(x, y)){
                    height = h.evaluate(x, y);
                    flight_state = ROLL;
                    height_velocity = 0;
                }

                frozen_direction = new Vector3((float)velocity.get_x(), (float)height_velocity, (float)velocity.get_y());

                travel_distance += (new Vector3((float)velocity.get_x(), 0f, (float)velocity.get_y())).len();
                rolling_distance += (new Vector3((float)velocity.get_x(), (float)height_velocity, (float)velocity.get_y())).len();

            }

            Vector2d start = new Vector2d(test_x, test_y);
            Vector2d end = new Vector2d(x, y);
            int steps = 3;

            for (int i = 0; i <= steps; i++) {
                    Vector2d xy = interpolate(start, end, steps, i);
                    x = xy.get_x();
                    y = xy.get_y();

                    if (isStuck()) {
                        is_moving = false;
                        velocity = new Vector2d(0, 0);
                        break;
                    }

            }

        }

    }

    public int getTick_count() {
        return ticks;
    }

    private boolean ballVsFenceCollision(){
        boolean r = false;

        if (x < BALL_RADIUS) {
            r = true;

            x = (BALL_RADIUS + 0.001 / WORLD_SCALING);
            velocity = (new Vector2d(-velocity.get_x()/2d, velocity.get_y()));
        }

        if (x > (50*GRAPHICS_SCALING / WORLD_SCALING - BALL_RADIUS)) {
            r = true;

            x = (49.99*GRAPHICS_SCALING / WORLD_SCALING - BALL_RADIUS);
            velocity = (new Vector2d(-velocity.get_x()/2d, velocity.get_y()));
        }

        if (y < BALL_RADIUS) {
            r = true;

            y = (BALL_RADIUS + 0.001 / WORLD_SCALING);
            velocity = (new Vector2d(velocity.get_x(), -velocity.get_y()/2d));
        }

        if (y > (50*GRAPHICS_SCALING / WORLD_SCALING - BALL_RADIUS)) {
            r = true;

            y = (49.99*GRAPHICS_SCALING / WORLD_SCALING - BALL_RADIUS);
            velocity = (new Vector2d(velocity.get_x(), -velocity.get_y()/2d));
        }

        return r;
    }

    private  boolean gradientTest(Function2d height_f, Vector2d old, Vector2d xy){
        Vector2d initial_gradient = height_f.gradient(old);
        Vector2d final_gradient = height_f.gradient(xy);

        Vector2d difference = xy.sub(old);
        double dx = difference.get_x() / Math.abs(difference.get_x());
        double dy = difference.get_y() / Math.abs(difference.get_y());
        Vector2d direction = new Vector2d(dx, dy);

        boolean c1 = initial_gradient.get_x() * direction.get_x() > final_gradient.get_x() * direction.get_x();
        boolean c2 = initial_gradient.get_y() * direction.get_y() > final_gradient.get_y() * direction.get_y();

        return c1 || c2;
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
        Vector3d vec = new Vector3d(
                toWorldScale(x),
                toWorldScale(world.get_height().evaluate(x, y) + BALL_RADIUS),
                toWorldScale(y)
        );

        if(flight_state == LAUNCH)
            vec = new Vector3d(vec.get_x(), toWorldScale(height + BALL_RADIUS), vec.get_z());

        return vec;
    }
    public Vector2d topDownPosition() {
        return new Vector2d(x, y);
    }

    @Override
    public ModelInstance getModel() {
        Vector3d real_pos = getPosition();
        model.transform.setTranslation((float) real_pos.get_x(), (float) real_pos.get_y(), (float) real_pos.get_z());
        return model;
    }

    @Override
    public TopDownPhysicsObject dupe() {
        Ball out = new Ball(x, y, null, null);

        out.flight_state = this.flight_state;
        out.velocity = this.velocity;
        out.height_velocity = this.height_velocity;
        out.height = this.height;
        out.frozen_direction = this.frozen_direction;
        out.engine = (PuttingCoursePhysics) engine.dupe();
        out.world = world;

        return out;
    }

    @Override
    public double getOrientation() {
        return 0;
    }

    public boolean isTouchingFlag() {
        Vector2d flag = world.get_flag_position();
        Vector2d ballPos = new Vector2d(x,y);
        double _r = world.get_hole_tolerance();
        if(flag.distance(ballPos) < _r) {
          return true;
        }

        return false;
    }

    public boolean isOnWater() {

        if (world.getHeightAt(x, y) <= 0 && flight_state == ROLL)
            return true;

        return false;
    }

    public boolean isStuck(){

        if(isOnWater())
            return true;
        return false;
    }

    public void hit(Vector2d direction, double speed){
        addVelocity(correctHitVector(direction, speed));
        hit_count += 1;
        old_x = x;
        old_y = y;
        turn_state = TURN_STATE_WAIT;
        travel_distance = 0d;
        rolling_distance = 0d;
        ticks = 0;
    }

    public void rewind(){
        x = old_x;
        y = old_y;
    }

    private Vector2d f(Vector2d pos, Vector2d vel){
        Function2d h = world.get_height();
        double gravity = world.get_gravity();
        double friction = world.get_friction_coefficient();

        if(isOnWater()){
            friction = 1d;
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

    private Vector2d verlet(Vector2d pos, Vector2d vel, double h){
        Vector2d k1 = f(pos, vel);
        Vector2d k2 = f(
                new Vector2d(pos.get_x() + h, pos.get_y() + h),
                vel
        );
        Vector2d acc = new Vector2d((k1.get_x() + k2.get_x())*h/2d, (k1.get_y() + k2.get_y())*h/2d);
        return new Vector2d(vel.get_x() + acc.get_x(), vel.get_y() + acc.get_y());
    }

    private Vector2d runge_kutta(Vector2d pos, Vector2d vel, double h){
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

    public double flightGravity(){
        return world.get_gravity() * 8d;
    }

    public static Vector2d correctHitVector(Vector2d direction, double speed){
        return new Vector2d(direction.get_x() * speed * SPEED_CORRECTION, direction.get_y() * speed * SPEED_CORRECTION);
    }

    public Ball simulateHit(Vector2d direction, double speed, int ticks, double h){
        PuttingCoursePhysics phy = (PuttingCoursePhysics) engine.dupe();
        Ball ball = (Ball) this.dupe();
        phy.addBody(ball);
        phy.useFixedDelta(true, h);

        ball.hit(direction, speed);

        for(int i = 0; i < ticks; i++){
            phy.frameStep(0);

            if(!ball.is_moving)
                break;

        }

        return ball;
    }

    public Ball resumeSimulatedHit(int ticks, double h){
        Vector2d direction = new Vector2d(frozen_direction.x, frozen_direction.z);
        double speed = direction.get_length();
        return this.simulateHit(direction.normalize(), speed, ticks, h);
    }

    private static Vector2d interpolate(Vector2d start, Vector2d end, int steps, int step){
        double t = (1d / (double)steps) * ((double)step);

        Vector2d xy = new Vector2d(
                start.get_x() + (end.get_x() - start.get_x()) * t,
                start.get_y() + (end.get_y() - start.get_y()) * t
        );

        return xy;
    }

    @Override
    public void setWorld(PuttingCourse world, PhysicsEngine engine){
        this.world = world;
        this.engine = (PuttingCoursePhysics) engine;
    }

    public AxisAllignedBoundingBox getBoundingBox() {
        // TODO implementation
        return null;
    }

}

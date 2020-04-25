package com.mygdx.game;

import com.badlogic.gdx.graphics.g3d.ModelInstance;

import java.util.List;

import static com.mygdx.game.Variables.*;

public class Ball implements TopDownPhysicsObject {
    public Vector2d velocity;
    public double x, y, old_x, old_y, init_x, init_y;
    public double r;
    public double mass = 0.005;
    private ModelInstance model;
    public boolean is_moving = false;
    public Player owner;
    public int hit_count;
    public int turn_state;

    private double height_velocity;
    private int flight_state;
    private double height;

    private static int LAUNCH = 0;
    private static int FALL = 1;
    private static int ROLL = 2;

    Ball(double radius, double x_pos, double y_pos, ModelInstance model, Player owner) {
        x = x_pos;
        y = y_pos;
        r = radius;
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
    }

    public void step(double delta, List<TopDownPhysicsObject> ents) {

        if(is_moving) {
            Function2d h = WORLD.get_height();
            Vector2d gradient = h.gradient(new Vector2d(x, y));
            double test_x = x;
            double test_y = y;

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
                height_velocity = height_difference + delta * (-mass * getCorrectedGravity());

                if(fence_check && velocity.get_length() < VELOCITY_CUTTOFF){
                    is_moving = false;
                    velocity = new Vector2d(0,0);
                }

                else if(velocity.get_length() < VELOCITY_CUTTOFF && gradient.get_length() < GRADIENT_CUTTOFF){
                    is_moving = false;
                    velocity = new Vector2d(0, 0);
                }

                if (ALLOW_FLIGHT)
                    if(height_velocity > 0 && gradientTest(h, new Vector2d(test_x, test_y), new Vector2d(x, y)) && velocity.get_length() > 0d)
                        flight_state = LAUNCH;

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

                height_velocity = height_velocity + delta * (-mass * getCorrectedGravity());
                height += height_velocity;

                if(height <= h.evaluate(x, y)){
                    height = h.evaluate(x, y);
                    flight_state = ROLL;
                    height_velocity = 0;
                }

            }

            Vector2d start = new Vector2d(test_x, test_y);
            Vector2d end = new Vector2d(x, y);
            int steps = 3;

            for(int i = 0; i <= steps; i++){
                Vector2d xy = interpolate(start, end, steps, i);
                x = xy.get_x();
                y = xy.get_y();

                if(isStuck()) {
                    is_moving = false;
                    velocity = new Vector2d(0, 0);
                    break;
                }

            }

        }

    }

    private boolean ballVsFenceCollision(){
        boolean r = false;

        if (x < BALL_RADIUS) {
            r = true;

            x = (BALL_RADIUS + 0.001 / WORLD_SCALING);
            velocity = (new Vector2d(-velocity.get_x()/2d, velocity.get_y()));
        }

        if (x > (50 / WORLD_SCALING - BALL_RADIUS)) {
            r = true;

            x = (49.99 / WORLD_SCALING - BALL_RADIUS);
            velocity = (new Vector2d(-velocity.get_x()/2d, velocity.get_y()));
        }

        if (y < BALL_RADIUS) {
            r = true;

            y = (BALL_RADIUS + 0.001 / WORLD_SCALING);
            velocity = (new Vector2d(velocity.get_x(), -velocity.get_y()/2d));
        }

        if (y > (50 / WORLD_SCALING - BALL_RADIUS)) {
            r = true;

            y = (49.99 / WORLD_SCALING - BALL_RADIUS);
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
                WORLD.get_height().evaluate(x, y) + BALL_RADIUS * WORLD_SCALING,
                toWorldScale(y)
        );

        if(flight_state == LAUNCH)
            vec = new Vector3d(vec.get_x(), height, vec.get_z());

        return vec;
    }

    @Override
    public ModelInstance getModel() {
        Vector3d real_pos = getPosition();
        model.transform.setTranslation((float) real_pos.get_x(), (float) real_pos.get_y(), (float) real_pos.get_z());
        return model;
    }

    @Override
    public TopDownPhysicsObject dupe() {
        return new Ball(r, x, y, null, null);
    }

    @Override
    public double getOrientation() {
        return 0;
    }

    public boolean isTouchingFlag() {
        Vector2d flag = WORLD.get_flag_position();
        Vector2d ballPos = new Vector2d(x,y);
        double _r = WORLD.get_hole_tolerance();
        if(flag.distance(ballPos) < _r) {
          return true;
        }

        return false;
    }

    public boolean isOnWater() {

        if (WORLD.getHeightAt(x, y) <= 0 && flight_state == ROLL)
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
    }

    public void rewind(){
        x = old_x;
        y = old_y;
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

    public double getCorrectedFriction(){
        // This is a provisional function, it will later need x, y coodinates
        // to work with variable friction, but the idea and use style will remain.

        return WORLD.get_friction_coefficient() * FRICTION_CORRECTION;
    }

    public double getCorrectedGravity(){
        return WORLD.get_gravity() * GRAVITY_CORRECTION;
    }

    public Vector2d correctHitVector(Vector2d direction, double speed){
        return new Vector2d(direction.get_x() * speed * SPEED_CORRECTION, direction.get_y() * speed * SPEED_CORRECTION);
    }

    public Ball simulateHit(Vector2d direction, double speed){
        return simulateHit(direction, speed, 2000);
    }

    public Ball simulateHit(Vector2d direction, double speed, int ticks){
        PuttingCoursePhysics phy = GAME.isolate(owner);
        phy.useFixedDelta(true, 0.1);

        Ball ball = CrazyPutting.findIsolatedBall(phy);
        ball.hit(direction, speed);

        for(int i = 0; i < ticks; i++){
            phy.frameStep(0);

            if(!ball.is_moving)
                break;

        }

        return ball;
    }

    private static Vector2d interpolate(Vector2d start, Vector2d end, int steps, int step){
        double t = (1d / (double)steps) * ((double)step);

        Vector2d xy = new Vector2d(
                start.get_x() + (end.get_x() - start.get_x()) * t,
                start.get_y() + (end.get_y() - start.get_y()) * t
        );

        return xy;
    }

}

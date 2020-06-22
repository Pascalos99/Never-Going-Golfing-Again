package com.mygdx.game;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.courses.MiniMapDrawer;
import com.mygdx.game.obstacles.AxisAllignedBoundingBox;
import com.mygdx.game.obstacles.CollisionData;
import com.mygdx.game.obstacles.Obstacle;
import com.mygdx.game.parser.Function2d;
import com.mygdx.game.physics.PuttingCoursePhysics;
import com.mygdx.game.physics.TopDownPhysicsObject;
import com.mygdx.game.utils.Vector2d;
import com.mygdx.game.utils.Vector3d;

import java.util.ArrayList;
import java.util.List;

import static com.mygdx.game.utils.Variables.*;

import java.util.AbstractMap.SimpleEntry;

public class Ball extends TopDownPhysicsObject {
    private static int LAUNCH = 0;
    private static int ROLL = 2;

    public double mass;
    public Vector3d velocity;
    public Vector3d position, previous_position, initial_position;

    public boolean is_moving;
    private int flight_state;

    public ModelInstance model;
    public Player owner;
    public int hit_count;
    public int turn_state;

    public double travel_distance;
    public double rolling_distance;
    public int ticks;
    public Vector3 frozen_direction;

    private List<CollisionData> global_collisions;

    Ball(double x, double y, ModelInstance model, Player owner) {
        this.mass = GAME_ASPECTS.getMassofBall() * 0.001;

        this.model = model;
        this.owner = owner;
        hit_count = 0;
        turn_state = 0;

        is_moving = false;
        flight_state = ROLL;

        travel_distance = 0d;
        rolling_distance = 0d;
        ticks = 0;

        global_collisions = new ArrayList<CollisionData>();

        position = new Vector3d(x, evalHeightAt(x, y) + BALL_RADIUS, y);
        velocity = new Vector3d(0, 0, 0);
        initial_position = position;
        previous_position = position;
    }

    // DEBUGGING CODE TODO check time consumption per part of physics simulation step;
    // TODO build estimate simulation method that minimizes time consumption by simplifying the expensive parts of the computation
    private long last_time_ns = 0;
    private void startTimeCalc() {
        last_time_ns = System.nanoTime();
    }
    private long getTimeSpent() {
        return System.nanoTime() - last_time_ns;
    }
    public static boolean DEBUG = true;
    private static void debug(String str) {
        if (DEBUG) System.out.println("[PHYSICS]: "+str);
    }
    private static void debug(String format, Object... parameters) {
        debug(String.format(format, parameters));
    }
    // END OF DEBUGGING CODE

    public void step(double delta, List<TopDownPhysicsObject> ents) {

        if(is_moving) {
            global_collisions = isColliding();
            boolean fence_check = ballVsFenceCollision();
            ticks += 1;

            Vector2d gradients = evalGradientsAt(position.get_x(), position.get_z());
            Vector3d initial_step_pos = position;

            Vector3d[] pair = new Vector3d[]{
                    position,
                    velocity
            };

            switch (CURRENT_PHYSICS_SETTING) {
                case Euler:
                    pair = eulerStep(ticks*delta, delta, pair, flight_state);
                    break;
                case Verlet:
                    throw new AssertionError("Verlet solver is still not implemented.");
                case Runge_Kutta:
                    throw new AssertionError("Runge-Kutta solver is still not implemented.");
            }

            if(flight_state == ROLL) {
                position = new Vector3d(
                        pair[0].get_x(),
                        evalHeightAt(pair[0].get_x(), pair[0].get_z()) + BALL_RADIUS,
                        pair[0].get_z()
                );
                velocity = new Vector3d(
                        pair[1].get_x(),
                        (position.get_y() - initial_step_pos.get_y())/delta,
                        pair[1].get_z()
                );

                if((fence_check || gradients.get_length() < GRADIENT_CUTTOFF) && velocity.get_length() < VELOCITY_CUTTOFF){
                    is_moving = false;
                    velocity = new Vector3d(0, 0, 0);
                }

                if (ALLOW_FLIGHT && (velocity.get_y() > 0)) {
                    flight_state = LAUNCH;
                }

            }

            else if(flight_state == LAUNCH){
                position = pair[0];
                velocity = pair[1];

                if(position.get_y() - BALL_RADIUS <= evalHeightAt(position.get_x(), position.get_z())){
                    position = new Vector3d(
                            position.get_x(),
                            evalHeightAt(position.get_x(), position.get_z()) + BALL_RADIUS,
                            position.get_z()
                    );
                    flight_state = ROLL;
                    velocity = new Vector3d(
                            velocity.get_x(),
                            0,
                            velocity.get_z()
                    );
                }

            }

            frozen_direction = new Vector3((float)velocity.get_x(), (float)velocity.get_y(), (float)velocity.get_z());

            travel_distance += (new Vector2d(initial_step_pos.get_x(), initial_step_pos.get_z())).distance(new Vector2d(position.get_x(), position.get_z()));
            rolling_distance += initial_step_pos.distance(position);

            global_collisions.clear();

            Vector3d start = initial_step_pos;
            Vector3d end = position;
            int steps = 3;

            for (int i = 0; i <= steps; i++) {
                    position = interpolate(start, end, steps, i);

                    if (isStuck()) {
                        is_moving = false;
                        velocity = new Vector3d(0, 0, 0);
                        break;
                    }

            }

        }

    }

    private boolean ballVsFenceCollision(){
        boolean r = false;

        if (position.get_x() < BALL_RADIUS) {
            r = true;

            position = new Vector3d((BALL_RADIUS + 0.001 / WORLD_SCALING), position.get_y(), position.get_z());
            velocity = new Vector3d(-velocity.get_x()/2d, velocity.get_y(), velocity.get_z());
        }

        if (position.get_x() > (50*GRAPHICS_SCALING / WORLD_SCALING - BALL_RADIUS)) {
            r = true;

            position = new Vector3d((49.99*GRAPHICS_SCALING / WORLD_SCALING - BALL_RADIUS), position.get_y(), position.get_z());
            velocity = new Vector3d(-velocity.get_x()/2d, velocity.get_y(), velocity.get_z());
        }

        if (position.get_z() < BALL_RADIUS) {
            r = true;

            position = new Vector3d(position.get_x(), position.get_y(), (BALL_RADIUS + 0.001 / WORLD_SCALING));
            velocity = new Vector3d(velocity.get_x(), velocity.get_y(), -velocity.get_z()/2d);
        }

        if (position.get_z() > (50*GRAPHICS_SCALING / WORLD_SCALING - BALL_RADIUS)) {
            r = true;

            position = new Vector3d(position.get_x(), position.get_y(), 49.99*GRAPHICS_SCALING / WORLD_SCALING - BALL_RADIUS);
            velocity = new Vector3d(velocity.get_x(), velocity.get_y(), -velocity.get_y()/2d);
        }

        return r;
    }

    public void addVelocity(Vector2d v) {
        is_moving = true;
        velocity = velocity.add(new Vector3d(v.get_x(), 0, v.get_y()));
    }

    @Override
    public Vector3d getGraphicsPosition() {
        return new Vector3d(
                toWorldScale(position.get_x()),
                toWorldScale(position.get_y()),
                toWorldScale(position.get_z())
        );
    }

    @Override
    public Vector3d getPhysicsPosition(){

        if(flight_state == ROLL)
            return new Vector3d(position.get_x(), evalHeightAt(position.get_x(), position.get_z()), position.get_z());

        return position;
    }

    public Vector2d topDownPosition() {
        return new Vector2d(position.get_x(), position.get_z());
    }

    @Override
    public ModelInstance getModel() {
        Vector3d real_pos = getGraphicsPosition();
        model.transform.setTranslation((float) real_pos.get_x(), (float) real_pos.get_y(), (float) real_pos.get_z());
        return model;
    }

    @Override
    public TopDownPhysicsObject dupe() {
        Ball out = new Ball(position.get_x(), position.get_z(), null, null);

        out.flight_state = this.flight_state;
        out.velocity = this.velocity;
        out.frozen_direction = this.frozen_direction;
        out.engine = engine.dupe();
        out.world = world;

        return out;
    }

    public double evalHeightAt(double x, double y){

        for(CollisionData collision : global_collisions){

            if(collision.atop)
                return collision.obstacle.getHeightAt(x, y);

        }

        if(world != null)
            return world.height_function.evaluate(x, y);

        if(WORLD != null)
            return WORLD.height_function.evaluate(x, y);

        throw new AssertionError("No terrain function has been declared yet.");
    }

    public double evalFrictionAt(double x, double y){

        for(CollisionData collision : global_collisions){

            if(collision.atop)
                return collision.obstacle.getFrictionAt(x, y);

        }

        if(world != null)
            return world.friction_function.evaluate(x, y);

        if(WORLD != null)
            return WORLD.friction_function.evaluate(x, y);

        throw new AssertionError("No terrain function has been declared yet.");
    }

    public Vector2d evalGradientsAt(double x, double y){

        for(CollisionData collision : global_collisions){

            if(collision.atop)
                return collision.obstacle.getGradientsAt(x, y);

        }

        if(world != null)
            return world.height_function.gradient(x, y);

        if(WORLD != null)
            return WORLD.height_function.gradient(x, y);

        throw new AssertionError("No terrain function has been declared yet.");
    }

    @Override
    public double getOrientation() {
        return 0;
    }

    public List<CollisionData> isColliding(){
        List<CollisionData> collisions = new ArrayList<CollisionData>();

        for(TopDownPhysicsObject body : world.getObstacles()){

            if(body instanceof Obstacle){
                Obstacle obstacle = (Obstacle) body;

                CollisionData data = obstacle.isColliding(this);

                if(data != null){
                    this.position = position.add(data.clipping_correction);
                    this.velocity = data.bounce;
                    collisions.add(data);
                }

            }

        }

        return collisions;
    }

    public boolean isTouchingFlag() {
        Vector2d flag = world.flag_position;
        Vector2d ballPos = new Vector2d(position.get_x(), position.get_z());
        double _r = world.hole_tolerance;

        if(flag.distance(ballPos) < _r) {
          return true;
        }

        return false;
    }

    public boolean isOnWater() {

        if (position.get_y() <= 0 && flight_state == ROLL)
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
        previous_position = position;
        turn_state = TURN_STATE_WAIT;
        travel_distance = 0d;
        rolling_distance = 0d;
        ticks = 0;
    }

    public void rewind(){
        position = previous_position;
        velocity = new Vector3d(0, 0, 0);
    }

    private Vector3d roll_g(double t, Vector3d pos, Vector3d vel){
        /*
            g(t, x, &x) = -n*g*h'(x) - (m*g*&x)/|&x|
        */
        vel = new Vector3d(vel.get_x(), 0, vel.get_z());

        double friction_eval = isOnWater()? 1d : evalFrictionAt(pos.get_x(), pos.get_z());
        Vector2d gradients_eval = evalGradientsAt(pos.get_x(), pos.get_z());

        double x_acc = -mass*landGravity()*gradients_eval.get_x() - (vel.get_length() > 0? mass*landGravity()*friction_eval*vel.get_x()/vel.get_length() : 0);
        double z_acc = -mass*landGravity()*gradients_eval.get_y() - (vel.get_length() > 0? mass*landGravity()*friction_eval*vel.get_z()/vel.get_length() : 0);

        return new Vector3d(x_acc, 0, z_acc);
    }

    private Vector3d flight_g(double t, Vector3d pos, Vector3d vel){
        /*
            g(t, x, &x) = (&x - &x_old)/h
         */
        return new Vector3d(
                0,
                landGravity(),
                0
        );
    }

    private Vector3d[] f(double t, Vector3d[] pair, int flight_state){
        Vector3d [] out = new Vector3d[2];
        out[0] = pair[1];

        if(flight_state == ROLL)
            out[1] = roll_g(t, pair[0], pair[1]);

        else if(flight_state == LAUNCH)
            out[1] = flight_g(t, pair[0], pair[1]);

        else
            throw new AssertionError("Unknown flight state " + flight_state);

        return out;
    }

    private Vector3d[] eulerStep(double t, double h, Vector3d[] pair, int flight_state){
        /*
            Euler --> y1 = y0 + h*f(t, y0)
        */
        Vector3d[] eval = f(t, pair, flight_state);
        eval = new Vector3d[]{eval[0].scale(h), eval[1].scale(h)};
        return new Vector3d[]{eval[0].add(pair[0]), eval[1].add(pair[1])};
    }

    /*private Vector2d f(Vector2d pos, Vector2d vel){
        Function2d h = world.height_function;
        double gravity = landGravity();
        double friction = world.friction_function.evaluate(x, y);

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
    }*/

    public double flightGravity(){
        return world.gravity*7d;
    }

    public double landGravity() { return world.gravity; }

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

    private static Vector3d interpolate(Vector3d start, Vector3d end, int steps, int step){
        double t = (1d / (double)steps) * ((double)step);

        return new Vector3d(
                start.get_x() + (end.get_x() - start.get_x())*t,
                start.get_y() + (end.get_y() - start.get_y()) * t,
                start.get_z() + (end.get_z() - start.get_z())*t
        );
    }

    public AxisAllignedBoundingBox getBoundingBox() {
        Vector3d physics_pos = getPhysicsPosition();

        return new AxisAllignedBoundingBox(
                    new Vector3d(physics_pos.get_x() - BALL_RADIUS, physics_pos.get_y() - BALL_RADIUS, physics_pos.get_z() - BALL_RADIUS),
                    BALL_RADIUS*2,
                    BALL_RADIUS*2,
                    BALL_RADIUS*2
                    );
    }

    @Override
    public void visit(MiniMapDrawer mapDrawer) {
        mapDrawer.draw(this);
    }

}

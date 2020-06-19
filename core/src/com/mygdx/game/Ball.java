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

public class Ball extends TopDownPhysicsObject {
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
    public double height;

    private static int LAUNCH = 0;
    private static int ROLL = 2;

    public double travel_distance;
    public double rolling_distance;
    public int ticks;
    public Vector3 frozen_direction;

    private List<CollisionData> global_collisions;

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

        global_collisions = new ArrayList<CollisionData>();
    }

    public void step(double delta, List<TopDownPhysicsObject> ents) {

        if(is_moving) {
//            System.out.println("Ball at " + getPhysicsPosition().toString());
            global_collisions = isColliding();

            Function2d h = world.height_function;
            Vector2d gradient = h.gradient(new Vector2d(x, y));
            double initial_x = x;
            double initial_y = y;
            double initial_height = h.evaluate(initial_x, initial_y) + BALL_RADIUS;
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
                height = h.evaluate(x, y) + BALL_RADIUS;

                double x_diff = x - initial_x;
                double y_diff = y - initial_y;

                Vector2d gradients = h.gradient(initial_x, initial_y);
                double predicted_height = initial_height + gradients.get_x()*x_diff + gradients.get_y()*y_diff;

                double final_height = height;

                height_velocity = predicted_height - final_height;
                /*System.out.println("|------------------|");
                System.out.println("landGRavity = " + landGravity());
                System.out.println("landGRavity x delta = " + delta*landGravity());
                System.out.println("predicted_height = " + predicted_height);
                System.out.println("final_height = " + final_height);
                System.out.println("height_velocity = " + height_velocity);
                System.out.println("|------------------|");*/

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

                if (ALLOW_FLIGHT && height_velocity > 0) {
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

                if(height - BALL_RADIUS <= h.evaluate(x, y)){
                    height = h.evaluate(x, y);
                    flight_state = ROLL;
                    height_velocity = 0;
                }

                frozen_direction = new Vector3((float)velocity.get_x(), (float)height_velocity, (float)velocity.get_y());

                travel_distance += (new Vector3((float)velocity.get_x(), 0f, (float)velocity.get_y())).len();
                rolling_distance += (new Vector3((float)velocity.get_x(), (float)height_velocity, (float)velocity.get_y())).len();

            }

            global_collisions.clear();

            Vector2d start = new Vector2d(initial_x, initial_y);
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

    public void addVelocity(Vector2d v) {
        is_moving = true;
        velocity = velocity.add(v);
    }

    @Override
    public Vector3d getGraphicsPosition() {
        Vector3d vec = new Vector3d(
                toWorldScale(x),
                toWorldScale(world.height_function.evaluate(x, y) + BALL_RADIUS),
                toWorldScale(y)
        );

        if(flight_state == LAUNCH)
            vec = new Vector3d(vec.get_x(), toWorldScale(height + BALL_RADIUS), vec.get_z());

        return vec;
    }

    @Override
    public Vector3d getPhysicsPosition(){
        return new Vector3d(x, height, y);
    }

    public Vector2d topDownPosition() {
        return new Vector2d(x, y);
    }

    @Override
    public ModelInstance getModel() {
        Vector3d real_pos = getGraphicsPosition();
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

    public double evalHeightAt(double x, double y){

        for(CollisionData collision : global_collisions){

            if(collision.atop)
                return collision.obstacle.getHeightAt(x, y);

        }

        return world.height_function.evaluate(x, y);
    }

    public double evalFrictionAt(double x, double y){

        for(CollisionData collision : global_collisions){

            if(collision.atop)
                return collision.obstacle.getFrictionAt(x, y);

        }

        return world.friction_function.evaluate(x, y);
    }

    public double evalHeightAt(Vector2d pos, List<CollisionData> collisions){
        return evalHeightAt(pos.get_x(), pos.get_y());
    }

    public double evalFrictionAt(Vector2d pos, List<CollisionData> collisions){
        return evalFrictionAt(pos.get_x(), pos.get_y());
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
                    this.x += data.clipping_correction.get_x();
                    this.y += data.clipping_correction.get_z();
                    this.height += data.clipping_correction.get_y();

                    this.velocity = new Vector2d(data.bounce.get_x(), data.bounce.get_z());
                    this.height_velocity = data.bounce.get_y();

                    collisions.add(data);

//                    System.out.println("Hey! Collision here!");
                }

            }

        }

        return collisions;
    }

    public boolean isTouchingFlag() {
        Vector2d flag = world.flag_position;
        Vector2d ballPos = new Vector2d(x,y);
        double _r = world.hole_tolerance;
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
    }

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

    private static Vector2d interpolate(Vector2d start, Vector2d end, int steps, int step){
        double t = (1d / (double)steps) * ((double)step);

        Vector2d xy = new Vector2d(
                start.get_x() + (end.get_x() - start.get_x()) * t,
                start.get_y() + (end.get_y() - start.get_y()) * t
        );

        return xy;
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

package com.mygdx.game.physics;

import com.mygdx.game.Ball;
import com.mygdx.game.courses.PuttingCourse;

import java.util.ArrayList;
import java.util.List;

import static com.mygdx.game.utils.Variables.*;

public class PuttingCoursePhysics implements PhysicsEngine {
    protected List<TopDownPhysicsObject> ents;
    protected List<Double> deltas;

    protected boolean use_fixed_delta;
    protected double fixed_delta;

    private static int DELTA_ARRAY_SIZE_LIMIT = 5;

    public PuttingCoursePhysics(){
        ents = new ArrayList<TopDownPhysicsObject>();
        deltas = new ArrayList<Double>(DELTA_ARRAY_SIZE_LIMIT);
    }

    public PuttingCoursePhysics(PuttingCourse world){
        ents = new ArrayList<TopDownPhysicsObject>();
        deltas = new ArrayList<Double>(DELTA_ARRAY_SIZE_LIMIT);
    }

    @Override
    public double frameStep(double previous_time) {
        double current_time = System.currentTimeMillis() / 1000.0;
        double delta = current_time - previous_time;
        addDelta(delta);
        double _delta = getDelta();

        if(use_fixed_delta)
            _delta = fixed_delta;

        for(TopDownPhysicsObject obj : ents){

            if(obj instanceof Ball){
                ((Ball) obj).step(_delta, ents);
            }

        }

        return current_time;
    }

    @Override
    public List<TopDownPhysicsObject> getBodies() {
        return ents;
    }

    @Override
    public void addBody(TopDownPhysicsObject obj) {
        obj.setWorld(this);
        ents.add(obj);
    }

    @Override
    public void destroyBody(TopDownPhysicsObject obj) {
        ents.remove(obj);
    }

    @Override
    public PhysicsEngine dupe() {
        PuttingCoursePhysics w = new PuttingCoursePhysics();

        for(TopDownPhysicsObject obj : ents) {

            if(!(obj instanceof Ball))
                w.addBody(obj.dupe());

        }

        return w;
    }

    @Override
    public void useFixedDelta(boolean flag, double delta) {
        use_fixed_delta = flag;
        fixed_delta = delta;
    }

    private void addDelta(double delta){
        deltas.add(delta);

        if(deltas.size() > DELTA_ARRAY_SIZE_LIMIT)
            deltas.remove(0);

    }

    private double getDelta(){
        double track = 0;

        for(Double delta : deltas)
            track += delta.doubleValue();

        return track / (double)deltas.size();
    }

}

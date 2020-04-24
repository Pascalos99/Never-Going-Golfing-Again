package com.mygdx.game;

import java.util.ArrayList;
import java.util.List;

import static com.mygdx.game.Variables.*;

public class PuttingCoursePhysics implements PhysicsEngine {
    public static final double BALL_SIZE = 0.22;
    protected List<TopDownPhysicsObject> ents;
    protected List<Double> deltas;

    private static int DELTA_ARRAY_SIZE_LIMIT = 5;

    PuttingCoursePhysics(){
        ents = new ArrayList<TopDownPhysicsObject>();
        deltas = new ArrayList<Double>(DELTA_ARRAY_SIZE_LIMIT);
    }

    @Override
    public double frameStep(double previous_time) {
        double current_time = System.currentTimeMillis() / 1000.0;
        double delta = current_time - previous_time;
        addDelta(delta);
        double _delta = getDelta();

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
        ents.add(obj);
    }

    @Override
    public void destroyBody(TopDownPhysicsObject obj) {
        ents.remove(obj);
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

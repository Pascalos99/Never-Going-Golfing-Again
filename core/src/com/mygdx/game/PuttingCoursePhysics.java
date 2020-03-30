package com.mygdx.game;

import java.util.ArrayList;
import java.util.List;

public class PuttingCoursePhysics implements PhysicsEngine {
    public static final double BALL_SIZE = 0.22;
    protected List<TopDownPhysicsObject> ents;
    protected PuttingCourse world;

    PuttingCoursePhysics(PuttingCourse world_map){
        this.world = world_map;
        ents = new ArrayList<TopDownPhysicsObject>();
    }

    @Override
    public double frameStep(double previous_time) {
        double current_time = System.currentTimeMillis() / 1000.0;
        double delta = current_time - previous_time;

        for(TopDownPhysicsObject obj : ents){

            if(obj instanceof Ball){
                ((Ball) obj).step(delta, world, ents);
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
}

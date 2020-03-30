package com.mygdx.game;

import java.util.List;

public interface PhysicsEngine {

    public double frameStep(double previous_time);//Simulate physics for one tick/time step
    public List<TopDownPhysicsObject> getBodies();//Return list of all objects in the physics simulation
    public void addBody(TopDownPhysicsObject obj);
    public void destroyBody(TopDownPhysicsObject obj);

}

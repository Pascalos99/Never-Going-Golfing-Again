package com.mygdx.game;

import com.badlogic.gdx.graphics.g3d.ModelInstance;

public interface TopDownPhysicsObject {

    public Vector3d getPosition(PuttingCourse world);
    public double getOrientation();
    public ModelInstance getModel(PuttingCourse world, Player p);

}

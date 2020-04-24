package com.mygdx.game;

import com.badlogic.gdx.graphics.g3d.ModelInstance;

public interface TopDownPhysicsObject {

    public Vector3d getPosition();
    public double getOrientation();
    public ModelInstance getModel();
    public void rewindTime();

}

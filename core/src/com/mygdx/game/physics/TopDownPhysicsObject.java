package com.mygdx.game.physics;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.mygdx.game.courses.PuttingCourse;
import com.mygdx.game.utils.Vector3d;

public interface TopDownPhysicsObject {

    public Vector3d getPosition();
    public double getOrientation();
    public ModelInstance getModel();
    public TopDownPhysicsObject dupe();
    public void setWorld(PuttingCourse world, PhysicsEngine engine);

}

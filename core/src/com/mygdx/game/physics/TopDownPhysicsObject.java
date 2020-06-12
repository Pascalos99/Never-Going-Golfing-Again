package com.mygdx.game.physics;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.mygdx.game.courses.Drawable;
import com.mygdx.game.courses.PuttingCourse;
import com.mygdx.game.obstacles.AxisAllignedBoundingBox;
import com.mygdx.game.utils.Vector3d;

public interface TopDownPhysicsObject extends Drawable {

    Vector3d getPosition();
    double getOrientation();
    ModelInstance getModel();
    TopDownPhysicsObject dupe();
    void setWorld(PuttingCourse world, PhysicsEngine engine);

    public abstract AxisAllignedBoundingBox getBoundingBox();

}

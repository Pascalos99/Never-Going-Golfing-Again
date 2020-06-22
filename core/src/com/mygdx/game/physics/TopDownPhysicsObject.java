package com.mygdx.game.physics;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.mygdx.game.courses.Drawable;
import com.mygdx.game.courses.PuttingCourse;
import com.mygdx.game.obstacles.AxisAllignedBoundingBox;
import com.mygdx.game.utils.Vector3d;

import static com.mygdx.game.utils.Variables.WORLD_SCALING;

public abstract class TopDownPhysicsObject implements Drawable {
    protected PuttingCourse world;
    protected PhysicsEngine engine;

    public abstract Vector3d getGraphicsPosition();
    public abstract Vector3d getPhysicsPosition();

    public abstract double getOrientation();
    public abstract ModelInstance[] getModel();
    public PuttingCourse getWorld(){
        return world;
    }

    public abstract TopDownPhysicsObject dupe();

    public void setWorld(PuttingCourse world, PhysicsEngine engine){
        this.world = world;
        this.engine = (PuttingCoursePhysics) engine;
    }

    public abstract AxisAllignedBoundingBox getBoundingBox();

    public float toWorldScale(double n){
        return (float)(n * WORLD_SCALING);
    }

    public double fromWorldScale(float n){
        return  n / WORLD_SCALING;
    }

}

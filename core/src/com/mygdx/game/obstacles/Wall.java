package com.mygdx.game.obstacles;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.mygdx.game.Ball;
import com.mygdx.game.courses.PuttingCourse;
import com.mygdx.game.physics.PhysicsEngine;
import com.mygdx.game.utils.Vector3d;

public class Wall extends Obstacle {
    @Override
    protected boolean isShapeColliding(Ball ball) {
        return false;
    }

    @Override
    public Vector3d getPosition() {
        throw new AssertionError("Wall has no unique position.");
    }

    @Override
    public double getOrientation() {
        throw new AssertionError("Wall has no unique orientation.");
    }

    @Override
    public ModelInstance getModel() {
        return null;
    }

    @Override
    public void setWorld(PuttingCourse world, PhysicsEngine engine) {
        throw new AssertionError("Obstacle holds no reference to World.");
    }

    @Override
    public AxisAllignedBoundingBox getBoundingBox() {
        return null;
    }
}

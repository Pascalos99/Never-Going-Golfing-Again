package com.mygdx.game.obstacles;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.mygdx.game.Ball;
import com.mygdx.game.physics.TopDownPhysicsObject;

public abstract class Obstacle implements TopDownPhysicsObject {

    public boolean isColliding(Ball ball) {
        AxisAllignedBoundingBox box = getBoundingBox();
        AxisAllignedBoundingBox ball_box = ball.getBoundingBox();
        return box.collides(ball_box) && isShapeColliding(ball);
    }

    protected abstract boolean isShapeColliding(Ball ball);

}

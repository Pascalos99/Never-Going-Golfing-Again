package com.mygdx.game.obstacles;

import com.mygdx.game.Ball;
import com.mygdx.game.physics.TopDownPhysicsObject;
import com.mygdx.game.utils.Vector2d;

public abstract class Obstacle implements TopDownPhysicsObject {

    public void setAnchorPoint(Vector2d position) {
        // TODO for Dennis
    }

    public Vector2d getAnchorPoint() {
        // TODO for Dennis
        return Vector2d.ZERO;
    }

    public boolean isColliding(Ball ball) {
        AxisAllignedBoundingBox box = getBoundingBox();
        AxisAllignedBoundingBox ball_box = ball.getBoundingBox();
        return box.collides(ball_box) && isShapeColliding(ball);
    }

    protected abstract boolean isShapeColliding(Ball ball);

    @Override
    public TopDownPhysicsObject dupe() {
        return (TopDownPhysicsObject)this;
    }

}

package com.mygdx.game.obstacles;

import com.mygdx.game.Ball;
import com.mygdx.game.physics.TopDownPhysicsObject;
import com.mygdx.game.utils.Vector2d;

public abstract class Obstacle extends TopDownPhysicsObject {

    public void setAnchorPoint(Vector2d position) {
        // TODO for Dennis
    }

    public Vector2d getAnchorPoint() {
        // TODO for Dennis
        return Vector2d.ZERO;
    }

    public CollisionData isColliding(Ball ball) {
        AxisAllignedBoundingBox box = getBoundingBox();
        AxisAllignedBoundingBox ball_box = ball.getBoundingBox();

        if(box.collides(ball_box))
            return isShapeColliding(ball);

        return null;
    }

    protected abstract CollisionData isShapeColliding(Ball ball);

    @Override
    public TopDownPhysicsObject dupe() {
        return (TopDownPhysicsObject)this;
    }

}

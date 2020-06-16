package com.mygdx.game.obstacles;

import com.mygdx.game.Ball;
import com.mygdx.game.physics.TopDownPhysicsObject;
import com.mygdx.game.utils.Vector2d;

public abstract class Obstacle extends TopDownPhysicsObject {

    Vector2d anchor = Vector2d.ZERO;

    public void setAnchorPoint(Vector2d position) {
        anchor = position;
    }

    public Vector2d getAnchorPoint() {
        return anchor;
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
//TODO for dennis, problems occured, pls help -AG
    //public abstract double getHeightAt(double x, double y);

//   // public double getHeightAt(Vector2d pos){
//        return getHeightAt(pos.get_x(), pos.get_y());
//    }

}

package com.mygdx.game.obstacles;

import com.mygdx.game.Ball;

public abstract class Obstacle {

    public boolean isColliding(Ball ball) {
        AxisAllignedBoundingBox box = getBoundingBox();
        AxisAllignedBoundingBox ball_box = ball.getBoundingBox();
        return box.collides(ball_box) && isShapeColliding(ball);
    }

    protected abstract boolean isShapeColliding(Ball ball);

    public abstract AxisAllignedBoundingBox getBoundingBox();

}

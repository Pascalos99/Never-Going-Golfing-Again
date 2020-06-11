package com.mygdx.game.obstacles;

import com.mygdx.game.utils.Vector3d;

public class Wall extends Static3DphysicsObject {

    private Vector3d[] points;

    public AxisAllignedBoundingBox getBoundingBox() {
        return new AxisAllignedBoundingBox();
    }


}

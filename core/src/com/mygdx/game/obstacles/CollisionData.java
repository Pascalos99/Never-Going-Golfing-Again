package com.mygdx.game.obstacles;

import com.mygdx.game.utils.Vector2d;
import com.mygdx.game.utils.Vector3d;

public class CollisionData {
    public Vector3d clipping_correction, bounce;
    public boolean atop;
    public Obstacle obstacle;

    CollisionData(Obstacle obstacle){
        clipping_correction = new Vector3d(0, 0, 0);
        bounce = new Vector3d(0, 0, 0);
        atop = false;
        this.obstacle = obstacle;
    }

}

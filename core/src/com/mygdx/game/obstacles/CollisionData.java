package com.mygdx.game.obstacles;

import com.mygdx.game.utils.Vector2d;
import com.mygdx.game.utils.Vector3d;

public class CollisionData {
    public Vector3d clipping_correction;
    public boolean contact;
    public Vector3d bounce;
    public boolean is_on_land;

    CollisionData(){
        clipping_correction = new Vector3d(0, 0, 0);
        contact = false;
        bounce = new Vector3d(0, 0, 0);
        is_on_land = false;
    }

}

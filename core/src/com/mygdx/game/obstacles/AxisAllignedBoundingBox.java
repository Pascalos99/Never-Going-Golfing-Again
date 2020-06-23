package com.mygdx.game.obstacles;

import com.mygdx.game.utils.Vector2d;
import com.mygdx.game.utils.Vector3d;

public class AxisAllignedBoundingBox {

    /** The point at the lowest x, y and z coordinates of this box */
    public Vector2d origin;
    /** The physics-scale length of this box in the x-direction */
    public double width;
    /** The physics-scale length of this box in the y-direction */
    public double depth;

    public AxisAllignedBoundingBox(Vector2d origin, double width, double depth) {
        this.origin = origin;
        this.width = width;
        this.depth = depth;
    }

    public boolean collides(AxisAllignedBoundingBox box) {

        if(origin.get_x() + width < box.origin.get_x())
            return false;

        if(origin.get_y() + depth < box.origin.get_y())
            return false;

        if(box.origin.get_x() + width < origin.get_x())
            return false;

        if(box.origin.get_y() + depth < origin.get_y())
            return false;

        return true;
    }

    @Override
    public String toString(){
        return "AABB from " + origin.toString() + " to " + origin.add(new Vector2d(width, depth)).toString();
    }

}

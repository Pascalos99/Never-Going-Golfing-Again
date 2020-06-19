package com.mygdx.game.obstacles;

import com.mygdx.game.utils.Vector3d;

public class AxisAllignedBoundingBox {

    /** The point at the lowest x, y and z coordinates of this box */
    public Vector3d origin;
    /** The physics-scale length of this box in the x-direction */
    public double width;
    /** The physics-scale length of this box in the y-direction */
    public double depth;
    /** The physics-scale length of this box in the z-direction */
    public double height;

    public AxisAllignedBoundingBox(Vector3d origin, double width, double height, double depth) {
        this.origin = origin;
        this.width = width;
        this.depth = depth;
        this.height = height;
    }

    public boolean collides(AxisAllignedBoundingBox box) {

        if(origin.get_x() + width < box.origin.get_x())
            return false;

        if(origin.get_y() + height < box.origin.get_y())
            return false;

        if(origin.get_z() + depth < box.origin.get_z())
            return false;

        if(box.origin.get_x() + width < origin.get_x())
            return false;

        if(box.origin.get_y() + height < origin.get_y())
            return false;

        if(box.origin.get_z() + depth < origin.get_z())
            return false;

        return true;
    }

    @Override
    public String toString(){
        return "AABB from " + origin.toString() + " to " + origin.add(new Vector3d(width, height, depth)).toString();
    }

}

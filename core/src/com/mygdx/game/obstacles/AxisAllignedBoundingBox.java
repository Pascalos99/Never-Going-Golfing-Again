package com.mygdx.game.obstacles;

import com.mygdx.game.utils.Vector3d;

public class AxisAllignedBoundingBox {

    /** The point at the lowest x, y and z coordinates of this box. */
    Vector3d origin;
    /** The physics-scale length of this box in the x-direction */
    double width;
    /** The physics-scale length of this box in the y-direction */
    double depth;
    /** The physics-scale length of this box in the z-direction */
    double height;

    public AxisAllignedBoundingBox(Vector3d origin, double width, double depth, double height) {
        this.origin = origin;
        this.width = width;
        this.depth = depth;
        this.height = height;
    }

    public boolean collides(AxisAllignedBoundingBox box) {
        return true;
    }

}

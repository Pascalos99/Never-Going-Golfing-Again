package com.mygdx.game.obstacles;

import com.mygdx.game.utils.Vector3d;

public class AxisAllignedBoundingBox {

    public final Static3DphysicsObject parent;

    public AxisAllignedBoundingBox(Static3DphysicsObject parent) {
        this.parent = parent;
    }

    static class Box {
        /** The point at the lowest x, y and z coordinates of this box. */
        Vector3d origin;
        /** The physics-scale length of this box in the x-direction */
        double width;
        /** The physics-scale length of this box in the y-direction */
        double depth;
        /** The physics-scale length of this box in the z-direction */
        double height;
    }

}

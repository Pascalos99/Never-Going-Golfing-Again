package com.mygdx.game;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class Vector3d {
    private double x, y, z;
    public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public double get_x() {
        return x;
    }
    public double get_y() {
        return y;
    }
    public double get_z(){
        return z;
    }
    public double get_length(){
        return Math.sqrt(x * x + y * y + z * z);
    }

    public void add(Vector3d v){
        x += v.get_x();
        y += v.get_y();
        z += v.get_z();
    }

    public String toString() {
        return String.format("(% f, % f, % f)", x, y, z);
    }
}
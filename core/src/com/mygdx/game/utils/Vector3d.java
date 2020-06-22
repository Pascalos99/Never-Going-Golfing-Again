package com.mygdx.game.utils;

import com.badlogic.gdx.math.Vector3;

public class Vector3d {
    private final double x, y, z;
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

    public Vector3d add(Vector3d v){
        return new Vector3d(x + v.get_x(),y + v.get_y(),z + v.get_z());
    }

    public Vector3d mul(Vector3d v){
        return new Vector3d(x * v.get_x(),y * v.get_y(),z * v.get_z());
    }

    public Vector3d div(Vector3d v){
        return new Vector3d(x / v.get_x(),y / v.get_y(),z / v.get_z());
    }

    public Vector3d normalize() {
        double len = get_length();
        return new Vector3d(x/len, z/len, y/len);
    }

    public Vector3d sub(Vector3d v){
        return new Vector3d(x - v.get_x(),y - v.get_y(),z - v.get_z());
    }

    public  double distance(Vector3d o){
        return  o.sub(this).get_length();
    }

    public Vector3d scale(double scalar){
        return new Vector3d(get_x()*scalar, get_y()*scalar, get_z()*scalar);
    }

    public String toString() {
        return String.format("(% f, % f, % f)", x, y, z);
    }

    public Vector3 toVector3() {
        return new Vector3((float)x, (float)y, (float)z);
    }
}
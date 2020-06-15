package com.mygdx.game.obstacles;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.mygdx.game.Ball;
import com.mygdx.game.courses.MiniMapDrawer;
import com.mygdx.game.courses.PuttingCourse;
import com.mygdx.game.physics.PhysicsEngine;
import com.mygdx.game.utils.Vector2d;
import com.mygdx.game.utils.Vector3d;

import java.awt.*;

public class Wall extends Obstacle {
    Vector2d[] points;
    Vector2d start, end;
    double thickness, angle;

    Wall(Vector2d a, Vector2d b, double thickness){
        Vector2d center = a.add(b).div(new Vector2d(2, 2));
        double length = a.distance(b);

        Vector2d left_point = (new Vector2d(-length/2d, 0));
        Vector2d right_point = (new Vector2d(length/2d, 0));

        Vector2d upper_left_point = left_point.add(new Vector2d(0, thickness/2d));
        Vector2d lower_left_point = left_point.add(new Vector2d(0, -thickness/2d));

        Vector2d upper_right_point = left_point.add(new Vector2d(0, thickness/2d));
        Vector2d lower_right_point = left_point.add(new Vector2d(0, -thickness/2d));

        Vector2d[] vectors = {upper_left_point, upper_right_point, lower_left_point, lower_right_point};

        for(int i = 0; i < vectors.length; i++) {
            vectors[i] = vectors[i].rotate(b.sub(a).angle());
            vectors[i] = vectors[i].add(center);
        }

        points = vectors;
        this.thickness = thickness;
        start = a;
        end = b;
        angle = b.sub(a).angle();
    }

    @Override
    protected boolean isShapeColliding(Ball ball) {
        return false;
    }

    @Override
    public Vector3d getPosition() {
        throw new AssertionError("Wall has no unique position.");
    }

    @Override
    public double getOrientation() {
        throw new AssertionError("Wall has no unique orientation.");
    }

    @Override
    public ModelInstance getModel() {
        return null;
    }

    @Override
    public void setWorld(PuttingCourse world, PhysicsEngine engine) {
        throw new AssertionError("Obstacle holds no reference to World.");
    }

    @Override
    public AxisAllignedBoundingBox getBoundingBox() {
        return null;
    }

    @Override
    public void visit(MiniMapDrawer mapDrawer) {
        mapDrawer.draw(this);
    }
}

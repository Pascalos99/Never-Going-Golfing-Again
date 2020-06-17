package com.mygdx.game.obstacles;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.mygdx.game.Ball;
import com.mygdx.game.courses.MiniMapDrawer;
import com.mygdx.game.courses.PuttingCourse;
import com.mygdx.game.physics.PhysicsEngine;
import com.mygdx.game.utils.Vector2d;
import com.mygdx.game.utils.Vector3d;
import static com.mygdx.game.utils.Variables.*;

import java.awt.*;

public class Wall extends Obstacle {
    final Vector2d[] points;
    final Vector2d start, end;
    final double thickness, angle;

    public Wall(Vector2d a, Vector2d b, double thickness){
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
    protected CollisionData isShapeColliding(Ball ball) {
        return null;
    }

    @Override
    public Vector3d getGraphicsPosition() {
        Vector2d vec = start.add(end).div(new Vector2d(2, 2));
        return new Vector3d(toWorldScale(vec.get_x()), WALL_BASE, toWorldScale(vec.get_y()));
    }

    public Vector2d getStart() {
        return start;
    }
    public Vector2d getEnd() {
        return end;
    }
    public double getThickness() {
        return thickness;
    }

    @Override
    public Vector3d getPhysicsPosition(){

        Vector2d real_position = end.sub(start).add(anchor);
        double y = 0;
        if (WORLD != null) y = WORLD.height_function.evaluate(real_position);

        return new Vector3d(real_position.get_x(), y, real_position.get_y());
    }

    public String toString() {
        return "Wall from "+start+" to "+end+", with a thickness of "+thickness;
    }

    @Override
    public double getOrientation() {
        return end.sub(start).angle();
    }

    @Override
    public ModelInstance getModel() {
        //TODO Samuele's
        return null;
    }

    @Override
    public void setWorld(PuttingCourse world, PhysicsEngine engine) {
        throw new AssertionError("Obstacle holds no reference to World.");
    }

    @Override
    public AxisAllignedBoundingBox getBoundingBox() {
        //TODO Dennis'
        return null;
    }

    @Override
    public void visit(MiniMapDrawer mapDrawer) {
        mapDrawer.draw(start, end, thickness);
    }
}

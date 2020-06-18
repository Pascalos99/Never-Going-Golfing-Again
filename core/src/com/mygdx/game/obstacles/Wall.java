package com.mygdx.game.obstacles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.math.Vector3;
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
    final double thickness, angle, length, height, rootHeight;


    public Wall(Vector2d a, Vector2d b, double thickness){
        Vector2d center = a.add(b).div(new Vector2d(2, 2));
        length = a.distance(b);
        height = 3.0;
        rootHeight = 1.0;


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
        Vector2d vec = start.add(end.sub(start).scale(.5)).add(WORLD_SHIFT);
        double y=WORLD.height_function.evaluate(vec);
        return new Vector3d(toWorldScale(vec.get_x()), toWorldScale(y)-rootHeight, toWorldScale(vec.get_y()));
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
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("tree", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.BROWN)));
        new BoxShapeBuilder().build(builder,toWorldScale(this.thickness), (float)(height+rootHeight), toWorldScale(this.length));
//        builder = modelBuilder.part("grid", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new com.badlogic.gdx.graphics.g3d.Material(ColorAttribute.createDiffuse(Color.GREEN)));
//        builder.sphere((float)this.radius*2f, (float)this.height, (float)this.radius*2f, 20,20);
        Model wall = modelBuilder.end();
        System.out.println((float)this.getGraphicsPosition().get_y());
        System.out.println(height+rootHeight);
        double y=this.getGraphicsPosition().get_y();
        ModelInstance wallInstance = new ModelInstance(wall, (float)this.getGraphicsPosition().get_x(), (float)(y+((height+rootHeight)/2f)), (float)this.getGraphicsPosition().get_z());
        Vector2d dir=end.sub(start);
        wallInstance.transform.rotateRad(Vector3.Y, (float)((Math.PI/2)-dir.angle()));
        return wallInstance;
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

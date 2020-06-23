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
import com.mygdx.game.utils.ColorProof;
import com.mygdx.game.utils.Vector2d;
import com.mygdx.game.utils.Vector3d;

import static com.mygdx.game.utils.Variables.*;

public class Wall extends Obstacle {
    final Vector2d start, end;
    final double thickness, angle, length;
    ModelInstance[] model;

    public Wall(Vector2d a, Vector2d b, double thickness) {
        length = a.distance(b);
        this.thickness = thickness;
        start = a;
        end = b;
        angle = b.sub(a).angle();

        model = null;
    }

    @Override
    protected CollisionData isShapeColliding(Ball ball) {
        return null;
    }

    @Override
    public boolean isPositionInsideShape(double x, double y) {
        Vector3d physic_pos = getPhysicsPosition();
        Vector2d midpoint = new Vector2d(physic_pos.get_x(), physic_pos.get_z());

        Vector2d real_p = new Vector2d(x, y);
        Vector2d p = (real_p.sub(midpoint)).rotate(-angle);

        AxisAllignedBoundingBox box = new AxisAllignedBoundingBox(new Vector2d(-length/2d, -thickness/2d), length, thickness);
        AxisAllignedBoundingBox dummy = new AxisAllignedBoundingBox(p, 0, 0);

        return box.collides(dummy);
    }

    @Override
    public Vector3d getGraphicsPosition() {
        Vector2d vec = start.add(end.sub(start).scale(.5)).add(WORLD_SHIFT);
        return new Vector3d(toWorldScale(vec.get_x()), (WALL_HEIGHT + WALL_BASE)/2d, toWorldScale(vec.get_y()));
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
    public Vector3d getPhysicsPosition() {
        Vector2d real_position = (end.add(start).scale(1d/2d)).add(anchor);
        return new Vector3d(real_position.get_x(), 0, real_position.get_y());
    }

    public String toString() {
        return "Wall from " + start + " to " + end + ", with a thickness of " + thickness;
    }

    @Override
    public double getOrientation() {
        return end.sub(start).angle();
    }

    public ModelInstance[] generateModel() {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("tree", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material(ColorAttribute.createDiffuse(ColorProof.WALL())));
        new BoxShapeBuilder().build(builder, toWorldScale(this.thickness), (float) (WALL_HEIGHT + Math.abs(WALL_BASE)), toWorldScale(this.length));
        Model wall = modelBuilder.end();
        double y = this.getGraphicsPosition().get_y();
        ModelInstance[] wallInstance = new ModelInstance[]{new ModelInstance(wall, (float) this.getGraphicsPosition().get_x(), (float) (y), (float) this.getGraphicsPosition().get_z())};
        Vector2d dir = end.sub(start);
        wallInstance[0].transform.rotateRad(Vector3.Y, (float) ((Math.PI / 2) - dir.angle()));
        return wallInstance;
    }

    @Override
    public ModelInstance[] getModel(){

        if(model == null)
            model = generateModel();

        return model;
    }

    @Override
    public void setWorld(PhysicsEngine engine) {
        throw new AssertionError("Obstacle holds no reference to the engine.");
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

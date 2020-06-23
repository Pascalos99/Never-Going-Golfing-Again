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
    final double RESTITUTION = 0.4;

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

        if(isPositionInsideShape(ball.position.get_x(), ball.position.get_z())) {
            Vector3d physic_pos = getPhysicsPosition();
            Vector2d midpoint = new Vector2d(physic_pos.get_x(), physic_pos.get_z());

            Vector2d real_point = new Vector2d(ball.position.get_x(), ball.position.get_z());
            Vector2d relative_point = real_point.sub(midpoint);
            Vector2d aligned_point = relative_point.rotate(-angle);
            Vector2d aligned_velocity = new Vector2d(ball.velocity.get_x(), ball.velocity.get_z()).rotate(-angle);

            AxisAllignedBoundingBox box = new AxisAllignedBoundingBox(new Vector2d(-length / 2d, -thickness / 2d), length, thickness);
            AxisAllignedBoundingBox p = new AxisAllignedBoundingBox(aligned_point.sub(new Vector2d(BALL_RADIUS, BALL_RADIUS)), BALL_RADIUS * 2d, BALL_RADIUS * 2d);

            double hld = Math.abs(p.origin.get_x() + length / 2d);//Horizontal left distance
            double hrd = Math.abs(p.origin.get_x() - length / 2d);//Horizontal right distance
            double x_clipping_correction = hld < hrd ? -(hld + BALL_RADIUS) : (hrd + BALL_RADIUS);

            double vud = Math.abs(p.origin.get_y() + thickness / 2d);//Vertical upper distance
            double vld = Math.abs(p.origin.get_y() - thickness / 2d);//Vertical lower distance
            double z_clipping_correction = vud < vld ? -(vud + BALL_RADIUS) : (vld + BALL_RADIUS);

            CollisionData data = new CollisionData(this);

            Vector2d clipping_correction = Math.abs(x_clipping_correction) > Math.abs(z_clipping_correction) ? new Vector2d(0, z_clipping_correction) : new Vector2d(x_clipping_correction, 0);

            clipping_correction = clipping_correction.rotate(angle);
            data.clipping_correction = new Vector3d(clipping_correction.get_x(), 0, clipping_correction.get_y());

            Vector2d entrance_normal = (new Vector2d(ball.velocity.get_x(), ball.velocity.get_z())).normalize();
            Vector2d clipping_normal = clipping_correction.normalize();

            double dotp = entrance_normal.dot(clipping_normal);
            Vector2d scaled_normal = clipping_normal.scale(2*dotp);

            Vector2d horizontal_bounce = entrance_normal.sub(scaled_normal);
            horizontal_bounce = horizontal_bounce.normalize().scale(ball.velocity.get_length()*RESTITUTION);
            data.bounce = new Vector3d(horizontal_bounce.get_x(), 0, horizontal_bounce.get_y());

            return data;
        }

        return null;
    }

    @Override
    public boolean isPositionInsideShape(double x, double y) {
        Vector3d physic_pos = getPhysicsPosition();
        Vector2d midpoint = new Vector2d(physic_pos.get_x(), physic_pos.get_z());

        Vector2d real_point = new Vector2d(x, y);
        Vector2d relative_point = real_point.sub(midpoint);
        Vector2d aligned_point = relative_point.rotate(-angle);

        AxisAllignedBoundingBox box = new AxisAllignedBoundingBox(new Vector2d(-length/2d, -thickness/2d), length, thickness);
        AxisAllignedBoundingBox p = new AxisAllignedBoundingBox(aligned_point.sub(new Vector2d(BALL_RADIUS, BALL_RADIUS)), BALL_RADIUS*2d, BALL_RADIUS*2d);

        return box.collides(p);
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
        Vector2d real_position = start.add(end.sub(start).scale(.5)).add(WORLD_SHIFT);
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
        double side = length;

        if(thickness > length)
            side = thickness;

        Vector3d physics_pos = getPhysicsPosition();
        Vector2d topdown = new Vector2d(physics_pos.get_x(), physics_pos.get_z());

        return new AxisAllignedBoundingBox(
                topdown.add(new Vector2d(-side/2d, -side/2d)),
                side, side
        );
    }

    @Override
    public void visit(MiniMapDrawer mapDrawer) {
        mapDrawer.draw(start, end, thickness);
    }

}

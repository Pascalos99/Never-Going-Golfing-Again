package com.mygdx.game.obstacles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.mygdx.game.Ball;
import com.mygdx.game.courses.MiniMapDrawer;
import com.mygdx.game.courses.PuttingCourse;
import com.mygdx.game.physics.PhysicsEngine;
import com.mygdx.game.utils.Variables;
import com.mygdx.game.utils.Vector3d;

public class DummyObstacle extends Obstacle {

    private static double size = 1;

    private static double TO_GRAPHICS = Variables.WORLD_SCALING/Variables.GRAPHICS_SCALING;

    @Override
    protected CollisionData isShapeColliding(Ball ball) {
        return null;
    }

    @Override
    public Vector3d getPosition() {
        return pos;
    }

    @Override
    public double getOrientation() {
        return 0;
    }

    private Vector3d pos;

    public DummyObstacle(double x, double y) {
        try {
            if (model == null) model = createModel();
            pos = new Vector3d(x, 1, y);
            model_instance = new ModelInstance(model, (float)(pos.get_x()*Variables.WORLD_SCALING), (float)(pos.get_y()*Variables.WORLD_SCALING), (float)(pos.get_z()*Variables.WORLD_SCALING));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private static Model model = null;
    private ModelInstance model_instance;

    private static Model createModel() {
        ModelBuilder mb = new ModelBuilder();
        mb.begin();
        MeshPartBuilder builder = mb.part("grid", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new com.badlogic.gdx.graphics.g3d.Material(ColorAttribute.createDiffuse(
                        new Color(1f, 0.3f, 0.3f, 1f))));
        BoxShapeBuilder.build(builder,
                (float)(size*TO_GRAPHICS),
                (float)(size*TO_GRAPHICS),
                (float)(size*TO_GRAPHICS));
        Model m = mb.end();
        return m;
    }

    @Override
    public ModelInstance getModel() {
        return model_instance;
    }

    @Override
    public void setWorld(PuttingCourse world, PhysicsEngine engine) {

    }

    @Override
    public AxisAllignedBoundingBox getBoundingBox() {
        return new AxisAllignedBoundingBox(pos, size, size, size);
    }

    @Override
    public void visit(MiniMapDrawer mapDrawer) {
        mapDrawer.draw(this, size);
    }
}

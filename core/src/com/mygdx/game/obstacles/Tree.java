package com.mygdx.game.obstacles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CylinderShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.mygdx.game.Ball;
import com.mygdx.game.courses.MiniMapDrawer;
import com.mygdx.game.utils.ColorProof;
import com.mygdx.game.utils.Vector2d;
import com.mygdx.game.utils.Vector3d;
import static com.mygdx.game.utils.Variables.*;

public class Tree extends Obstacle {
    public  static  final double RESTITUTION = 0.4;
    public static final int TEXTURE_NOT_SPECIFIED = 0;
    public static final int TEXTURE_SMALL = 1;
    public static final double HEIGHT_SMALL = 15;
    public static final int TEXTURE_MEDIUM = 2;
    public static final double HEIGHT_MEDIUM = 23;
    public static final int TEXTURE_LARGE = 3;
    public static final double HEIGHT_LARGE = 31;
    /** tree height / tree radius = H_R_RATIO (for all default trees) - "the tree is 18* taller than it is wide" */
    public static final double H_R_RATIO = 125d;
    static final double rootHeight=1;

    public int texture_used = TEXTURE_NOT_SPECIFIED;

    private Vector2d position;
    private double height, radius;
    private AxisAllignedBoundingBox aabb = null;

    public Tree(Vector2d position, double height, double radius){
        this.position = position;
        this.height = height;
        this.radius = radius;

        System.out.println(height);
    }

    @Override
    protected CollisionData isShapeColliding(Ball ball) {
        Vector3d physics_pos = getPhysicsPosition();
        Vector2d topdown_pos = new Vector2d(physics_pos.get_x(), physics_pos.get_z());
        Vector2d ball_position = new Vector2d(ball.position.get_x(), ball.position.get_z());

        if (isPositionInsideShape(ball_position.get_x(), ball_position.get_y())) {
            CollisionData data = new CollisionData(this);

            Vector2d clipping_normal = ball_position.sub(topdown_pos).normalize();
            Vector2d unclipped_position = clipping_normal.scale(radius + BALL_RADIUS).add(topdown_pos);
            Vector2d clipping_correction = unclipped_position.sub(ball_position);
            data.clipping_correction = new Vector3d(clipping_correction.get_x(), 0, clipping_correction.get_y());

            Vector2d topdown_vel = new Vector2d(ball.velocity.get_x(), ball.velocity.get_z());
            Vector2d entrance_normal = topdown_vel.normalize();

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
        Vector3d physics_pos = getPhysicsPosition();
        Vector2d topdown_pos = new Vector2d(physics_pos.get_x(), physics_pos.get_z());

        return (new Vector2d(x, y)).distance(topdown_pos) < radius + BALL_RADIUS;
    }

    @Override
    public Vector3d getGraphicsPosition() {
        Vector3d vec = getPhysicsPosition();
        return new Vector3d(toWorldScale(vec.get_x()), toWorldScale(vec.get_y()), toWorldScale(vec.get_z()));
    }

    @Override
    public Vector3d getPhysicsPosition(){
        Vector2d real_position = position.add(anchor);
        double y = -10d;
        if (WORLD != null) y = WORLD.height_function.evaluate(real_position) - fromWorldScale((float)rootHeight);
        return new Vector3d(real_position.get_x(), y, real_position.get_y());
    }

    @Override
    public double getOrientation() {
        return 0;
    }

    private ModelInstance[] generateModel() {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("tree", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new com.badlogic.gdx.graphics.g3d.Material(ColorAttribute.createDiffuse(ColorProof.TRUNK())));
        new CylinderShapeBuilder().build(builder,(float)toWorldScale(this.radius*2f), (float)(this.height+rootHeight), (float)toWorldScale(this.radius*2f), 20);
        Model trunk = modelBuilder.end();

        modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        builder = modelBuilder.part("leaves", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new com.badlogic.gdx.graphics.g3d.Material(ColorAttribute.createDiffuse(ColorProof.LEAVES())));
        new SphereShapeBuilder().build(builder,(float)(this.height/2), (float)(this.height/2),(float)(this.height/2), 40,40);
        Model leaves = modelBuilder.end();

        ModelInstance trunkInstance = new ModelInstance(trunk, (float)this.getGraphicsPosition().get_x(), (float)this.getGraphicsPosition().get_y(), (float)this.getGraphicsPosition().get_z());
        ModelInstance leavesInstance = new ModelInstance(leaves, (float)this.getGraphicsPosition().get_x(), (float)(this.getGraphicsPosition().get_y()+this.height-(this.height/3f)), (float)this.getGraphicsPosition().get_z());

        ModelInstance[] treeInstance= new ModelInstance[]{trunkInstance,leavesInstance};

        return treeInstance;
    }

    private ModelInstance trunk;
    private ModelInstance leaves;

    @Override
    public ModelInstance[] getModel() {
        if (trunk == null || leaves == null) {
            ModelInstance[] models = generateModel();
            trunk = models[0];
            leaves = models[1];
        }
        return new ModelInstance[]{trunk, leaves};
    }

    @Override
    public AxisAllignedBoundingBox getBoundingBox() {
        if(aabb == null) {
            Vector3d physics_pos = getPhysicsPosition();
            aabb = new AxisAllignedBoundingBox(
                    new Vector2d(physics_pos.get_x() - radius, physics_pos.get_z() - radius),
                    radius * 2, radius * 2
            );
        }
        return aabb;
    }

    public double getHeight() {
        return height;
    }

    public double getRadius() {
        return radius;
    }

    public String toString() {
        return "Tree at "+position.add(anchor)+" with height of "+height+" and radius of "+radius;
    }

    @Override
    public void visit(MiniMapDrawer mapDrawer) {
        if (texture_used == TEXTURE_SMALL) mapDrawer.drawSmall(this);
        else if (texture_used == TEXTURE_MEDIUM) mapDrawer.drawMedium(this);
        else if (texture_used == TEXTURE_LARGE) mapDrawer.drawLarge(this);
        else mapDrawer.draw(this);
    }
}

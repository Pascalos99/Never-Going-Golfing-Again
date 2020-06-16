package com.mygdx.game.obstacles;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.mygdx.game.Ball;
import com.mygdx.game.courses.MiniMapDrawer;
import com.mygdx.game.utils.Vector2d;
import com.mygdx.game.utils.Vector3d;
import static com.mygdx.game.utils.Variables.*;

public class Tree extends Obstacle {
    private Vector2d position = null;
    private double height = 0d, radius = 0d;
    private AxisAllignedBoundingBox aabb = null;

    Tree(Vector2d position, double height, double radius){
        this.position = position;
        this.height = height;
        this.radius = radius;
    }

    @Override
    protected CollisionData isShapeColliding(Ball ball) {
        CollisionData data = new CollisionData();

        if(ball.height - BALL_RADIUS <= height) {

            Vector2d ball_position = new Vector2d(ball.x, ball.y);

            if(ball_position.distance(position) < radius + BALL_RADIUS){

                if(ball.height < height){
                    Vector2d clipping_normal = ball_position.sub(position).normalize();
                    Vector2d unclipped_position = clipping_normal.scale(radius + BALL_RADIUS).add(position);
                    Vector2d clipping_correction = unclipped_position.sub(ball_position);

                    data.clipping_correction = new Vector3d(clipping_correction.get_x(), 0, clipping_correction.get_y());
                }

                else{
                    data.clipping_correction = new Vector3d(0, height + BALL_RADIUS, 0);
                }

            }

        }

        return data;
    }

    @Override
    public Vector3d getPosition() {
        Vector3d vec = new Vector3d(position.get_x(), getWorld().height_function.evaluate(position), position.get_y());
        return  new Vector3d(toWorldScale(vec.get_x()), toWorldScale(vec.get_y()), toWorldScale(vec.get_z()));
    }

    @Override
    public double getOrientation() {
        return 0;
    }

    @Override
    public ModelInstance getModel() {
        //TODO Samuele's
        return null;
    }

    @Override
    public AxisAllignedBoundingBox getBoundingBox() {

        if(aabb == null)
            aabb = new AxisAllignedBoundingBox(
                    new Vector3d(position.get_x() - radius, height, position.get_y() - radius),
                    radius*2, radius*2, height + 10
            );

        return aabb;
    }

    @Override
    public void visit(MiniMapDrawer mapDrawer) {
        mapDrawer.draw(this);
    }
}

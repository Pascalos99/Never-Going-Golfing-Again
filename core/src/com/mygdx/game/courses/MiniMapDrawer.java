package com.mygdx.game.courses;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.mygdx.game.Ball;
import com.mygdx.game.obstacles.Wall;
import com.mygdx.game.parser.Function2d;
import com.mygdx.game.parser.SandFunction2d;
import com.mygdx.game.physics.TopDownPhysicsObject;
import com.mygdx.game.utils.Variables;
import com.mygdx.game.utils.Vector2d;

import java.awt.*;

public abstract class MiniMapDrawer {

    protected final double scale_X, scale_Y;
    protected final int width, height;
    private Pixmap pm;
    private Vector2d anchor;

    public static Texture defaultMiniMap(int width, int height, double zoom, PuttingCourse course, Vector2d bottom_left_corner) {
        MiniMapDrawer mmd = new DefaultMiniMap(width, height, width / zoom, height / zoom);
        mmd.setAnchor(bottom_left_corner);
        mmd.draw(course);
        return mmd.getTexture();
    }

    public MiniMapDrawer(int image_width, int image_height, double course_width, double course_height) {
        width = image_width;
        height = image_height;
        scale_X = image_width / course_width;
        scale_Y = image_height / course_height;
        pm = new Pixmap(width, height, Pixmap.Format.RGBA8888);
    }

    public void setAnchor(Vector2d bottom_left_corner) {
        anchor = bottom_left_corner;
    }
    public Vector2d getAnchor() {
        return anchor;
    }

    public void draw(PuttingCourse course) {
        drawHeight(course.height_function, pm);
        if (course.friction_function instanceof SandFunction2d) drawSand((SandFunction2d) course.friction_function, pm);
        for (Drawable draw : course.obstacles) draw.visit(this, pm);
    }

    public Texture getTexture() {
        return new Texture(pm);
    }

    // TODO make a drawer that implements these methods and then returns an image (like in the builder pattern)

    public abstract void drawHeight(Function2d height, Pixmap pm);

    public abstract void drawSand(SandFunction2d sand, Pixmap pm);

    public abstract void draw(TopDownPhysicsObject unspecified_object, Pixmap pm);

    public abstract void draw(Wall wall, Pixmap pm);

    public abstract void draw(Ball ball, Pixmap pm);

    static class DefaultMiniMap extends MiniMapDrawer {

        public DefaultMiniMap(int image_width, int image_height, double course_width, double course_height) {
            super(image_width, image_height, course_width, course_height);
        }

        @Override
        public void drawHeight(Function2d h, Pixmap pm) {
            for (int i=0; i < width; i++) {
                double x = i / scale_X + getAnchor().get_x();
                for (int j=0; j < height; j++) {
                    double y = j / scale_Y + getAnchor().get_y();
                    Color color;
                    if (h.evaluate(x, y) <= 0) color = new Color(0, 0, 200);
                    else if (h.evaluate(x, y) <= 20) color = new Color(20, (int)Math.round(50 + 150 * (h.evaluate(x, y)/20d)), 20);
                    else color = new Color(25,40,25);
                    pm.drawPixel(i, j, color.getRGB());
                }
            }
        }

        @Override
        public void drawSand(SandFunction2d s, Pixmap pm) {
            for (int i=0; i < width; i++) {
                double x = i / scale_X + getAnchor().get_x();
                for (int j=0; j < height; j++) {
                    double y = j / scale_Y + getAnchor().get_x();
                    if (s.isSandAt(x, y))
                        pm.drawPixel(i, j, new Color(180,180,0).getRGB());
                }
            }
        }

        @Override
        public void draw(TopDownPhysicsObject unspecified_object, Pixmap pm) {
            System.err.println("implementation for MiniMapDrawer incomplete");
        }

        @Override
        public void draw(Wall wall, Pixmap pm) {

        }

        @Override
        public void draw(Ball ball, Pixmap pm) {
            int ball_x = (int)Math.round((ball.topDownPosition().get_x() - getAnchor().get_x())*scale_X);
            int ball_y = (int)Math.round((ball.topDownPosition().get_y() - getAnchor().get_y())*scale_Y);
            int size_x = (int)Math.round(Variables.BALL_RADIUS * Variables.WORLD_SCALING * scale_X);
            int size_y = (int)Math.round(Variables.BALL_RADIUS * Variables.WORLD_SCALING * scale_Y);
            String ball_color = ball.owner.getBallColor();
            for (int i=0; i < Variables.BALL_COLORS.length; i++)
                if (Variables.BALL_COLORS[i].name.equals(ball_color)) {
                    com.badlogic.gdx.graphics.Color c = Variables.BALL_COLORS[i].color;
                    pm.setColor(c);
                    pm.fillCircle(ball_x, ball_y, (size_x > size_y)?size_x:size_y);
                }
        }
    }

}

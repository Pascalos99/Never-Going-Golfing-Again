package com.mygdx.game.courses;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.mygdx.game.Ball;
import com.mygdx.game.obstacles.DummyObstacle;
import com.mygdx.game.obstacles.Wall;
import com.mygdx.game.parser.Function2d;
import com.mygdx.game.parser.SandFunction2d;
import com.mygdx.game.physics.TopDownPhysicsObject;
import com.mygdx.game.utils.Variables;
import com.mygdx.game.utils.Vector2d;
import com.mygdx.game.utils.Vector3d;

public abstract class MiniMapDrawer {

    protected final double scale_X, scale_Y;
    protected final int width, height;
    protected Pixmap pm;
    private Vector2d anchor;

    public static MiniMapDrawer defaultDrawer(double course_width, double course_height, int pixels_per_unit, Vector2d bottom_left_corner) {
        MiniMapDrawer mmd = new DefaultMiniMap(
                (int)(course_width*pixels_per_unit), (int)(course_height*pixels_per_unit), course_width, course_height);
        mmd.setAnchor(bottom_left_corner);
        return mmd;
    }

    public static Texture defaultMiniMap(double course_width, double course_height, int pixels_per_unit, PuttingCourse course, Vector2d bottom_left_corner) {
        MiniMapDrawer mmd = defaultDrawer(course_width, course_height, pixels_per_unit, bottom_left_corner);
        mmd.draw(course);
        return mmd.getTexture();
    }

    public static Texture defaultMiniMap(double course_width, double course_height, int pixels_per_unit, CourseBuilder course, Vector2d bottom_left_corner) {
        MiniMapDrawer mmd = defaultDrawer(course_width, course_height, pixels_per_unit, bottom_left_corner);
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

    public Pixmap getPixmap() {
        return pm;
    }

    public void draw(PuttingCourse course) {
        drawHeight(course.height_function, 5);
        if (course.friction_function instanceof SandFunction2d) drawSand((SandFunction2d) course.friction_function);
        for (Drawable draw : course.obstacles) draw.visit(this);
        drawStartingPos(course.start_position);
        drawGoalPos(course.flag_position);
    }
    public void draw(CourseBuilder course) {
        if (course.height_function != null) drawHeight(course.height_function, 5 * Variables.WORLD_SCALING);
        if (course.friction_function instanceof SandFunction2d) drawSand((SandFunction2d) course.friction_function);
        for (Drawable draw : course.obstacles) draw.visit(this);
        if (course.start != null) drawStartingPos(course.start);
        if (course.goal != null) drawGoalPos(course.goal);
    }

    public Texture getTexture() {
        return new Texture(pm);
    }

    // TODO make a drawer that implements these methods and then returns an image (like in the builder pattern)

    public abstract void drawHeight(Function2d height, double heightBarrier);

    public abstract void drawSand(SandFunction2d sand);

    public abstract void draw(TopDownPhysicsObject unspecified_object);

    public abstract void draw(Wall wall);

    public abstract void draw(Ball ball);

    public abstract void draw(DummyObstacle dummy, double size);

    public abstract void drawStartingPos(Vector2d start);

    public abstract void drawGoalPos(Vector2d flag);

    public void drawAtWorldPos(Texture texture, double x, double y) {
        TextureData td = texture.getTextureData();
        td.prepare();
        Pixmap tm = td.consumePixmap();
        int x_pos = (int)Math.round((x - getAnchor().get_x()) * scale_X);
        int y_pos = (int)Math.round((y - getAnchor().get_y()) * scale_Y);
        pm.drawPixmap(tm, x_pos, y_pos);
    }

    static class DefaultMiniMap extends MiniMapDrawer {

        public DefaultMiniMap(int image_width, int image_height, double course_width, double course_height) {
            super(image_width, image_height, course_width, course_height);
        }

        @Override
        public void drawHeight(Function2d h, double heightBarrier) {
            for (int i=0; i < width; i++) {
                double x = i / scale_X + getAnchor().get_x();
                for (int j=0; j < height; j++) {
                    double y = j / scale_Y + getAnchor().get_y();
                    Color color;
                    if (h.evaluate(x, y) <= 0)
                        color = new Color(20/255f, 20/255f, (float)((200d + h.evaluate(x, y) * 25d)/255d),1f);
                    else
                        color = new Color(20/255f, (float)((200-150* (h.evaluate(x, y)/heightBarrier))/255d), 20/255f,1f);

                    pm.setColor(color);
                    pm.drawPixel(i, j);
                }
            }
        }

        @Override
        public void drawSand(SandFunction2d s) {
            for (int i=0; i < width; i++) {
                double x = i / scale_X + getAnchor().get_x();
                for (int j=0; j < height; j++) {
                    double y = j / scale_Y + getAnchor().get_y();
                    if (s.isSandAt(x, y) && s.main.evaluate(x, y) > 0) {
                        pm.setColor(new Color(180/255f, 180/255f, 0f, 1f));
                        pm.drawPixel(i, j);
                    }
                }
            }
        }

        @Override
        public void draw(TopDownPhysicsObject unspecified_object) {
            System.err.println("implementation for MiniMapDrawer incomplete");
        }

        @Override
        public void draw(Wall wall) {

        }

        @Override
        public void draw(Ball ball) {
            int ball_x = (int)Math.round((ball.topDownPosition().get_x() - getAnchor().get_x())*scale_X);
            int ball_y = (int)Math.round((ball.topDownPosition().get_y() - getAnchor().get_y())*scale_Y);
            int size_x = (int)Math.round(Variables.BALL_RADIUS * Variables.WORLD_SCALING * scale_X);
            int size_y = (int)Math.round(Variables.BALL_RADIUS * Variables.WORLD_SCALING * scale_Y);
            String ball_color = ball.owner.getBallColor();
            for (int i=0; i < Variables.BALL_COLORS.length; i++)
                if (Variables.BALL_COLORS[i].name.equals(ball_color)) {
                    Color c = Variables.BALL_COLORS[i].color;
                    pm.setColor(c);
                    pm.fillCircle(ball_x, ball_y, (size_x > size_y)?size_x:size_y);
                }
        }

        @Override
        public void draw(DummyObstacle dummy, double size) {
            Vector3d p = dummy.getPhysicsPosition();
            Color c = new Color(1f, 0.3f, 0.3f, 1f);
            pm.setColor(c);
            pm.fillRectangle(
                    (int)Math.round((p.get_x() - getAnchor().get_x())*scale_X),
                    (int)Math.round((p.get_z() - getAnchor().get_y())*scale_Y),
                    (int)Math.round(size * scale_X),
                    (int)Math.round(size * scale_X));
        }

        @Override
        public void drawStartingPos(Vector2d start) {
            System.out.println("hey");
            Texture ball_point;
            try {
                ball_point = new Texture(Gdx.files.internal("misc/Start.png"));
                drawAtWorldPos(ball_point, start.get_x(), start.get_y());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("it gave error with message: \""+e.getMessage()+"\"");
            }
        }

        @Override
        public void drawGoalPos(Vector2d flag) {
            Texture flag_point;
            try {
                flag_point = new Texture(Gdx.files.internal("misc/Flag.png"));
                drawAtWorldPos(flag_point, flag.get_x(), flag.get_y());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("it gave error with message: \""+e.getMessage()+"\"");
            }
        }

    }

}

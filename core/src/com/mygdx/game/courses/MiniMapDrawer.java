package com.mygdx.game.courses;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.mygdx.game.Ball;
import com.mygdx.game.obstacles.Obstacle;
import com.mygdx.game.obstacles.Tree;
import com.mygdx.game.parser.Function2d;
import com.mygdx.game.parser.SandFunction2d;
import com.mygdx.game.physics.TopDownPhysicsObject;
import com.mygdx.game.utils.Variables;
import com.mygdx.game.utils.Vector2d;

import static com.mygdx.game.courses.CourseBuilderListener.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

public abstract class MiniMapDrawer {

    protected final double scale_X, scale_Y;
    protected final int width, height;

    protected Pixmap main_image;
    protected Pixmap height_image;
    protected Pixmap sand_image;
    protected Pixmap obstacles_image;
    protected Pixmap start_and_goal_image;
    protected Pixmap tempwall_image;

    private Vector2d anchor;

    private static double HEIGHT_BARRIER = 5;

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
        main_image = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        height_image = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        sand_image = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        obstacles_image = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        start_and_goal_image = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        tempwall_image = new Pixmap(width, height, Pixmap.Format.RGBA8888);
    }

    public void setAnchor(Vector2d bottom_left_corner) {
        anchor = bottom_left_corner;
    }
    public Vector2d getAnchor() {
        return anchor;
    }

    public Pixmap getPixmap() {
        return main_image;
    }

    private Comparator<Obstacle> obstacleSort = (o1, o2) -> {
        if (o1.getPhysicsPosition().get_z() < o2.getPhysicsPosition().get_z()) return -1;
        if (o1.getPhysicsPosition().get_z() == o2.getPhysicsPosition().get_z()) return 0;
        return 1;
    };

    public void draw(PuttingCourse course) {
        drawHeight(course.height_function, HEIGHT_BARRIER);
        if (course.friction_function instanceof SandFunction2d) drawSand((SandFunction2d) course.friction_function);
        course.obstacles.sort(obstacleSort);
        for (Drawable draw : course.obstacles) draw.visit(this);
        drawStartAndGoalPos(course.start_position, course.flag_position);
    }
    public void draw(CourseBuilder course) {
        if (cbl != null && cbl.cb == course) {
            redraw();
            return;
        }
        if (course.height_function != null) drawHeight(course.height_function, HEIGHT_BARRIER * Variables.WORLD_SCALING);
        if (course.friction_function instanceof SandFunction2d) drawSand((SandFunction2d) course.friction_function);
        course.obstacles.sort(obstacleSort);
        for (Drawable draw : course.obstacles) draw.visit(this);
        drawStartAndGoalPos(course.start, course.goal);
        if (course.temp_wall != null) course.temp_wall.visit(this);
    }

    private CourseBuilderListener cbl;

    public void setListener(CourseBuilderListener cbl) {
        this.cbl = cbl;
        cbl.setAll(true);
    }
    public boolean notify(int update_code) {
        if (cbl != null) return cbl.reset(update_code);
        return false;
    }
    public void update() {
        if (cbl == null) return;

        if (cbl.reset(UPDATE_HEIGHT) && cbl.cb.height_function != null)
            drawHeight(cbl.cb.height_function, HEIGHT_BARRIER * Variables.WORLD_SCALING);

        if (cbl.reset(UPDATE_FRICTION) && cbl.cb.friction_function instanceof SandFunction2d)
            drawSand((SandFunction2d) cbl.cb.friction_function);

        if (cbl.reset(UPDATE_START) && cbl.cb.start != null) {
            drawStartAndGoalPos(cbl.cb.start, cbl.cb.goal);
            cbl.reset(UPDATE_GOAL);
        } if (cbl.reset(UPDATE_GOAL) && cbl.cb.goal != null) {
            drawStartAndGoalPos(cbl.cb.start, cbl.cb.goal);
            cbl.reset(UPDATE_START);
        } if (cbl.cb.temp_wall != null && cbl.reset(UPDATE_TEMP_WALL)) cbl.cb.temp_wall.visit(this);
        if (cbl.reset(UPDATE_TEMP_WALL)) {
            tempwall_image.setColor(Color.CLEAR);
            tempwall_image.fill();
        }
        if (cbl.reset(UPDATE_OBSTACLES)) {
            obstacles_image.setColor(Color.CLEAR);
            obstacles_image.fill();
            cbl.obstacles = new ArrayList<>(cbl.cb.obstacles);
            cbl.set(ADD_OBSTACLE, true);
        }
        if (cbl.reset(ADD_OBSTACLE))
            for (Obstacle o : cbl.consumeObstacles()) o.visit(this);
    }
    public void assembleImage() {
        main_image.drawPixmap(height_image, 0, 0);
        main_image.drawPixmap(sand_image, 0, 0);
        main_image.drawPixmap(obstacles_image, 0, 0);
        main_image.drawPixmap(tempwall_image, 0, 0);
        main_image.drawPixmap(start_and_goal_image, 0, 0);
    }
    public boolean redraw() {
        if (cbl == null) return false;
        update();
        assembleImage();
        return true;
    }

    public Vector2d getRealPos(Vector2d pos_in_image) {
        return new Vector2d(toPhysicsX(pos_in_image.get_x()), toPhysicsY(pos_in_image.get_y()));
    }

    protected int toMiniMapX(double physics_x) {
        return (int)Math.round((physics_x - getAnchor().get_x())*scale_X);
    }
    protected int toMiniMapY(double physics_y) {
        return (int)Math.round((physics_y - getAnchor().get_y())*scale_Y);
    }
    protected int toMiniMapW(double physics_w) {
        return (int)Math.round(physics_w * scale_X);
    }
    protected int toMiniMapH(double physics_h) {
        return (int)Math.round(physics_h * scale_X);
    }

    protected double toPhysicsX(double minimap_x) {
        return minimap_x / scale_X + getAnchor().get_x();
    }
    protected double toPhysicsY(double minimap_y) {
        return minimap_y / scale_Y + getAnchor().get_y();
    }

    public Texture getTexture() {
        return new Texture(main_image);
    }

    // TODO make a drawer that implements these methods and then returns an image (like in the builder pattern)

    public abstract void drawHeight(Function2d height, double heightBarrier);

    public abstract void drawSand(SandFunction2d sand);

    public abstract void draw(TopDownPhysicsObject unspecified_object);

    public abstract void draw(Vector2d from, Vector2d to, double thickness);
    public abstract void drawTemp(Vector2d from, Vector2d to, double thickness);

    public abstract void draw(Ball ball);

    public abstract void draw(Tree tree);
    public abstract void drawSmall(Tree tree);
    public abstract void drawMedium(Tree tree);
    public abstract void drawLarge(Tree tree);

    public abstract void drawStartAndGoalPos(Vector2d start, Vector2d flag);

    public void drawAtWorldPos(Texture texture, double x, double y, Pixmap pm) {
        TextureData td = texture.getTextureData();
        td.prepare();
        Pixmap tm = td.consumePixmap();
        pm.drawPixmap(tm, toMiniMapX(x) - td.getWidth() / 2, toMiniMapY(y)-td.getHeight());
    }
    public void drawImageAtWorldPos(String internal_path, double x, double y, Pixmap pm) {
        Texture texture;
        try {
            texture = new Texture(Gdx.files.internal(internal_path));
            drawAtWorldPos(texture, x, y, pm);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("could not load image "+internal_path+": \""+e.getMessage()+"\"");
        }
    }

    static class DefaultMiniMap extends MiniMapDrawer {

        public DefaultMiniMap(int image_width, int image_height, double course_width, double course_height) {
            super(image_width, image_height, course_width, course_height);
        }

        @Override
        public void drawHeight(Function2d h, double heightBarrier) {
            for (int i=0; i < width; i++) {
                double x = toPhysicsX(i);
                for (int j=0; j < height; j++) {
                    double y = toPhysicsY(j);
                    Color color;
                    if (h.evaluate(x, y) <= 0)
                        color = new Color(20/255f, 20/255f, (float)((200d + h.evaluate(x, y) * 25d)/255d),1f);
                    else
                        color = new Color(20/255f, (float)((200-150* (h.evaluate(x, y)/heightBarrier))/255d), 20/255f,1f);

                    height_image.setColor(color);
                    height_image.drawPixel(i, j);
                }
            }
        }

        @Override
        public void drawSand(SandFunction2d s) {
            for (int i=0; i < width; i++) {
                double x = toPhysicsX(i);
                for (int j=0; j < height; j++) {
                    double y = toPhysicsY(j);
                    if (s.isSandAt(x, y) && s.main.evaluate(x, y) > 0) {
                        sand_image.setColor(new Color(180/255f, 180/255f, 0f, 1f));
                        sand_image.drawPixel(i, j);
                    }
                }
            }
        }

        @Override
        public void draw(TopDownPhysicsObject unspecified_object) {
            System.err.println("implementation for MiniMapDrawer incomplete");
        }

        private void draw(Vector2d from, Vector2d to, double thickness, Pixmap pm) {

            Vector2d short_line = to.sub(from).normalize().rotate(Math.PI/2).scale(thickness);

            Vector2d v1 = from.add(short_line);
            Vector2d v2 = from.sub(short_line);
            Vector2d v3 = v2.add(to.sub(from));
            Vector2d v4 = v1.add(to.sub(from));

            Polygon poly = new Polygon(new int[]{
                    toMiniMapX(v1.get_x()), toMiniMapX(v2.get_x()),
                    toMiniMapX(v3.get_x()), toMiniMapX(v4.get_x())},
                    new int[]{
                            toMiniMapY(v1.get_y()), toMiniMapY(v2.get_y()),
                            toMiniMapY(v3.get_y()), toMiniMapY(v4.get_y())
                    }, 4);

            pm.setColor(new Color(0.2f, 0.2f, 0.2f, 1f));
            for (int i=0; i < width; i++)
                for (int j=0; j < height; j++)
                    if (poly.contains(i, j)) pm.drawPixel(i, j);
        }

        @Override
        public void draw(Vector2d from, Vector2d to, double thickness) {
            draw(from, to, thickness, obstacles_image);
        }

        @Override
        public void drawTemp(Vector2d from, Vector2d to, double thickness) {
            tempwall_image.setColor(Color.CLEAR);
            tempwall_image.fill();
            draw(from, to, thickness, tempwall_image);
        }

        @Override
        public void draw(Tree tree) {
            double tree_val = tree.getHeight()*tree.getRadius();
            double distance_from_small = Math.abs(tree_val - Tree.HEIGHT_SMALL*Tree.HEIGHT_SMALL/Tree.H_R_RATIO);
            double distance_from_medium = Math.abs(tree_val - Tree.HEIGHT_MEDIUM*Tree.HEIGHT_MEDIUM/Tree.H_R_RATIO);
            double distance_from_large = Math.abs(tree_val - Tree.HEIGHT_LARGE*Tree.HEIGHT_LARGE/Tree.H_R_RATIO);
            double minimum_distance = Math.min(distance_from_small, Math.min(distance_from_medium, distance_from_large));
            if (minimum_distance == distance_from_small) drawSmall(tree);
            else if (minimum_distance == distance_from_large) drawLarge(tree);
            else drawMedium(tree);
        }

        @Override
        public void drawSmall(Tree tree) {
            drawImageAtWorldPos("misc/SmallTree.png",
                    tree.getPhysicsPosition().get_x(), tree.getPhysicsPosition().get_z(), obstacles_image);
        }

        @Override
        public void drawMedium(Tree tree) {
            drawImageAtWorldPos("misc/MediumTree.png",
                    tree.getPhysicsPosition().get_x(), tree.getPhysicsPosition().get_z(), obstacles_image);
        }

        @Override
        public void drawLarge(Tree tree) {
            drawImageAtWorldPos("misc/LargeTree.png",
                    tree.getPhysicsPosition().get_x(), tree.getPhysicsPosition().get_z(), obstacles_image);
        }

        @Override
        public void draw(Ball ball) {
            int ball_x = toMiniMapX(ball.topDownPosition().get_x());
            int ball_y = toMiniMapY(ball.topDownPosition().get_y());
            int size_x = toMiniMapW(Variables.BALL_RADIUS * Variables.WORLD_SCALING);
            int size_y = toMiniMapH(Variables.BALL_RADIUS * Variables.WORLD_SCALING);
            String ball_color = ball.owner.getBallColor();
            for (int i=0; i < Variables.BALL_COLORS.length; i++)
                if (Variables.BALL_COLORS[i].name.equals(ball_color)) {
                    Color c = Variables.BALL_COLORS[i].color;
                    assembleImage();
                    main_image.setColor(c);
                    main_image.fillCircle(ball_x, ball_y, (size_x > size_y)?size_x:size_y);
                }
        }

        @Override
        public void drawStartAndGoalPos(Vector2d start, Vector2d flag) {
            start_and_goal_image.setColor(Color.CLEAR);
            start_and_goal_image.fill();
            if (start != null) drawImageAtWorldPos("misc/Start.png", start.get_x(), start.get_y(), start_and_goal_image);
            if (flag != null) drawImageAtWorldPos("misc/Flag.png", flag.get_x(), flag.get_y(), start_and_goal_image);
        }

    }

}

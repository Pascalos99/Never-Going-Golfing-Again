package com.mygdx.game.courses;

import com.mygdx.game.Ball;
import com.mygdx.game.obstacles.Wall;
import com.mygdx.game.parser.Function2d;
import com.mygdx.game.parser.SandFunction2d;
import com.mygdx.game.physics.TopDownPhysicsObject;

public abstract class MapDrawer {

    public void draw(PuttingCourse course) {
        drawHeight(course.height_function);
        if (course.friction_function instanceof SandFunction2d) drawSand((SandFunction2d) course.friction_function);

    }

    // TODO make a drawer that implements these methods and then returns an image (like in the builder pattern)

    public abstract void drawHeight(Function2d height);

    public abstract void drawSand(SandFunction2d sand);

    public abstract void draw(TopDownPhysicsObject unspecified_object);

    public abstract void draw(Wall wall);

    public abstract void draw(Ball ball);

}

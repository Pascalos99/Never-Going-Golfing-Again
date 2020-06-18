package com.mygdx.game.courses;

import com.mygdx.game.obstacles.Wall;
import com.mygdx.game.utils.Vector2d;

public class TempWall implements Drawable {
    public final Vector2d start;
    public double thickness;
    public Vector2d end;
    public TempWall(Vector2d start, double thickness) {
        this.start = start; this.thickness = thickness;
    }
    public Wall get() {
        if (end == null) end = start;
        return new Wall(start, end, thickness);
    }
    public void visit(MiniMapDrawer mapDrawer) {
        if (end == null) end = start;
        mapDrawer.draw(start, end, thickness);
    }
}
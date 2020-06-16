package com.mygdx.game.parser;

import com.mygdx.game.utils.Vector2d;

public class SandFunction2d implements Function2d {

    public final double default_friction;
    public final double sand_friction;
    public final Function2d main;
    public final Function2d sand;

    /**
     * Defines a function that equals {@code sand_friction} where {@code sand} lies above {@code main} and
     *  {@code normal_friction} elsewhere.
     * @param normal_friction
     * @param sand_friction
     * @param main
     * @param sand
     */
    public SandFunction2d(double normal_friction, double sand_friction, Function2d main, Function2d sand) {
        default_friction = normal_friction;
        this.sand_friction = sand_friction;
        this.main = main;
        this.sand = sand;
    }

    @Override
    public Vector2d gradient(double x, double y) {
        return Vector2d.ZERO;
    }

    @Override
    public double evaluate(double x, double y) {
        if (isSandAt(x, y)) return sand_friction;
        else return default_friction;
    }

    public boolean isSandAt(double x, double y) {
        return main.evaluate(x, y) < sand.evaluate(x, y);
    }

}

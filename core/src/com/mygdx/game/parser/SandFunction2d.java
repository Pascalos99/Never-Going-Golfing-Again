package com.mygdx.game.parser;

public class SandFunction2d extends Function2d.ConstantFunction2d {

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
        super(normal_friction);
        this.sand_friction = sand_friction;
        this.main = main;
        this.sand = sand;
    }

    @Override
    public double evaluate(double x, double y) {
        if (isSandAt(x, y)) return sand_friction;
        else return super.evaluate(x, y);
    }

    public boolean isSandAt(double x, double y) {
        return main.evaluate(x, y) < sand.evaluate(x, y);
    }

}

package com.mygdx.game;

import static com.mygdx.game.Variables.WORLD;

public class AI_Basic implements AI_controller {

    public String getTypeName() {
        return "Basic Bot";
    }

    public void calculate(Player player) {
        Vector2d currentPos = new Vector2d(player.getBall().x, player.getBall().y);
        Vector2d toFlag = WORLD.flag_position.sub(currentPos);
        angle = Math.atan2(toFlag.get_y(), toFlag.get_x());
        velocity = toFlag.get_length() * 0.75 + 0.5;
    }

    private double angle;
    private double velocity;

    public double getShotAngle() {
        return angle;
    }

    public double getShotVelocity() {
        return velocity;
    }
}
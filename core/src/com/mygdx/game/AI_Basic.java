package com.mygdx.game;

import static com.mygdx.game.Variables.WORLD;

public class AI_Basic extends AI_controller {

    public String getName() {
        return "Basic Bot";
    }

    public String getDescription() {
        return "Point and shoot based AI";
    }

    public void calculate(Player player) {
        Vector2d currentPos = new Vector2d(player.getBall().x, player.getBall().y);
        Vector2d toFlag = WORLD.flag_position.sub(currentPos);
        double angle = Math.atan2(toFlag.get_y(), toFlag.get_x());
        double velocity = AIUtils.unfoldDistance(currentPos, WORLD.flag_position, WORLD.height_function, 100) * 0.7;
        setShotAngle(angle);
        setShotVelocity(velocity);
    }

}
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
        double angle = toFlag.angle();
        double velocity = toFlag.get_length() * 0.75 + 0.5;
        // TODO perfect this mess
        setShotAngle(angle);
        setShotVelocity(velocity);
    }

}
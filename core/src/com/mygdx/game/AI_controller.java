package com.mygdx.game;

import static com.mygdx.game.Variables.GAME_ASPECTS;

public interface AI_controller {

    String getTypeName();

    void calculate(Player player);

    /** @return the desired shot angle in radians    */
    double getShotAngle();

    /** @return the desired shot velocity in m/s    */
    double getShotVelocity();

}

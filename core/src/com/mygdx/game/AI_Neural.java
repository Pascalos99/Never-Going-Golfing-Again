package com.mygdx.game;

import static com.mygdx.game.AIUtils.*;
import static com.mygdx.game.Variables.GAME_ASPECTS;
import static com.mygdx.game.Variables.WORLD;

public class AI_Neural extends AI_controller {

    @Override
    public String getName() {
        return "Neural Bot";
    }

    @Override
    public String getDescription() {
        return "Heuristic based AI with trained weights using a genetic algorithm";
    }

    @Override
    public void calculate(Player player) {
        if (WORLD.height_function != current_function) {
            current_function = WORLD.height_function;
            function_grid = asGrid(current_function, 5000);
        }
    }

    private Function2d current_function;
    private double[][] function_grid;

    @Override
    public double getShotAngle() {
        return Math.random() * Math.PI * 2;
    }

    @Override
    public double getShotVelocity() {
        return Math.random() * GAME_ASPECTS.maxVelocity;
    }
}

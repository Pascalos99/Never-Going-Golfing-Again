package com.mygdx.game;

import static com.mygdx.game.AIUtils.*;
import static com.mygdx.game.Variables.WORLD;

public class AI_Neural implements AI_controller {

    @Override
    public String getName() {
        return "Neural Bot";
    }

    @Override
    public String getDescription() {
        return "Heuristic based AI with trained weights using a genetic algorithm";
    }

    @Override
    public boolean calculate(Player player) {
        calculate_grid(5000);
        return true;
    }

    private Function2d current_function;
    private double[][] function_grid;

    private void calculate_grid(int resolution) {
        if (WORLD.height_function != current_function) {
            current_function = WORLD.height_function;
            function_grid = asGrid(current_function, resolution);
        }
    }

    @Override
    public double getShotAngle() {
        return 0;
    }

    @Override
    public double getShotVelocity() {
        return 0;
    }
}

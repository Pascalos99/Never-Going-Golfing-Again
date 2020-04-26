package com.mygdx.game;

public class AI_Genetic implements AI_controller {

    @Override
    public String getName() {
        return "Genetic Bot";
    }

    @Override
    public String getDescription() {
        return "Heuristic based AI with trained weights using a genetic algorithm";
    }

    @Override
    public void calculate(Player player) {

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

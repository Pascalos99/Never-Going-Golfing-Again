package com.mygdx.game;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.mygdx.game.Variables.WORLD;

public class AI_SimpleLearner implements AI_controller {

    public AI_SimpleLearner() {
        VMODs = new LinkedList<>();
        resetVMODs();
    }

    @Override
    public String getTypeName() {
        return "Simple Learn Bot";
    }

    @Override
    public void calculate(Player player) {
        Vector2d currentPos = new Vector2d(player.getBall().x, player.getBall().y);
        Vector2d toFlag = WORLD.flag_position.subtract(currentPos);
        angle = Math.atan2(toFlag.get_y(), toFlag.get_x());
        double distance_to_flag = toFlag.get_length();

        if (best_distance_to_flag - distance_to_flag < delta_distance_bound)
            VMODs.remove(Integer.valueOf(velocity_index));
        else
            resetVMODs();
        velocity_index = getTrial();

        last_distance_to_flag = distance_to_flag;
        if (last_distance_to_flag < best_distance_to_flag) best_distance_to_flag = last_distance_to_flag;
    }

    private static double[] velocity_modifiers = {0.125, 0.25, 0.5, 0.6, 0.7, 0.75, 0.8, 0.85, 0.9, 1.0, 1.1, 1.5, 2.0, 3.0, 4.0, 5.0};
    public static double delta_distance_bound = 0.5;

    private double angle;
    private int velocity_index;
    private List<Integer> VMODs;
    private double last_distance_to_flag = Double.MAX_VALUE;
    private double best_distance_to_flag = Double.MAX_VALUE;

    private void resetVMODs() {
        VMODs.clear();
        for (int i=0; i < velocity_modifiers.length; i++) VMODs.add(i);
        System.out.println("reset VMODs gives: "+VMODs);
    }

    private int getTrial() {
        if (VMODs.size() <= 0) resetVMODs();
        int select = VMODs.get(VMODs.size() / 2);
        System.out.println("selected "+velocity_modifiers[select]);
        return select;
    }

    @Override
    public double getShotAngle() {
        return angle;
    }

    @Override
    public double getShotVelocity() {
        return velocity_modifiers[velocity_index] * last_distance_to_flag;
    }
}

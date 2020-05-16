package com.mygdx.game.bots;

import com.mygdx.game.Player;
import com.mygdx.game.Vector2d;

import java.util.*;

import static java.lang.Math.PI;

public class AI_SimpleLearner extends AI_controller {

    public AI_SimpleLearner() {
        VMODs = new HashMap<>();
        last_distance_to_flag = new HashMap<>();
        best_distance_to_flag = new HashMap<>();
        angle_adjustment = new HashMap<>();
    }

    @Override
    public String getName() {
        return "Simple Learn Bot";
    }

    public String getDescription() {
        return "Trial and error based AI";
    }

    @Override
    public void calculate(Player player) {
        current_player = player;
        if (!has_seen_player(player)) setup(player);
        Vector2d currentPos = new Vector2d(player.getBall().x, player.getBall().y);
        Vector2d toFlag = getWorld().flag_position.sub(currentPos);
        angle = toFlag.angle();
        double distance_to_flag = toFlag.get_length();

        if (best_distance_to_flag.get(player) - distance_to_flag < delta_distance_bound)
            VMODs.get(player).remove(Integer.valueOf(velocity_index));
        else {
            resetVMODs(player);
            angle_adjustment.put(player, 0);
        }
        velocity_index = getTrial(player);
        last_distance_to_flag.put(player, distance_to_flag);

        if (last_distance_to_flag.get(player) < best_distance_to_flag.get(player))
            best_distance_to_flag.put(player, last_distance_to_flag.get(player));
    }

    public void clear() {
        VMODs = new HashMap<>();
        last_distance_to_flag = new HashMap<>();
        best_distance_to_flag = new HashMap<>();
        angle_adjustment = new HashMap<>();
    }

    private static double epi = PI/8;
    private static double[] velocity_modifiers = {0.7, 0.9, 1.5, 2.5};
    private static double[] angle_modifiers = {0, epi, -epi, 1.5*epi, -1.5*epi};
    public static double delta_distance_bound = 0.5;

    private double angle;
    private int velocity_index;
    private Player current_player;

    private Map<Player, List<Integer>> VMODs;
    private Map<Player, Double> last_distance_to_flag;
    private Map<Player, Double> best_distance_to_flag;
    private Map<Player, Integer> angle_adjustment;

    private boolean has_seen_player(Player player) {
        return VMODs.containsKey(player);
    }

    private void setup(Player player) {
        VMODs.put(player, new LinkedList<>());
        last_distance_to_flag.put(player, Double.MAX_VALUE);
        best_distance_to_flag.put(player, Double.MAX_VALUE);
        angle_adjustment.put(player, 0);
        resetVMODs(player);
    }

    private void resetVMODs(Player player) {
        VMODs.get(player).clear();
        for (int i=0; i < velocity_modifiers.length; i++) VMODs.get(player).add(i);
    }

    private int getTrial(Player player) {
        if (VMODs.get(player).size() <= 0) {
            nextAngleMod(player);
            resetVMODs(player);
        }
        int select = VMODs.get(player).get(VMODs.get(player).size() / 2);
        System.out.println("selected "+velocity_modifiers[select]);
        return select;
    }

    private void nextAngleMod(Player player) {
        int current = angle_adjustment.get(player) + 1;
        if (current >= angle_modifiers.length) current = 0;
        angle_adjustment.put(player, current);
    }

    @Override
    public double getShotAngle() {
        return angle + angle_modifiers[angle_adjustment.get(current_player)];
    }

    @Override
    public double getShotVelocity() {
        return velocity_modifiers[velocity_index] * last_distance_to_flag.get(current_player);
    }
}

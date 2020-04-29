package com.mygdx.game;

import java.util.ArrayList;
import java.util.List;

import static com.mygdx.game.Variables.WORLD;

public class AI_Finder extends AI_controller {
    public String getName() { return "Finder bot"; }

    public String getDescription() { return "Heuristic bot that chooses the best shot based on a selection of predictions"; }

    static class Shot {
        public Shot(double a, double v) {
            angle = a;
            velocity = v;
        }
        final double angle;
        final double velocity;
    }

    List<Shot> chosen_shots;

    protected void calculate(Player player) {
        if (chosen_shots.size() > player.getBall().hit_count) {
            setShotAngle(chosen_shots.get(player.getBall().hit_count).angle);
            setShotAngle(chosen_shots.get(player.getBall().hit_count).velocity);
            return; }
        List<Shot> suit = makeSuit(player.getBall().x, player.getBall().y);

    }

    private List<Shot> makeSuit(double x, double y) {
        ArrayList<Shot> list = new ArrayList<>();
        Vector2d currentPos = new Vector2d(x, y);
        Vector2d toFlag = getWorld().flag_position.sub(currentPos);
        double angle = Math.atan2(toFlag.get_y(), toFlag.get_x());
        return list;
    }

}

package com.mygdx.game.bots;

import com.mygdx.game.Player;
import com.mygdx.game.Vector2d;

import java.util.ArrayList;
import java.util.List;

public class AI_Finder extends AI_controller {
    public String getName() { return "Finder bot"; }

    public String getDescription() { return "Heuristic bot that chooses the best shot based on a selection of predictions"; }

    static class Shot {

        double angle;
        double velocity;
        Vector2d position;
        int maximum_ticks;
        int ticks_spent;
        boolean simulated = false;
        Shot next_shot = null;

        public Shot(double a, double v, Vector2d pos, int maximum_ticks) {
            angle = a;
            velocity = v;
            position = pos;
            this.maximum_ticks = maximum_ticks;
        }

        public void calculate() {

        }

        public Vector2d directionVector() {
            Vector2d one = new Vector2d(1,0);
            return one.rotate(angle);
        }
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
        double angle = toFlag.angle();
        return list;
    }

}

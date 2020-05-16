package com.mygdx.game.bots;

import com.mygdx.game.Ball;
import com.mygdx.game.Player;
import com.mygdx.game.utils.Vector2d;

public class AI_Finder extends AI_controller {
    public String getName() { return "Finder bot"; }

    public String getDescription() { return "Heuristic bot that chooses the best shot based on a selection of predictions"; }

    static class Shot {

        double angle;
        double velocity;
        int maximum_ticks;
        int ticks_spent = 0;
        boolean calculated = false;
        boolean attempt_success = false;

        public Shot(double a, double v, int maximum_ticks) {
            angle = a;
            velocity = v;
            this.maximum_ticks = maximum_ticks;
        }
        public Vector2d directionVector() {
            Vector2d one = new Vector2d(1,0);
            return one.rotate(angle);
        }
        public Ball attempt(Ball start) {
            Ball result = start.simulateHit(directionVector(), velocity, maximum_ticks, 0.001);
            attempt_success = !result.is_moving;
            ticks_spent += result.getTick_count();
            return result;
        }
    }

    protected void calculate(Player player) {
       Ball current = player.getBall();
    }

}

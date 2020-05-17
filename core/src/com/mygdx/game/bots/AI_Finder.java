package com.mygdx.game.bots;

import com.mygdx.game.Player;

public class AI_Finder extends AI_controller {
    public String getName() { return "Finder bot"; }

    public String getDescription() { return "Heuristic bot that chooses the best shot based on a selection of predictions"; }

    protected void calculate(Player player) {}

}
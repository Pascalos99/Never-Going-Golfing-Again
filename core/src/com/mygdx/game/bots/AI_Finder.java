package com.mygdx.game.bots;

import com.mygdx.game.Ball;
import com.mygdx.game.Player;
import com.mygdx.game.bots.tree_search.*;
import com.mygdx.game.utils.Vector2d;

import java.util.List;

public class AI_Finder extends AI_controller {
    public String getName() { return "Finder bot"; }

    public String getDescription() { return "Heuristic bot that uses A* and MCTS based tree search to find the optimal set of shots"; }

    protected void calculate(Player player) {
        // TODO make it remember a part of the tree if a nice route was found
        setupTreeSearch(player.getBall());
    }

    private SimulationTreeSearch tree_search;
    private HeuristicFunction heuristic;
    private StopCondition stopCondition;
    private GolfSuite suiteMaker;

    public AI_Finder() {

        suiteMaker = new GolfSuite();

        heuristic = n -> {
            GolfNode node = (GolfNode)n;
            if (!node.simulation_successful) return Double.MIN_VALUE;
            // TODO
            return 0;
        };

        stopCondition = n -> {
            GolfNode node = (GolfNode)n;
            // TODO
            return false;
        };

    }

    private boolean first_tree = true;
    private void setupTreeSearch(Ball root) {
        if (first_tree) tree_search = new SimulationTreeSearch(initial_node(root), heuristic, suiteMaker, stopCondition);
        else tree_search.rebase(initial_node(root));
    }

    private GolfNode initial_node(Ball current) {
        return new GolfNode(0, current, new Vector2d(0,0), 0) {
            protected double simulate() {
                simulation_successful = true;
                resulting_ball = start_ball;
                return 0;
            }
        };
    }

    class GolfNode extends Node {

        private final int MAX_TICKS = 8000;
        private final double STEP_SIZE = 0.001;

        Ball start_ball;
        Ball resulting_ball = null;
        private Vector2d direction;
        private double velocity;

        boolean simulation_successful;

        protected GolfNode(double estimate_quality, Ball start_ball, Vector2d direction, double velocity) {
            super(estimate_quality);
            this.start_ball = start_ball;
            this.direction = direction;
            this.velocity = velocity;
        }

        @Override
        protected synchronized double simulate() {
            double max_allowed_cost = tree_search.getMaximumCost() - tree_search.getTotalCost();
            resulting_ball = start_ball.simulateHit(direction, velocity, MAX_TICKS, STEP_SIZE);
            double cost = resulting_ball.ticks;
            boolean done_calculating = true;
            if (resulting_ball.is_moving) {
                done_calculating = false;
                while (cost < max_allowed_cost && !done_calculating) {
                    resulting_ball = resulting_ball.resumeSimulatedHit(MAX_TICKS, STEP_SIZE);
                    cost += resulting_ball.ticks;
                    if (!resulting_ball.is_moving) done_calculating = true;
                }
            } simulation_successful = done_calculating;
            return cost;
        }
    }

    class GolfSuite extends SuiteMaker {

        @Override
        protected List<Node> makeBareSuite(Node parent, SimulationTreeSearch tree, long seed) {
            return null;
        }
    }

}
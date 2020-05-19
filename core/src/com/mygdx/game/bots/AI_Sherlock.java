package com.mygdx.game.bots;

import com.mygdx.game.Ball;
import com.mygdx.game.Player;
import com.mygdx.game.bots.tree_search.*;
import com.mygdx.game.utils.Variables;
import com.mygdx.game.utils.Vector2d;

import java.util.ArrayList;
import java.util.List;

public class AI_Sherlock extends AI_controller {
    public String getName() { return "Sherlock bot"; }

    public String getDescription() { return "Heuristic bot that uses A* and MCTS based tree search to find the optimal set of shots"; }

    Node last_node = null;

    protected void calculate(Player player) {
        if (last_node == null) last_node = initial_node(player.getBall());
        setupTreeSearch(initial_node(player.getBall()));
        last_node = getFirstShotToNode(tree_search.completeTreeSearch(MAX_TICKS, 0));
        GolfNode shot = (GolfNode)last_node;
        setShotAngle(shot.direction.angle());
        setShotVelocity(shot.velocity);
    }

    private Node getFirstShotToNode(Node node) {
        Node to_return = node;
        while (to_return.getDepth() > 1) to_return = to_return.getParent();
        return to_return;
    }

    private static int TICK_INTERVAL = 1000;
    private static int MAX_TICKS = 1000000;
    private static double STEP_SIZE = Variables.DELTA;
    private static double CHILD_IMPROVEMENT = 0.8;

    private SimulationTreeSearch tree_search;
    private HeuristicFunction heuristic;
    private StopCondition stopCondition;
    private GolfSuite suiteMaker;

    public AI_Sherlock() {

        suiteMaker = new GolfSuite();

        heuristic = n -> {
            GolfNode node = (GolfNode)n;
            if (!node.simulation_successful || node.resulting_ball.isStuck()) return -Double.MAX_VALUE;
            Vector2d current = node.resulting_ball.topDownPosition();
            Vector2d goal = getWorld().flag_position;
            double current_distance = current.distance(goal);
            if (node.getDepth() <= 1) return -current_distance;
            double parent = node.getParent().getHeuristic() * (1 + CHILD_IMPROVEMENT);
            Vector2d previous = node.start_ball.topDownPosition();
            double previous_distance = previous.distance(goal);
            return parent + previous_distance - current_distance;
        };

        stopCondition = n -> {
            GolfNode node = (GolfNode)n;
            return node.getDepth() <= 1 && node.resulting_ball.isTouchingFlag();
        };

    }

    private boolean first_tree = true;
    private void setupTreeSearch(Node root) {
        /*if (first_tree)*/ tree_search = new SimulationTreeSearch(root, heuristic, suiteMaker, stopCondition);
        /*else {
            tree_search.rebase(root);
            tree_search.resetCost();
        }*/
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
            setHeuristic(heuristic);
        }

        @Override
        protected synchronized double simulate() {
            double max_allowed_cost = tree_search.getMaximumCost() - tree_search.getTotalCost();
            resulting_ball = start_ball.simulateHit(direction, velocity, TICK_INTERVAL, STEP_SIZE);
            double cost = resulting_ball.ticks;
            boolean done_calculating = true;
            if (resulting_ball.is_moving) {
                done_calculating = false;
                while (cost < max_allowed_cost && !done_calculating) {
                    resulting_ball = resulting_ball.resumeSimulatedHit(TICK_INTERVAL, STEP_SIZE);
                    cost += resulting_ball.ticks;
                    if (!resulting_ball.is_moving) done_calculating = true;
                }
            } simulation_successful = done_calculating;
            return cost;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("node at depth ").append(getDepth()).append(" {\n");
            sb.append("start = "+start_ball.topDownPosition()).append("\n");
            sb.append("angle = "+direction.angle()).append("\n");
            sb.append("speed = "+velocity).append("\n}");
            return sb.toString();
        }
    }

    class GolfSuite extends SuiteMaker {

        @Override
        protected List<Node> makeBareSuite(Node p, SimulationTreeSearch tree, long seed) {
            GolfNode parent = (GolfNode)p;
            // TODO improve size selection and speed factors
            int size = 10;
            double[] speed_factors = AIUtils.linearSpacing(0.1, 1.2, 10);
            int nums = speed_factors.length;
            List<Node> nodes = new ArrayList<Node>(size);
            double angle_range = Math.PI/2d + seed * 0.45;
            double interval = angle_range / size;

            for (int i=0; i < size * nums; i++) {
                Vector2d toFlag = getWorld().flag_position.sub(parent.start_ball.topDownPosition());
                double angle_mod = interval * (i/nums) - angle_range/2d;
                double angle = toFlag.angle() + angle_mod;
                double speed = speed_factors[i%nums] * toFlag.get_length();
                Vector2d direction = Vector2d.X.rotate(angle);
                boolean path_is_clear = AIUtils.isClearPath(parent.start_ball.topDownPosition(), getWorld().flag_position,
                        getWorld().height_function, 500, getWorld().hole_tolerance);
                double estimate = createEstimate(parent, angle_mod);
                if (path_is_clear) nodes.add(new GolfNode(estimate, parent.resulting_ball, direction, speed));
            }
            return nodes;
        }

        public double createEstimate(GolfNode node, double angle_mod) {
            return Math.random() * 3 - node.getDepth() - 2*Math.abs(angle_mod);
        }

    }

}
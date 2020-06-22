package com.mygdx.game.bots;

import com.mygdx.game.Ball;
import com.mygdx.game.Player;
import com.mygdx.game.bots.tree_search.*;
import com.mygdx.game.obstacles.Obstacle;
import com.mygdx.game.utils.DebugModule;
import com.mygdx.game.utils.Variables;
import com.mygdx.game.utils.Vector2d;

import java.util.*;

import static com.mygdx.game.utils.Variables.WORLD;

/**
 * aka. Estimated Suite-step Search | ESS
 */
public class AI_Sherlock extends AI_controller {

    public static boolean DEBUG = true;

    public String getName() { return "Estimated Suite-step Search"; }

    public String getDescription() { return "Heuristic bot that uses A* and MCTS based tree search to find the optimal set of shots"; }

    Node last_node = null;
    private static double ERROR_BOUND = 0.001;
    private static double CHILD_IMPROVEMENT = 0.8;

    protected void calculate(Player player) {
        debug.startChapter("CALCULATION");
        if (last_node == null) last_node = initial_node(player.getBall());
        else {
            Vector2d error = ((GolfNode)last_node).resulting_ball.topDownPosition().sub(player.getBall().topDownPosition());
            debug.debug("got an error of %.3f from last shot", error.get_length());
            if (error.get_length() > ERROR_BOUND) last_node = initial_node(player.getBall());
        }
        setupTreeSearch(initial_node(player.getBall()));
        debug.startChapter("SEARCH");
        Node result = tree_search.completeTreeSearch(MAX_TICKS, 0);
        debug.endChapter();
        last_node = getFirstShotToNode(result);
        GolfNode shot = (GolfNode)last_node;
        setShotAngle(shot.direction.angle());
        setShotVelocity(shot.velocity);
        debug.endChapter();
    }

    private Node getFirstShotToNode(Node node) {
        Node to_return = node;
        while (to_return.getDepth() > 1) to_return = to_return.getParent();
        return to_return;
    }

    private static int TICK_INTERVAL = 1000;
    private static int MAX_TICKS = 500000;
    private static double STEP_SIZE = Variables.DELTA;
    private static double EXPECTED_IMPROVEMENT_PER_SHOT = 1;

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
            boolean b = node.getDepth() <= 1 && node.resulting_ball.isTouchingFlag();
            return b;
        };

    }

    private boolean first_tree = true;
    private void setupTreeSearch(Node root) {
        if (first_tree) tree_search = new SimulationTreeSearch(root, heuristic, suiteMaker, stopCondition);
        else {
            tree_search.rebase(root);
            tree_search.resetCost();
        }
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
            debug.startSection("simulation");
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
            debug.endSection("simulation");
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
            debug.startSection("suite calculation");
            GolfNode parent = (GolfNode)p;
            // TODO improve size selection and speed factors
            int size = 50;
            int speed_precise = 10;
            double[] speed_factors = AIUtils.linearSpacing(0.1, 1, speed_precise);
            int nums = speed_factors.length;
            List<Node> nodes = new ArrayList<Node>(size);
            double angle_range = Math.PI/2d + seed * 0.45;
            double interval = angle_range / size;
            List<Obstacle> empty = new ArrayList<>(0);

            for (int i=0; i < size * nums; i++) {
                Vector2d toFlag = getWorld().flag_position.sub(parent.start_ball.topDownPosition());
                double angle_mod = interval * (i/nums) - angle_range/2d;
                double angle = toFlag.angle() + angle_mod;
                double speed = speed_factors[i%nums] * Variables.MAX_SHOT_VELOCITY;
                if (speed > getWorld().maximum_velocity) {
                    speed = getWorld().maximum_velocity;
                    i += nums - i%nums;
                }
                Vector2d direction = Vector2d.X.rotate(angle);
                double estimate = createEstimate(parent, direction, speed);
                nodes.add(new GolfNode(estimate, parent.resulting_ball, direction, speed));
            }
            debug.endSection("suite calculation");
            return nodes;
        }

        public double createEstimate(GolfNode parent, Vector2d direction, double speed) {
            debug.startSection("estimate heuristic");
            //Vector2d pos = parent.resulting_ball.topDownPosition();
            double result = 0;
            debug.endSection("estimate heuristic");
            return result;
        }

    }

    // DEBUGGING CODE
    public static DebugModule debug;
    static {
        debug = DebugModule.get("sherlock", DEBUG);
    }
    // END OF DEBUGGING CODE

}
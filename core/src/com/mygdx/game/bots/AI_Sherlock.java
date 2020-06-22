package com.mygdx.game.bots;

import com.mygdx.game.Ball;
import com.mygdx.game.Player;
import com.mygdx.game.bots.tree_search.*;
import com.mygdx.game.obstacles.Obstacle;
import com.mygdx.game.utils.Variables;
import com.mygdx.game.utils.Vector2d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * aka. Estimated Suite-step Search | ESS
 */
public class AI_Sherlock extends AI_controller {
    public String getName() { return "Estimated Suite-step Search"; }

    public String getDescription() { return "Heuristic bot that uses A* and MCTS based tree search to find the optimal set of shots"; }

    Node last_node = null;
    private static double ERROR_BOUND = 0.001;

    // DEBUGGING CODE
    private long last_time_ns = 0;
    private void startTimeCalc() {
        last_time_ns = System.nanoTime();
    }
    private long getTimeSpent() {
        return System.nanoTime() - last_time_ns;
    }
    public static boolean DEBUG = false;
    private static void debug(String str) {
        if (DEBUG) System.out.println("[SHERLOCK]: "+str);
    }
    private static void debug(String format, Object... parameters) {
        debug(String.format(format, parameters));
    }
    // END OF DEBUGGING CODE

    protected void calculate(Player player) {
        if (last_node == null) last_node = initial_node(player.getBall());
        else {
            Vector2d error = ((GolfNode)last_node).resulting_ball.topDownPosition().sub(player.getBall().topDownPosition());
            debug("got an error of %s from last shot", error);
            if (error.get_length() > ERROR_BOUND) last_node = initial_node(player.getBall());
        }
        setupTreeSearch(initial_node(player.getBall()));
        last_node = getFirstShotToNode(tree_search.completeAggregateTreeSearch(MAX_TICKS, -10, 0.5));
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
    private static int MAX_TICKS = 500000;
    // TODO wtf why is Sherlock so slow??
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
            return -current_distance;
        };

        stopCondition = n -> {
            GolfNode node = (GolfNode)n;
            return node.getDepth() <= 1 && node.resulting_ball.isTouchingFlag();
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
            debug("starting suite creation");
            //startTimeCalc();
            GolfNode parent = (GolfNode)p;
            // TODO improve size selection and speed factors
            Random rand = new Random(System.currentTimeMillis());
            int size = rand.nextInt(8) + 8;
            int speed_precise = rand.nextInt(8) + 8;
            double[] speed_factors = AIUtils.linearSpacing(0.1, 1, speed_precise);
            int nums = speed_factors.length;
            List<Node> nodes = new ArrayList<Node>(size);
            double angle_range = Math.PI/2d + seed * 0.45;
            double interval = angle_range / size;
            List<Obstacle> empty = new ArrayList<>(0);
            //debug("spent %d ns on suite initialization",getTimeSpent());

            for (int i=0; i < size * nums; i++) {
                //startTimeCalc();
                Vector2d toFlag = getWorld().flag_position.sub(parent.start_ball.topDownPosition());
                double angle_mod = interval * (i/nums) - angle_range/2d;
                double angle = toFlag.angle() + angle_mod;
                double speed = speed_factors[i%nums] * Variables.MAX_SHOT_VELOCITY;
                if (speed > getWorld().maximum_velocity) {
                    speed = getWorld().maximum_velocity;
                    i += nums - i%nums;
                }
                Vector2d direction = Vector2d.X.rotate(angle);
                //debug("spent %d ns on path check initialization",getTimeSpent());
                //startTimeCalc();
                boolean path_is_clear = AIUtils.isWaterFreePath(parent.start_ball.topDownPosition(),
                        direction.scale(speed), getWorld().height_function, empty, 1, speed * 2);
                //debug("spent %d ns on clear path checking",getTimeSpent());
                if (path_is_clear) {
                    double estimate = createEstimate(parent, direction, speed);
                    nodes.add(new GolfNode(estimate, parent.resulting_ball, direction, speed));
                }
            }
            return nodes;
        }

        public double createEstimate(GolfNode parent, Vector2d direction, double speed) {
            //Vector2d pos = parent.resulting_ball.topDownPosition();
            return Math.random();
        }

    }

}
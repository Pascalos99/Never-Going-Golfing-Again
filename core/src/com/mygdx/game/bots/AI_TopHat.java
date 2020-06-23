package com.mygdx.game.bots;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.mygdx.game.Ball;
import com.mygdx.game.Player;
import com.mygdx.game.utils.DebugModule;
import com.mygdx.game.utils.Variables;
import com.mygdx.game.utils.Vector2d;

import java.util.*;

public class AI_TopHat extends AI_controller {

    private int MAX_TICKS = 8000;
    private double STEP_SIZE = Variables.DELTA;
    private int ANGLE_PARTITION = 50;
    private int SPEED_PARTITION = 20;
    private double EAGERNESS_TO_EXPLORE = 2.5;
    private double ERROR_BOUND = 0.1;

    public static boolean DEBUG = true;

    public Heuristic SHOT_COUNT = n -> n.depth;
    public Heuristic DISTANCE = n -> {
        double distance = n.ball.topDownPosition().distance(getWorld().flag_position);
        return distance / getWorld().maximum_velocity;
    };
    public Explorer GAUSSIAN = new Explorer() {
        @Override
        public Node[] exploreNode(Node parent) {
            Node[] result = new Node[ANGLE_PARTITION * SPEED_PARTITION];
            double vmax = getWorld().maximum_velocity;
            double start_v = vmax / SPEED_PARTITION;
            double[] angles = createAnglePartitions(parent.ball.topDownPosition());
            for (int i=0; i < ANGLE_PARTITION; i++) {
                int k = 0;
                for (int j=0; j < SPEED_PARTITION; j++) {
                    double v = AIUtils.linearInterpolate(start_v, vmax, j/((double)SPEED_PARTITION));
                    if (v == 0) result[i * SPEED_PARTITION + k++] = parent.shoot(v, getWorld().maximum_velocity);
                    else result[i * SPEED_PARTITION + k++] = parent.shoot(v, angles[i]);
                }
            }
            return result;
        }
        private Random random = new Random(System.currentTimeMillis());
        private double[] createAnglePartitions(Vector2d currentPos) {
            double[] angle_partition = new double[ANGLE_PARTITION];
            angle_partition[0] = 0;
            double sigma = g(currentPos, getWorld().flag_position);
            for (int i=1; i < angle_partition.length; i++) {
                double val = 2*Math.PI;
                int n = 0;
                while ((val > Math.PI || val < -Math.PI) && (n < 20)) { val = random.nextGaussian() * sigma; n++; }
                angle_partition[i] = val;
            }
            return angle_partition;
        }
        private double g(Vector2d c, Vector2d d) {
            double max_distance = getWorld().start_position.distance(d);
            double current_distance = c.distance(d);
            return EAGERNESS_TO_EXPLORE * current_distance / max_distance;
        }
    };
    public Explorer ALL_ROUND = n -> {
            Node[] result = new Node[ANGLE_PARTITION * SPEED_PARTITION];
            double vmax = getWorld().maximum_velocity;
            double start_v = vmax / SPEED_PARTITION;
            double[] angles = equal_partition_of_angles(ANGLE_PARTITION);
            for (int i=0; i < ANGLE_PARTITION; i++) {
                int k = 0;
                for (int j=0; j < SPEED_PARTITION; j++) {
                    double v = AIUtils.linearInterpolate(start_v, vmax, j/((double)SPEED_PARTITION));
                    if (v == 0) result[i * SPEED_PARTITION + k++] = n.shoot(v, getWorld().maximum_velocity);
                    else result[i * SPEED_PARTITION + k++] = n.shoot(v, angles[i]);
                }
            }
            return result;
    };

    private double[] equal_partition_of_angles(int count) {
        double[] result = new double[count];
        result[0] = 0;
        for (int i=1; i < result.length; i++)
            result[i] = AIUtils.linearInterpolate(-Math.PI, Math.PI, i / ((double)(result.length - 1)));
        return result;
    }

    private Node root;
    private Node last_node;
    private Node result;
    private PriorityQueue<Node> expandable_nodes;
    private List<Node> all_nodes;
    private Heuristic h_value = DISTANCE;
    private Heuristic g_cost = SHOT_COUNT;
    private Explorer explorer = ALL_ROUND;
    private boolean first_shot;
    private int current_depth;
    private boolean found_solution;

    public AI_TopHat() {
        clear();
    }

    @Override
    public void clear() {
        clearTree();
        first_shot = true;
    }

    private void clearTree() {
        expandable_nodes = new PriorityQueue<>();
        all_nodes = new ArrayList<>();
        current_depth = 1;
        found_solution = false;
        result = null;
        root = null;
    }

    private void restart(Player player) {
        root = new Node(player.getBall());
        expandable_nodes.add(root);
    }

    @Override
    public String getName() {
        return "Heuristic Dijkstra Search";
    }

    @Override
    public String getDescription() {
        return "An implementation of the Dijkstra inspired A* algorithm";
    }

    private Vector2d previous_position;

    @Override
    protected void calculate(Player player) {
        Vector2d current_position = player.getBall().topDownPosition();
        double error;
        boolean error_exceeded = false;
        if (!first_shot) {
            error = last_node.ball.topDownPosition().sub(player.getBall().topDownPosition()).get_length();
            if (error > ERROR_BOUND) {
                debug.debug("Error of %.3f exceeds error bound of %.3f\n", error, ERROR_BOUND);
                error_exceeded = true;
            } else if (current_position.distance(previous_position) < 0.001) {
                debug.debug("Ball ended up in water unexpectedly");
                error_exceeded = true;
            }
        }
        if (first_shot) {
            restart(player);
            result = search();
            first_shot = false;
        }
        else if (!found_solution || error_exceeded) {
            debug.debug("didn't find a solution, re-starting search");
            rebase(player);
            result = search();
        }
        Node next_shot = getNodeAtDepth(result, current_depth++);
        setShotAngle(next_shot.angle);
        setShotVelocity(next_shot.speed);
        last_node = next_shot;
        previous_position = current_position;
    }

    private void rebase(Player player) {
        clearTree();
        restart(player);
    }

    private Node getNodeAtDepth(Node node, int depth) {
        if (node.depth <= depth) return node;
        return getNodeAtDepth(node.parent, depth);
    }

    private Node search() {
        long start = System.currentTimeMillis();
        long time_spent = 0;
        while (time_spent < 5000) {
            exploreNode(expandable_nodes.poll());
            time_spent = System.currentTimeMillis() - start;
        }
        if (result == null || !found_solution) {
            Node best = null;
            double best_h = Double.POSITIVE_INFINITY;
            for (Node n : all_nodes)
                    if (n != null && n.getHeuristic() < best_h) {
                        best_h = n.getHeuristic();
                        best = n;
                    }
            return best;
        }
        if (found_solution) debug.debug("Found a solution in %d shot%s",result.depth,result.depth==1?"":"s");
        return result;
    }

    private void exploreNode(Node n) {
        Node[] nodes = explorer.exploreNode(n);
        for (int k=0; k < nodes.length; k++) {
            Node node = nodes[k];
            if (node == null) continue;
            expandable_nodes.add(node);
            all_nodes.add(node);
            if (node.reached_goal && (result == null || node.getHeuristic() < result.getHeuristic())) {
                result = node;
                found_solution = true;
            }
        }
    }

    @FunctionalInterface
    interface Explorer {
        Node[] exploreNode(Node n);
    }

    @FunctionalInterface
    interface Heuristic {
        double evaluate(Node node);
    }

    class Node implements Comparable<Node> {
        final Node parent;
        final int depth;
        final Ball ball;
        final double heuristic_value;
        final double g_cost_value;
        final boolean reached_goal;
        List<Node> children;
        double speed;
        double angle;
        public Node(Ball initial_ball) {
            children = new ArrayList<>();
            parent = null;
            ball = initial_ball;
            depth = 0;
            reached_goal = ball.topDownPosition().distance(getWorld().flag_position) < getWorld().hole_tolerance;
            heuristic_value = Double.POSITIVE_INFINITY;
            g_cost_value = Double.POSITIVE_INFINITY;
        }
        private Node(Node parent, double speed, double angle) throws ExceptionInInitializerError {
            children = new ArrayList<>();
            this.parent = parent;
            parent.children.add(this);
            this.ball = parent.ball.simulateHit(Vector2d.X.rotate(angle), speed, MAX_TICKS, STEP_SIZE);
            if (ball.ticks == MAX_TICKS) throw new ExceptionInInitializerError("simulation exceeds max tick count");
            this.speed = speed;
            this.angle = angle;
            reached_goal = ball.topDownPosition().distance(getWorld().flag_position) < getWorld().hole_tolerance;
            if (ball.isStuck() && !reached_goal) throw new ExceptionInInitializerError("simulated ball hit water");
            depth = parent.depth + 1;
            heuristic_value = h_value.evaluate(this);
            g_cost_value = g_cost.evaluate(this);
        }
        public Node shoot(double speed, double angle) {
            Node child;
            try {
                child = new Node(this, speed, angle);
            } catch (ExceptionInInitializerError e) {
                return null;
            }
            return child;
        }

        public double getHeuristic() {
            return heuristic_value + g_cost_value;
        }

        @Override
        public int compareTo(Node n) {
            if (getHeuristic() < n.getHeuristic()) return -1;
            if (getHeuristic() == n.getHeuristic()) return 0;
            return 1;
        }
    }

    public static DebugModule debug = DebugModule.get("tophat", DEBUG);
}

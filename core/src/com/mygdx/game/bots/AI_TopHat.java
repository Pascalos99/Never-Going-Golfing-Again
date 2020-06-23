package com.mygdx.game.bots;

import com.mygdx.game.Ball;
import com.mygdx.game.Player;
import com.mygdx.game.utils.Variables;
import com.mygdx.game.utils.Vector2d;

import java.util.*;

public class AI_TopHat extends AI_controller {

    private int MAX_TICKS = 8000;
    private double STEP_SIZE = Variables.DELTA;
    private int GRID_RESOLUTION = 1000;
    private int ANGLE_PARTITION = 20;
    private int SPEED_PARTITION = 50;
    private double EAGERNESS_TO_EXPLORE = 1.5;
    private double WORLD_BOUND = Variables.BOUNDED_WORLD_SIZE;

    public Heuristic SHOT_COUNT = n -> n.depth;
    public Heuristic DISTANCE = n -> {
        double distance = n.ball.topDownPosition().distance(getWorld().flag_position);
        return distance / getWorld().maximum_velocity;
    };
    public Explorer GAUSSIAN = new Explorer() {
        @Override
        public Node[] exploreNode(Node parent) {
            Node[] result = new Node[ANGLE_PARTITION * SPEED_PARTITION - 1];
            double speed_increase = getWorld().maximum_velocity / SPEED_PARTITION;
            double[] angles = createAnglePartitions(parent.ball.topDownPosition());
            for (int i=0; i < ANGLE_PARTITION; i++) {
                int k = 0;
                for (double v = speed_increase; v <= getWorld().maximum_velocity; v += speed_increase) {
                    result[i * SPEED_PARTITION + k++] = parent.shoot(v, angles[i]);
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

    private Node root;
    private Node last_node;
    private Node result;
    private PriorityQueue<Node> expandable_nodes;
    private Heuristic h_value = DISTANCE;
    private Heuristic g_cost = SHOT_COUNT;
    private Explorer explorer = GAUSSIAN;
    private boolean first_shot;
    private int current_depth;
    private boolean found_solution;

    private Node[][] total_node_grid;
    private boolean[][] stationary_grid;

    public AI_TopHat() {
        clear();
    }

    @Override
    public void clear() {
        expandable_nodes = new PriorityQueue<>();
        total_node_grid = new Node[GRID_RESOLUTION][GRID_RESOLUTION];
        stationary_grid = null;
        first_shot = true;
        current_depth = 1;
        found_solution = false;
        result = null;
        root = null;
    }

    @Override
    public String getName() {
        return "Heuristic Dijkstra Search";
    }

    @Override
    public String getDescription() {
        return "An implementation of the Dijkstra inspired A* algorithm";
    }

    @Override
    protected void calculate(Player player) {
        if (first_shot) {
            stationary_grid = AIUtils.convertPointsToArray(AIUtils.getStationaryPoints(getWorld().height_function,
                    Variables.GRADIENT_CUTTOFF, WORLD_BOUND, GRID_RESOLUTION), WORLD_BOUND, GRID_RESOLUTION);
            root = new Node(player.getBall());
            expandable_nodes.add(root);
            result = search();
            first_shot = false;
        }
        else if (!found_solution) {
            System.out.println("didn't find a solution, re-starting search");
            rebase(last_node);
            result = search();
        }
        Node next_shot = getNodeAtDepth(result, current_depth++);
        setShotAngle(next_shot.angle);
        setShotVelocity(next_shot.speed);
        last_node = next_shot;
    }

    private Node getNodeAtDepth(Node node, int depth) {
        if (node.depth <= depth) return node;
        return getNodeAtDepth(node.parent, depth);
    }

    private void rebase(Node base) {
        root = base;
        // TODO more
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
            for (int i=0; i < total_node_grid.length; i++)
                for (int j=0; j < total_node_grid[i].length; j++) {
                    Node n = total_node_grid[i][j];
                    if (n != null && n.getHeuristic() < best_h) {
                        best_h = n.getHeuristic();
                        best = n;
                    }
                }
            return best;
        } return result;
    }

    private void exploreNode(Node n) {
        Node[] nodes = explorer.exploreNode(n);
        List<Node> simulated_nodes = List.of(nodes); // TODO fix nullpointer exception
        for (Node node : simulated_nodes) {
            Vector2d index = AIUtils.getClosestValidArrayIndex(node.ball.topDownPosition(), stationary_grid, WORLD_BOUND);
            int i = (int)index.get_x(), j = (int)index.get_y();
            Node previous = total_node_grid[i][j];
            if (previous == null) {
                expandable_nodes.add(node);
                total_node_grid[i][j] = node;
            }
            else if (node.g_cost_value < previous.g_cost_value) {
                    expandable_nodes.remove(previous);
                    expandable_nodes.add(node);
                    total_node_grid[i][j] = node;
                }
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
        public Node(Node parent, double speed, double angle) throws ExceptionInInitializerError {
            children = new ArrayList<>();
            this.parent = parent;
            parent.children.add(this);
            this.ball = parent.ball.simulateHit(Vector2d.X.rotate(angle), speed, MAX_TICKS, STEP_SIZE);
            if (ball.ticks == MAX_TICKS) throw new ExceptionInInitializerError("simulation exceeds max tick count");
            this.speed = speed;
            this.angle = angle;
            reached_goal = ball.topDownPosition().distance(getWorld().flag_position) < getWorld().hole_tolerance;
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
}

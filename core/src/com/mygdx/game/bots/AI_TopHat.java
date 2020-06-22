package com.mygdx.game.bots;

import com.mygdx.game.Ball;
import com.mygdx.game.Player;
import com.mygdx.game.utils.Variables;
import com.mygdx.game.utils.Vector2d;

import java.util.PriorityQueue;
import java.util.Random;

public class AI_TopHat extends AI_controller {

    private int MAX_TICKS = 8000;
    private double STEP_SIZE = Variables.DELTA;
    private int GRID_RESOLUTION = 1000;
    private int ANGLE_PARTITION = 20;
    private int SPEED_PARTITION = 50;
    private double EAGERNESS_TO_EXPLORE = 1.5;

    public Heuristic SHOT_COUNT = n -> n.depth;
    public Heuristic DISTANCE = n -> {
        double distance = n.ball.topDownPosition().distance(getWorld().flag_position);
        return distance / getWorld().maximum_velocity;
    };
    public Explorer GAUSSIAN = new Explorer() {
        @Override
        public Node[] exploreNode(Node n) {
            Node[] result = new Node[ANGLE_PARTITION * SPEED_PARTITION];
            double speed_increase = getWorld().maximum_velocity / SPEED_PARTITION;
            double[] angles = createAnglePartitions(n.ball.topDownPosition());
            for (int i=0; i < ANGLE_PARTITION; i++) {
                int k = 0;
                for (double v = speed_increase; v <= getWorld().maximum_velocity; v += speed_increase)
                    result[i*ANGLE_PARTITION + k++] = new Node(n, v, angles[i]);
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
    private Node result;
    private PriorityQueue<Node> expandable_nodes;
    private Heuristic h_value = DISTANCE;
    private Heuristic g_cost = SHOT_COUNT;
    private Explorer explorer = GAUSSIAN;
    private boolean first_shot;
    private int current_depth;

    private Node[][] total_node_grid;

    public AI_TopHat() {
        clear();
    }

    @Override
    public void clear() {
        expandable_nodes = new PriorityQueue<>();
        total_node_grid = new Node[GRID_RESOLUTION][GRID_RESOLUTION];
        first_shot = true;
        current_depth = 1;
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
            root = new Node(player.getBall());
            expandable_nodes.add(root);
            result = search(root);
            first_shot = false;
        }
        Node next_shot = getNodeAtDepth(result, current_depth++);
        setShotAngle(next_shot.angle);
        setShotVelocity(next_shot.speed);
    }

    private Node getNodeAtDepth(Node node, int depth) {
        if (node.depth <= depth) return node;
        return getNodeAtDepth(node.parent, depth);
    }

    private Node search(Node root) {
        return root;
    }

    private void exploreNode(Node n) {
        Node[] simulated_nodes = explorer.exploreNode(n);

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
        double speed;
        double angle;
        public Node(Ball initial_ball) {
            parent = null;
            ball = initial_ball;
            speed = 0;
            angle = 0;
            depth = 0;
            heuristic_value = Double.POSITIVE_INFINITY;
            g_cost_value = Double.POSITIVE_INFINITY;
        }
        public Node(Node parent, double speed, double angle) throws ExceptionInInitializerError {
            this.parent = parent;
            this.ball = parent.ball.simulateHit(Vector2d.X.rotate(angle), speed, MAX_TICKS, STEP_SIZE);
            if (ball.ticks == MAX_TICKS) throw new ExceptionInInitializerError("simulation exceeds max tick count");
            this.speed = speed;
            this.angle = angle;
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

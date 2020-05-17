package com.mygdx.game.bots.tree_search;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SimulationTreeSearch {
    private Node root_node;
    private List<Node> endorsed_nodes;
    private List<Node> suggested_nodes;
    private Node best_node;
    private double total_cost;
    private double max_cost;
    private double minimum_improvement;

    public final HeuristicFunction heuristic;
    public final SuiteMaker suite_maker;
    public final StopCondition stop_condition;

    private boolean stop_simulation;

    /**
     * @param initial_node it is advised to have an initial node with an h-value of Double.MIN_VALUE and a cost of 0<br>
     *                     the estimate-heuristic does not matter for the initial node as it will be simulated no matter what
     * @param heuristic the heuristic function that determines the quality of each node and will partially determine whether a
     *                  node is endorsed or not.
     * @param suite_maker the mechanism used for generating test suites to run simulations on. Not all suggested explorations will
     *                    actually be explored (for example when a solution is found or the cost limit has been reached)
     * @param stop_condition the condition used for determining whether a certain node is a solution or not. Every node that is
     *                       simulated will also be tested for this condition, regardless of the heuristic value of that node
     */
    public SimulationTreeSearch(Node initial_node, HeuristicFunction heuristic, SuiteMaker suite_maker, StopCondition stop_condition) {
        this.heuristic = heuristic;
        this.suite_maker = suite_maker;
        this.stop_condition = stop_condition;
        endorsed_nodes = new ArrayList<>();
        suggested_nodes = new ArrayList<>();
        root_node = initial_node;
        total_cost = 0;
        simulateNode(root_node);
    }

    /**
     * creates a Tree Search without stop condition
     */
    public SimulationTreeSearch(Node initial_node, HeuristicFunction heuristic, SuiteMaker suite_maker) {
        this(initial_node, heuristic, suite_maker, n -> false);
    }

    public Node completeTreeSearch(double max_cost, double minimum_improvement) {
        startTreeSearch(max_cost, minimum_improvement);
        return best_node;
    }

    public void startTreeSearch(double max_cost, double minimum_improvement) {
        this.max_cost = max_cost;
        this.minimum_improvement = minimum_improvement;
        // TODO implementation of algorithm
        // TODO step when 'stop_simulation' is true, the simulation should return best_node immediately
    }

    public double getTotalCost() {
        return total_cost;
    }
    public double getBestHeuristicValue() {
        return best_node.getHeuristic();
    }
    public Node getBestNode() {
        return best_node;
    }

    private void makeSuite(Node from) {
        List<Node> suite = suite_maker.makeSuite(from, this, from.current_suite_seed++);
        for (Node node : suite) suggested_nodes.add(node);
    }

    private void simulateNode(Node node) {
        if (!node.isSimulated()) node.computeSimulation();
        total_cost += node.getCost();
        double h_value = node.getHeuristic();
        suggested_nodes.remove(node);
        if (node == root_node || h_value > getBestHeuristicValue()) best_node = node;
        // pruning step:
        else if (stop_condition.isSolution(node)) {
            best_node = node;
            stop_simulation = true;
            return;
        }
        else if (h_value < node.parent.getHeuristic() + minimum_improvement) return;

        endorsed_nodes.add(node);
        makeSuite(node);
    }
}







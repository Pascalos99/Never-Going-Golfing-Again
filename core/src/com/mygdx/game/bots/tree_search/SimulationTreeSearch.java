package com.mygdx.game.bots.tree_search;

import java.util.*;

public class SimulationTreeSearch {
    private Node root_node;
    private List<Node> endorsed_nodes;
    private List<Node> suggested_nodes;
    private Node best_node;
    private double total_cost;
    private double maximum_cost;
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
        suggested_nodes = auto_sorted_node_list();
        root_node = initial_node;
        total_cost = 0;
        simulateNode(root_node, true);
    }

    /**
     * creates a Tree Search without stop condition
     * @see #SimulationTreeSearch(Node, HeuristicFunction, SuiteMaker, StopCondition) 
     */
    public SimulationTreeSearch(Node initial_node, HeuristicFunction heuristic, SuiteMaker suite_maker) {
        this(initial_node, heuristic, suite_maker, n -> false);
    }

    public Node completeTreeSearch(double max_cost, double minimum_improvement) {
        startTreeSearch(max_cost, minimum_improvement);
        return best_node;
    }

    public void rebase(Node new_root) {
        root_node = new_root;
        new_root.setDepth(0);
        if (endorsed_nodes.contains(new_root)) {
            clear(false);
            addAllChildren(new_root);
        } else clear(true);
    }

    public Node getRoot() {
        return root_node;
    }

    private void addAllChildren(Node from) {
        from.updateHeuristic();
        for (Node child : from.children) {
            if (child.isSimulated() && child.getHeuristic() >= from.getHeuristic() + minimum_improvement) {
                endorsed_nodes.add(child);
                addAllChildren(child);
            } else if (!child.isSimulated()) {
                suggested_nodes.add(child);
            }
        }
    }
    public void reset() {
        clear(true);
    }
    public void clear(boolean makeNewSuiteAtRoot) {
        best_node = root_node;
        endorsed_nodes.clear();
        suggested_nodes.clear();
        resetCost();
        simulateNode(root_node, makeNewSuiteAtRoot);
    }

    /**
     *
     * @param max_cost the maximum cost for running the search; the cost value is the sum of all costs generated by node
     *                 simulations. Therefore the unit and scale of this value is fully determined by the implementor.
     *                 Set this value to a negative number to ignore costs (this will result in an infinite loop if the
     *                 stop condition isn't sufficient)
     * @param minimum_improvement minimum improvement in the heuristic function for a node to be used for further exploration
     *                            of the tree. Setting this value to 0 means h-values equal to the parent's value are still allowed,
     *                            but anything below it will be pruned off. A negative value allows worse individuals to still be
     *                            used for further exploration (results in more deep solutions; which might not be desired)
     */
    public void startTreeSearch(double max_cost, double minimum_improvement) {
        this.minimum_improvement = minimum_improvement;
        this.maximum_cost = max_cost;
        stop_simulation = false;
        while (total_cost <= max_cost && !stop_simulation) {
            Node next_simulation = selectSuggestedNode();
            simulateNode(next_simulation, true);
        }
        validateBestNode();
    }

    public double getTotalCost() {
        return total_cost;
    }
    public double getMaximumCost() {
        return maximum_cost;
    }
    public double getBestHeuristicValue() {
        return best_node.getHeuristic();
    }
    public Node getBestNode() {
        return best_node;
    }
    public void resetCost() {
        total_cost = 0;
    }
    public boolean hasTriggeredStopCondition() {
        return stop_simulation;
    }

    /**
     * @return the suggested node for simulation with the highest estimate-heuristic or an Exception if there are no
     *          suggested nodes to select from.
     */
    private Node selectSuggestedNode() {
        return suggested_nodes.get(0);
    }

    /**
     * relies on the assumption that this method is called only once per child after it has been simulated<br>
     * makes sure the parent node is aware of the simulation of this node and creates a new suite from its parent
     * when all its siblings and itself have been simulated (this method marks the current node as simulated to the
     * parent, but will only do so if it has already marked itself as simulated)
     */
    private void updateParentSuggestedChildrenCount(Node child) {
        if (!child.isSimulated()) return;
        Node parent = child.parent;
        if (parent == null) return;
        parent.suggested_children_count--;
        if (parent.suggested_children_count <= 0) makeSuite(parent);
    }

    private void simulateNode(Node node, boolean makeSuite) {
        if (!node.isSimulated()) {
            node.computeSimulation();
            updateParentSuggestedChildrenCount(node);
        }
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
        if (makeSuite) makeSuite(node);
    }

    public void validateBestNode() {
        if (stop_simulation) return; // the 'best_node' satisfies the stop condition, so it does not have to be validated
        Node original_best = best_node;
        Node current_best = best_node;
        while (true) {
            for (Node child : current_best.children)
                if (endorsed_nodes.contains(child)) return; // there is a child node that is better than best_node
            resetCost();
            while (current_best.suggested_children_count <= 0) makeSuite(current_best);
            for (Node child : current_best.children) {
                simulateNode(child, false);
                if (endorsed_nodes.contains(child)) return; // there is a child node that is better than best_node
            }
            endorsed_nodes.remove(current_best);
            if (endorsed_nodes.size() <= 1) { best_node = original_best; return; }
            current_best = root_node;
            for (Node node : endorsed_nodes) if (node.getHeuristic() > current_best.getHeuristic()) current_best = node;
            best_node = current_best;
        }
    }

    /**
     * Makes the next suite for testing starting from the given node and adds all elements in the suite to the
     * suggested nodes of this tree search
     */
    private void makeSuite(Node from) {
        List<Node> suite = suite_maker.makeSuite(from, this, from.current_suite_count++);
        for (Node node : suite) suggested_nodes.add(node);
    }

    /**
     * implements a simple version of an auto-sorted linked-list
     * @return a LinkedList that automatically sorts incoming elements
     */
    private LinkedList<Node> auto_sorted_node_list() {
        return new LinkedList<Node>() {
            @Override
            public boolean add(Node node) {
                ListIterator<Node> iter = this.listIterator();
                boolean is_inserted = false;
                while (iter.hasNext()) {
                    Node current = iter.next();
                    if (node.estimate_heuristic > current.estimate_heuristic) {
                        iter.previous();
                        iter.add(node);
                        is_inserted = true;
                        break;
                    }
                }
                if (!is_inserted) iter.add(node);
                return true;
            }

            @Override
            public boolean addAll(Collection<? extends Node> elements) {
                for (Node node : elements) add(node);
                return true;
            }

            @Override
            public boolean addAll(int index, Collection<? extends Node> elements) {
                // there is no use in changing the starting index, because the list needs to remain sorted
                return addAll(elements);
            }
        };
    }
}







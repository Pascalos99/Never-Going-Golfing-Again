package com.mygdx.game.bots.tree_search;

import java.util.ArrayList;
import java.util.List;

public abstract class Node implements Comparable<Node> {
    /**
     * @param estimate_quality the estimated value of the heuristic that determines the order of simulation execution.<br>
     *                         the actual value of the estimated_heuristic will never be compared with the heuristic_value
     *                         of any node, so this value can be anything, but it is customary to make it depend on the
     *                         heuristic_value of the parent and the estimated benefit to the heuristic value that this
     *                         exploration will provide.
     */
    protected Node(double estimate_quality) {
        current_suite_count = 0;
        estimate_heuristic = estimate_quality;
        children = new ArrayList<>();
    }

    protected final void setHeuristic(HeuristicFunction heuristic) {
        this.heuristic = heuristic;
    }

    final void setParent(Node parent) {
        this.parent = parent;
        if (parent==null) depth = 0;
        else {
            setDepth(parent.depth + 1);
            parent.children.add(this);
            if (!simulated) parent.suggested_children_count++;
        }
    }

    /**
     * Simulates the exploration step of this node, thereby updating the initial state of the node such that
     *   the heuristic and suite can be computed (the initial node will also be simulated; this is mostly so that a value
     *   for the initial heuristic and suite can be computed and does not need to actually simulate anything)
     * @return the 'cost' of this simulation; this could be the elapsed time or something else, it will signify when the search
     *   should definitely be concluded (if a total cost is reached exceeding the required maximum)
     */
    protected abstract double simulate();

    public final double estimate_heuristic;

    long current_suite_count;
    final List<Node> children;
    HeuristicFunction heuristic;
    Node parent;
    int depth;
    int suggested_children_count = 0;

    private boolean simulated;
    private double heuristic_value;
    private double cost;

    public final void computeSimulation() {
        cost = simulate();
        simulated = true;
        updateHeuristic();
    }
    public final void updateHeuristic() {
        heuristic_value = heuristic.calculate(this);
    }
    void setDepth(int depth) {
        this.depth = depth;
        for (Node child : children) child.setDepth(depth + 1);
    }
    public final boolean isSimulated() {
        return simulated;
    }
    public final double getHeuristic() {
        return heuristic_value;
    }
    public final double getCost() {
        return cost;
    }
    public final Node getParent() {
        return parent;
    }
    public final int getDepth() {
        return depth;
    }
    public final Node[] getChildren() {
        return children.toArray(new Node[children.size()]);
    }

    @Override
    public int compareTo(Node n) {
        if (n.estimate_heuristic > estimate_heuristic) return -1;
        if (n.estimate_heuristic == estimate_heuristic) return 0;
        return 1;
    }

}
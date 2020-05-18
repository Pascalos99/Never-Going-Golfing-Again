package com.mygdx.game.bots.tree_search;

@FunctionalInterface
public interface StopCondition {

    /**
     * @param current_node the current node in the tree search; each node will be evaluated at least once (including the
     *                     initial node)
     * @return {@code true} if the simulation should return this node as best node and stop the search immediately,
     *         {@code false} otherwise
     */
    boolean isSolution(Node current_node);

}

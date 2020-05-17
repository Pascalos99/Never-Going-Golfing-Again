package com.mygdx.game.bots.tree_search;

@FunctionalInterface
public interface HeuristicFunction {
    /**
     * @param node the simulated node for which the h-value must be calculated
     * @return the absolute heuristic value corresponding to the simulation results from {@code node}
     */
    double calculate(Node node);
}
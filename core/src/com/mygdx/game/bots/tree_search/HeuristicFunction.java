package com.mygdx.game.bots.tree_search;

@FunctionalInterface
public interface HeuristicFunction {
    /**
     * @param node the simulated node for which the h-value must be calculated
     * @return the heuristic value corresponding to the simulation results from {@code node}, the higher this return value,
     *         the better the {@code node} is.
     */
    double calculate(Node node);

}
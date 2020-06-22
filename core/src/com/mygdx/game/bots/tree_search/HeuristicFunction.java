package com.mygdx.game.bots.tree_search;

@FunctionalInterface
public interface HeuristicFunction {
    /**
     * @param node the simulated node for which the h-value must be calculated
     * @return the absolute heuristic value corresponding to the simulation results from {@code node}
     */
    double calculate(Node node);

    // TODO implementation of heuristic where h decreases as depth increases according to a minimum_improvement factor:
    /** double factor = 1 + percentage_improvement;
     *  if (depth <= 0) h = -distance(current, goal)
     *  else h = parent.h * factor + distance(previous, goal) - distance(current, goal)
     *
     */

}
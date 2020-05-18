package com.mygdx.game.bots.tree_search;

import java.util.List;

public abstract class SuiteMaker {
    /**
     * @param parent parent of the nodes to be created, must be the parent in all nodes given in this suite
     * @param tree all the nodes is this tree-search instance, can be used to query things like "best h-value" and
     *             "total cost" to determine the size of the suite or precision of steps between nodes in the suite.
     * @return a list of all suggested nodes to be added to the tree-search
     */
    protected abstract List<Node> makeBareSuite(Node parent, SimulationTreeSearch tree, long seed);

    List<Node> makeSuite(Node parent, SimulationTreeSearch tree, long seed) {
        List<Node> result = makeBareSuite(parent, tree, seed);
        for (Node node : result) {
            node.setParent(parent);
            node.setHeuristic(tree.heuristic);
        }
        return result;
    }
}

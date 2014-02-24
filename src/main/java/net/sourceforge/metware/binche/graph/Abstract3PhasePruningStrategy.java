package net.sourceforge.metware.binche.graph;

import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract class which holds most of the relevant control flow for pruning strategies of
 * 3 phases (pre-loop, loop, and final phases).
 *
 * @author Pablo Moreno
 */
public abstract class Abstract3PhasePruningStrategy {

    private static final Logger LOGGER = Logger.getLogger(Abstract3PhasePruningStrategy.class);

    List<ChEBIGraphPruner> preLoopPruners;
    List<ChEBIGraphPruner> loopPruners;
    List<ChEBIGraphPruner> finalPruners;

    /**
     * Given a ChEBIGraph, this strategy is applied as follow: preloop pruners run one time each, then the loop pruners
     * are iterated over the graph until they produce no different in the graph, to end up invoking the final pruners
     * that run only one time each.
     *
     * @param graph to be pruned.
     * @return an integer with the number of nodes from the graph that could be removed.
     */
    public Integer applyStrategy(ChebiGraph graph) {
        int initial = graph.getVertexCount();
        for (ChEBIGraphPruner pruner : preLoopPruners) {
            pruner.prune(graph);
        }
        LOGGER.info("Removed pre loop : "+(initial - graph.getVertexCount()));
        int difference = 1;
        int afterPreLoop = graph.getVertexCount();
        int beforeIteration = afterPreLoop;
        while (difference>0) {
            for (ChEBIGraphPruner pruner : loopPruners) {
                pruner.prune(graph);
                LOGGER.info(pruner.getClass().getCanonicalName()+" left: "+ graph.getVertexCount());
            }
            int current = graph.getVertexCount();
            difference = beforeIteration - current;
            beforeIteration = current;
        }
        LOGGER.info("Removed in loop : "+(afterPreLoop -  beforeIteration));

        for (ChEBIGraphPruner pruner : finalPruners) {
            pruner.prune(graph);
        }

        LOGGER.info("Removed in final : "+(beforeIteration - graph.getVertexCount()));

        return initial - graph.getVertexCount();
    }
}

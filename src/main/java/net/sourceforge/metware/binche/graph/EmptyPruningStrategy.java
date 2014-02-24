package net.sourceforge.metware.binche.graph;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Container for pruning strategy with no prunning.
 *
 * @author Pablo Moreno
 */
public class EmptyPruningStrategy extends Abstract3PhasePruningStrategy implements PrunningStrategy {

    public EmptyPruningStrategy() {
        finalPruners = new LinkedList<ChEBIGraphPruner>();
        loopPruners = new LinkedList<ChEBIGraphPruner>();
        preLoopPruners = new LinkedList<ChEBIGraphPruner>();
    }

}

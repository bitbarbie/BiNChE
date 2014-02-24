package net.sourceforge.metware.binche.graph;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Pruning strategy (a set of pruning objects set as either pre loop, loop and final) designed for the
 * weighted enrichment analysis of fragments. <p>
 *
 * Loop : {@link HighPValueBranchPruner} at 0.05 and {@link LinearBranchCollapserPruner}.
 *
 *
 * @author Pablo Moreno
 */
public class FragmentEnrichPruningStrategy extends Abstract3PhasePruningStrategy implements PrunningStrategy {

    /**
     * Initializes the pruning strategy to be used for the weighted enrichment analysis (of fragments).
     */
    public FragmentEnrichPruningStrategy() {
        finalPruners = new LinkedList<ChEBIGraphPruner>();
        loopPruners = Arrays.asList(
                new HighPValueBranchPruner(0.05)
                , new LinearBranchCollapserPruner()
                //, new ZeroDegreeVertexPruner()
        );
        preLoopPruners = Arrays.asList(
                new HighPValueBranchPruner(0.05)
                , new LinearBranchCollapserPruner()
                //, new RootChildrenPruner(3,false)
        );

    }

}

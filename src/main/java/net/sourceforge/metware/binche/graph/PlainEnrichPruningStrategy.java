package net.sourceforge.metware.binche.graph;

import java.util.Arrays;
import java.util.LinkedList;

/**
 *
 * Pruning strategy (a set of pruning objects set as either pre loop, loop and final) designed for the
 * plain enrichment analysis. <p>
 *
 * Pre  : {@link HighPValueBranchPruner} at 0.05, {@link LinearBranchCollapserPruner}, and {@link RootChildrenPruner}
 * at 3 levels.<p>
 * Loop : {@link MoleculeLeavesPruner}, {@link HighPValueBranchPruner} at 0.05, {@link LinearBranchCollapserPruner},
 * and {@link ZeroDegreeVertexPruner}.<p>
 *
 * @author Pablo Moreno
 */
public class PlainEnrichPruningStrategy extends Abstract3PhasePruningStrategy implements PrunningStrategy {

    public PlainEnrichPruningStrategy() {
        preLoopPruners = Arrays.asList(new HighPValueBranchPruner(0.05),
                new LinearBranchCollapserPruner(),
                new RootChildrenPruner(3,false));

        loopPruners = Arrays.asList(new MoleculeLeavesPruner(), new HighPValueBranchPruner(0.05)
                , new LinearBranchCollapserPruner(), new ZeroDegreeVertexPruner());
        finalPruners = new LinkedList<ChEBIGraphPruner>();
    }
}

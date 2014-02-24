package net.sourceforge.metware.binche.graph;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Prunning strategy devised for the weighted enrichment analysis.
 *
 * @author Pablo Moreno
 */
public class WeightedEnrichPruningStrategy extends Abstract3PhasePruningStrategy implements PrunningStrategy {

    public WeightedEnrichPruningStrategy() {
        finalPruners = new LinkedList<ChEBIGraphPruner>();
        finalPruners.add(new LinearBranchCollapserPruner());
        finalPruners.add(new ZeroDegreeVertexPruner());
        loopPruners = new LinkedList<ChEBIGraphPruner>();
        preLoopPruners = Arrays.asList(
                new MoleculeLeavesPruner(), new RootChildrenPruner(4,false), new HighPValueBranchPruner(0.05)
        );

    }

}

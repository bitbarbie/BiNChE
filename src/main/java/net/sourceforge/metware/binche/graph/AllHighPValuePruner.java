/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sourceforge.metware.binche.graph;

import java.util.Collection;
import java.util.HashSet;

/**
 * This pruner inspects all nodes and removes those with p-value above the set threshold. This pruner produces
 * many disconnected components and is not generally useful.
 *
 * @name    AllHighPValuePruner
 * @date    2013.04.23
 * @author  Pablo Moreno, Stephan Beisken
 * @brief   Removes all nodes that have a p-value above a set threshold.
 *
 */
public class AllHighPValuePruner implements ChEBIGraphPruner {
    
    private Double threshold;

    /**
     * Initializes the pruner with the maximum allowed p-value, above which nodes are deleted when
     * pruning.
     *
     * @param threshold the maximal p-value, above which nodes are deleted.
     */
    public AllHighPValuePruner(Double threshold) {
        this.threshold = threshold;
    }

    @Override
    public void prune(ChebiGraph graph) {
        Collection<ChebiVertex> toRem = new HashSet<ChebiVertex>();
        for (ChebiVertex chebiVertex : graph.getVertices()) {
            Double pValue = chebiVertex.getCorrPValue() != null 
                    ? chebiVertex.getCorrPValue() : chebiVertex.getpValue();
            
            if(threshold<pValue) {
                toRem.add(chebiVertex);
            }
        }
        
        for (ChebiVertex chebiVertex : toRem) {
            graph.removeVertex(chebiVertex);
        }
    }


}

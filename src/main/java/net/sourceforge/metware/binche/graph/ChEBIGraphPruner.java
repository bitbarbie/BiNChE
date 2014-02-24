/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.metware.binche.graph;

/**
 * Interface that defines the facade of algorithms that process the ChebiGraph to prune it.
 *
 * @author Pablo Moreno
 * @author Stephan Beisken.
 */
public interface ChEBIGraphPruner {
    
    /**
     * Prunes the graph given, leaving the changes in the same graph.
     * 
     * @param graph the graph to be pruned.
     */
    public void prune(ChebiGraph graph);
    
}

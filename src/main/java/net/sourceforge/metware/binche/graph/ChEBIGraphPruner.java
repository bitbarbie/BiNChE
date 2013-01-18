/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.metware.binche.graph;

/**
 * Interface that defines the facade of algorithms that process the ChebiGraph to prune it.
 * @author pmoreno
 */
public interface ChEBIGraphPruner {
    
    public void prune(ChebiGraph graph);
    
}

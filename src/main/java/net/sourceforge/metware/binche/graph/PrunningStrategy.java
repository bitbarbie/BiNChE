package net.sourceforge.metware.binche.graph;

/**
 * Defines the main behaviour of a prunning strategy, which is a set of pruners applied in a particular
 * manner.
 *
 * @author Pablo Moreno
 */
public interface PrunningStrategy {

    /**
     * Applies the implemented prunning strategy to the graph provided. Returns the number of prunned nodes.
     *
     * @param graph
     * @return number of pruned nodes.
     */
    public Integer applyStrategy(ChebiGraph graph);

}

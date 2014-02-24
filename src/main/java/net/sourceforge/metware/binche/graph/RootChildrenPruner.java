/**
 * RootChildrenPruner.java
 *
 * 2012.10.25
 *
 * This file is part of the CheMet library
 *
 * The CheMet library is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * CheMet is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with CheMet. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.metware.binche.graph;

import java.util.Collection;
import org.apache.log4j.Logger;

/**
 * This pruner aims to delete the roots (the more general entities in the ontology) and its children up to a defined
 * level. Removes the defined level of children from the root class of the ontology.
 *
 * @name RootChildrenPruner
 * @date 2012.10.25
 *
 * @author Pablo Moreno
 * @author Stephan Beisken
 */
public class RootChildrenPruner implements ChEBIGraphPruner {

    private static final Logger LOGGER = Logger.getLogger(RootChildrenPruner.class);
    private int level;
    private boolean reExecution;
    private int executionCount = 0;

    /**
     * Initializes the root pruner, setting the level of entities that will be pruned starting
     * from the roots and down.
     *
     * @param level a non negative integer; 1 only prunes roots, 2 prunes the root and its direct neighbours, and so on.
     * @param allowReExecution whether the pruner can be executed multiple times on a given graph.
     */
    public RootChildrenPruner(int level, boolean allowReExecution) {
        this.level = level;
        this.reExecution = allowReExecution;
    }

    public void prune(ChebiGraph graph) {
        if (reExecution || executionCount == 0) {
            Collection<ChebiVertex> roots = graph.getRoots();
            for (ChebiVertex root : roots) {
                deleteChildren(root, graph, level);
            }
        }
        executionCount++;
    }

    private void deleteChildren(ChebiVertex vertex, ChebiGraph graph, int nextLevel) {
        if (nextLevel > 0) {
            nextLevel--;
            for (ChebiVertex child : graph.getChildren(vertex)) {
                deleteChildren(child, graph, nextLevel);
            }
            graph.removeVertex(vertex);
        }
    }
}

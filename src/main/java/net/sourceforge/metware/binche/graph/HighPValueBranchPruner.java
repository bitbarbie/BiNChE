/**
 * HighPValueBranchPruner.java
 *
 * 2012.10.21
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * This pruner looks for branches of the ontology in the enrichment analysis result graph which
 * have only high p-values, and gets rid of them. If the branch inspected has at least one node with
 * a p-value below threshold set, then the branch is kept.
 *
 * @name HighPValueBranchPruner
 * @date 2012.10.21
 * @author Pablo Moreno
 * @author Stephan Beisken
 */
public class HighPValueBranchPruner implements ChEBIGraphPruner {

    private static final Logger LOGGER = Logger.getLogger(HighPValueBranchPruner.class);
    private Double pvalueThreshold;

    /**
     * Constructor which takes as parameter the p-value threshold to be used. Anything higher than this is set for
     * rejection/removal.
     *
     * @param pvalueThreshold the maximal p-value to accept before rejecting the node.
     */
    public HighPValueBranchPruner(Double pvalueThreshold) {
        this.pvalueThreshold = pvalueThreshold;
    }

    @Override
    public void prune(ChebiGraph graph) {
        for (ChebiVertex root : graph.getRoots()) {
            //System.out.println("Root : " + root.getChebiName());
            if (root == null) {
                return;
            }

            int beforePrunSize = graph.getVertexCount();
            processNode(root, graph);
            int afterPrunSize = graph.getVertexCount();
            while (beforePrunSize > afterPrunSize) {
                beforePrunSize = afterPrunSize;
                processNode(root, graph);
                afterPrunSize = graph.getVertexCount();
            }
        }

    }

    private boolean processNode(ChebiVertex node, ChebiGraph graph) {

        boolean hasDescendentWithCompliantPValue = false;

        List<ChebiVertex> toRemVertex = new ArrayList<ChebiVertex>();
        Collection<ChebiVertex> children = graph.getChildren(node);

        for (ChebiVertex childNode : children) {
            if (!processNode(childNode, graph)) {
                toRemVertex.add(childNode);
            } else {
                hasDescendentWithCompliantPValue = true;
            }
        }

        Double nodePValue = node.getCorrPValue() != null ? node.getCorrPValue() : node.getpValue();

        if (nodePValue <= this.pvalueThreshold) {
            hasDescendentWithCompliantPValue = true;
        } else if (children.isEmpty() || children.size() == toRemVertex.size()) {
            graph.removeVertex(node);
        }

        return hasDescendentWithCompliantPValue;
    }
}

/**
 * LinearBranchCollapserPruner.java
 *
 * 2012.10.24
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
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * @name LinearBranchCollapserPruner @date 2012.10.24
 *
 * @version $Rev$ : Last Changed $Date$
 * @author pmoreno
 * @author $Author$ (this version) @brief ...class description...
 *
 */
public class LinearBranchCollapserPruner implements ChEBIGraphPruner {

    private static final Logger LOGGER = Logger.getLogger(LinearBranchCollapserPruner.class);

    public void prune(ChebiGraph graph) {
        ChebiVertex root = graph.getRoot();

        for (ChebiVertex chebiVertex : graph.getChildren(root)) {
            LinearBranch linearCandidate = new LinearBranch(root);
            processNodeBranches(chebiVertex, linearCandidate, graph);
            // If a node has more than one child, then it can only be a head or a last member, head nodes
            // of a linear branch shouldn't return their branches, but collapse them. Only inner members (and last
            // members) should return them. Consider that a node can be both last member and head of different branches.

        }
    }

    private void processNodeBranches(ChebiVertex chebiVertex, LinearBranch linearCandidate, ChebiGraph graph) {
        /**
         * We always start by adding this chebiVertex to the linear candidate, this can have 3 outcomes: 
         * A. Added to the
         * linear body (not as head or last element). In this case we just proceed to pass the linear candidate to the
         * only child this node will have. 
         * 
         * B. Added as last member. In this case we collapse the linearCandidate and
         * start a new linear candidate for each children, where the current node is set as head.
         */
        boolean result = linearCandidate.addMember(chebiVertex, graph);
        if (result) {
            ChebiVertex lastMember = linearCandidate.getLastMember();
            
            if (lastMember!=null && lastMember.equals(chebiVertex)) {
                // Case B
                linearCandidate.collapseBranch(graph);
                for (ChebiVertex child : graph.getChildren(chebiVertex)) {
                    LinearBranch branch = new LinearBranch(chebiVertex);
                    processNodeBranches(child, branch, graph);
                }
            } else {
                // Case A
                Collection<ChebiVertex> children = graph.getChildren(chebiVertex);
                if(children.size()==1) {
                    processNodeBranches(children.iterator().next(), linearCandidate, graph);
                }
            }
        }
    }

    class LinearBranch {

        private List<ChebiVertex> linearBody;
        private ChebiVertex head;
        private ChebiVertex lastMember;
        private boolean acceptNewMembers;

        /**
         * Starts the linear branch with a head vertex, we should have more than one children node.
         *
         * @param head
         */
        public LinearBranch(ChebiVertex head) {
            this.head = head;
            linearBody = new LinkedList<ChebiVertex>();
            acceptNewMembers = true;
        }

        /**
         * The size of the linear branch is the lenght of elements in the linear portion (those that have only one
         * child)
         *
         * @return number of nodes in linear portion.
         */
        public int size() {
            return linearBody.size();
        }

        public boolean addMember(ChebiVertex vertex, ChebiGraph graph) {
            if (acceptNewMembers && graph.getChildren(vertex).size() == 1) {
                linearBody.add(vertex);
                return true;
            } else if (acceptNewMembers) {
                lastMember = vertex;
                acceptNewMembers = false;
                return true;
            }
            return false;
        }

        public void collapseBranch(ChebiGraph graph) {
            if (size() > 0 && this.lastMember != null) {
                for (ChebiVertex chebiVertex : linearBody) {
                    graph.removeVertex(chebiVertex);
                }
                graph.addEdge(this.head, this.lastMember);
            }
        }

        private ChebiVertex getHead() {
            return this.head;
        }

        private ChebiVertex getLastMember() {
            return this.lastMember;
        }
    }
}

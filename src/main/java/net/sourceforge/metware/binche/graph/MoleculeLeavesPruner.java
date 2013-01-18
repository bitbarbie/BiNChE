/**
 * MoleculeLeavesPruner.java
 *
 * 2012.10.18
 *
 * This file is part of the CheMet library
 * 
 * The CheMet library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * CheMet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with CheMet.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sourceforge.metware.binche.graph;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * @name    MoleculeLeavesPruner
 * @date    2012.10.18
 * @version $Rev$ : Last Changed $Date$
 * @author  pmoreno
 * @author  $Author$ (this version)
 * @brief   Prunes leaves that are molecules (most of them should be).
 *
 * This prunner inspects the graph, descending to the leaves and removing any leave that it is a defined molecule, and
 * not a class or role. This is useful for reducing nodes in the binomial enrichment analysis.
 */
public class MoleculeLeavesPruner implements ChEBIGraphPruner {

    private static final Logger LOGGER = Logger.getLogger( MoleculeLeavesPruner.class );

    public void prune(ChebiGraph graph) {
        List<ChebiVertex> toRemove=new LinkedList<ChebiVertex>();
        
        for (ChebiVertex vertex : graph.getVertices()) {
            if(graph.isLeaf(vertex) && vertex.isMolecule()) {
                toRemove.add(vertex);
            }
        }
        
        for (ChebiVertex chebiVertex : toRemove) {
            graph.removeVertex(chebiVertex);
        }
    }


}

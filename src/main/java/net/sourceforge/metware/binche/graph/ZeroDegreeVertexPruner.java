/**
 * ZeroDegreeVertexPruner.java
 *
 * 2012.10.25
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
 * This pruner removes vertices that have zero degree (considering incoming and outgoing edges). This type
 * of vertices appear after some of the pruners are applied, leaving islands.
 *
 * @author Pablo Moreno
 * @author Stephan Beisken
 */
public class ZeroDegreeVertexPruner implements ChEBIGraphPruner {

    private static final Logger LOGGER = Logger.getLogger( ZeroDegreeVertexPruner.class );

    private List<ChebiVertex> removed;

    /**
     * Returns the vertices that were removed in the last {@link #prune(net.sourceforge.metware.binche.graph.ChebiGraph) }
     * call. This should only be called after the prune has been executed.
     * 
     * @return 
     */
    public List<ChebiVertex> getRemoved() {
        if(removed==null)
            return new ArrayList<ChebiVertex>();    
        return removed;
    }
    
    public void prune(ChebiGraph graph) {
        List<ChebiVertex> toRemove = new LinkedList<ChebiVertex>();
        for (ChebiVertex chebiVertex : graph.getVertices()) {
            if(graph.getInEdges(chebiVertex).isEmpty() && graph.getOutEdges(chebiVertex).isEmpty()) {
                toRemove.add(chebiVertex);
            }
        }
        
        for (ChebiVertex chebiVertex : toRemove) {
            graph.removeVertex(chebiVertex);
        }
        
        this.removed = toRemove;
    }


}

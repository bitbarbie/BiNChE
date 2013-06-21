/**
 * LowPValueBranchPruner.java
 *
 * 2012.10.21
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
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * @name    LowPValueBranchPruner
 * @date    2012.10.21
 * @version $Rev$ : Last Changed $Date$
 * @author  pmoreno
 * @author  $Author$ (this version)
 * @brief   Gets rid of branches that have high pvalues.
 *
 */
public class LowPValueBranchPruner implements ChEBIGraphPruner {

    private static final Logger LOGGER = Logger.getLogger( LowPValueBranchPruner.class );

    private Double pvalueThreshold;

    public LowPValueBranchPruner(Double pvalueThreshold) {
        this.pvalueThreshold = pvalueThreshold;
    }

    @Override
    public void prune(ChebiGraph graph) {
        ChebiVertex root = graph.getRoot();
//        System.out.println("Root : "+root.getChebiName());
        if(root==null)
            return;
        
        int prevSize = graph.getVertexCount();
        processNode(root,graph);
        int afterSize = graph.getVertexCount();
        while(prevSize>afterSize) {
            prevSize = afterSize;
            processNode(root, graph);
            afterSize = graph.getVertexCount();
        }
    }

    private boolean processNode(ChebiVertex node, ChebiGraph graph) {

        boolean hasDescendentWithCompliantPValue = false;

        List<ChebiVertex> toRemVertex = new ArrayList<ChebiVertex>();
        Collection<ChebiVertex> children = graph.getChildren(node);
        //System.out.println("Visiting "+node.getChebiName());
        //System.out.println("Children "+children.size()+" "+children.toString());
        //System.out.println("PValue   "+graph.getVertexPValue(node));
        
        for (ChebiVertex childNode : children) {
            if(!processNode(childNode, graph)) {
                toRemVertex.add(childNode);
            } else {
                hasDescendentWithCompliantPValue = true;
            }
        }        

        if(node.getpValue()<=this.pvalueThreshold) {
            hasDescendentWithCompliantPValue = true;
        } else if(children.isEmpty() || children.size()==toRemVertex.size()){
            graph.removeVertex(node);
        }

        return hasDescendentWithCompliantPValue;
    }


}

/*
 * Copyright (c) 2012, Stephan Beisken. All rights reserved.
 *
 * This file is part of BiNChe.
 *
 * BiNChe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BiNChe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BiNChe. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.metware.binche.graph;

import cytoscape.data.annotation.ChEBIOntologyTerm;
import cytoscape.data.annotation.Ontology;
import edu.uci.ics.jung.algorithms.layout.DAGLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.*;
import java.util.List;
import net.sourceforge.metware.binche.BiNChENode;

/**
 * Class providing access to a directed graph plus visualisation functionality required to write
 * and display the graph. The graph is tailored to deal with the ChEBI ontology.
 *
 * @author Stephan Beisken
 * @author Pablo Moreno
 */
public class ChebiGraph {

    private static final Logger LOGGER = Logger.getLogger(ChebiGraph.class);

    private Graph<ChebiVertex, ChebiEdge> graph;
    private Map<String, ChebiVertex> vertexMap;
    private Map<String, BiNChENode> tmpBiNCheNodeMap;
    private Set<String> edgeSet;
    private int vertexId = 0;

    private Double pValueThreshold = 0.05;

    /**
     * Only P-Values below the set threshold will be considered for the coloring scheme. P-Values above will have
     * the default color.
     *
     * @param pValueThreshold
     */
    public void setpValueThreshold(Double pValueThreshold) {
        this.pValueThreshold = pValueThreshold;
    }

    private ColorGradient gradient;
    private Ontology ontology;
    private Layout<ChebiVertex, ChebiEdge> layout;

    /**
     * Constructs the ChEBI graph relying on the new {@link BiNChENode} instead of the maps. Each node contains its p-value
     * and corrected p-value among others.
     * 
     * @param enrichmentNodes the list of nodes produced as a result of the execution of {@link BiNChE}
     * @param ontology        the parsed ontology
     * @param inputNodes      the set of input ChEBI ids
     */
    public ChebiGraph(List<BiNChENode> enrichmentNodes, Ontology ontology, Set<String> inputNodes) {
        this.ontology = ontology;
        this.gradient = new ColorGradient(getListOfPValues(enrichmentNodes), pValueThreshold);
        
        populateGraph(enrichmentNodes, ontology, formatNodeNames(inputNodes));
    } 

    /**
     * Creates and populates an directed sparse multigraph.
     * 
     * @param enrichmentNodes all the nodes of the enrichment analysis
     * @param ontology        the parsed ontology
     * @param nodes           the set of input ChEBI ids
     */
    private void populateGraph(List<BiNChENode> enrichmentNodes, Ontology ontology, Set<String> nodes) {

        graph = new DirectedOrderedSparseMultigraph<ChebiVertex, ChebiEdge>();
        this.tmpBiNCheNodeMap = new HashMap<String, BiNChENode>();

        vertexMap = new HashMap<String, ChebiVertex>();
        edgeSet = new HashSet<String>();
        
        addTermsToTmpMap(enrichmentNodes);
        
        //Initially iterated over the the input nodes. Doesn't work when the ontology and annotation were split into
        //two separate files, because the ontology no longer contains the chemicals.
        //Now iterates over the pValue Map.
        for (BiNChENode node : enrichmentNodes)    {
            // TODO this is risky (String2Integer), at some point we should avoid relying on the cytoscape 
            // ontology object to get rid of the numerical identifier restriction. 
            addTermWithRelationsToGraph(ontology, node);
        }
    }

    /**
     * Adds a vertex to the vertex map if not already contained and sets its color depending on the estimated p value
     * from the enrichment analysis.
     *
     * @param node the node/vertex to be added
     */
    private void addVertex(BiNChENode node) {

        if (!vertexMap.containsKey(node.getIdentifier())) {
            ChEBIOntologyTerm term = (ChEBIOntologyTerm) ontology.getTerm(Integer.parseInt(node.getIdentifier()));
            ChebiVertex v = new ChebiVertex(vertexId, node.getIdentifier(), term.getName(),term.isMolecule());
            v.setpValue(node.getPValue());
            v.setCorrPValue(node.getCorrPValue());
            v.setFoldOfEnrichment(node.getFoldOfEnrichment());
            v.setSamplePercentage(node.getSamplePercentage());
            // We only color the node if it has a pValue and the pValue is below the threshold.
            // TODO here again we get the case where we could either be working with corrected and un-corrected pvalues.
            // this needs a better solution, as it might generate problems.
            Double pVal = node.getCorrPValue()!=null ? node.getCorrPValue() : node.getPValue();
            if (pVal!=null) {
                v.setColor(gradient.getGradientColor(pVal));
            }
            vertexMap.put(node.getIdentifier(), v);

            vertexId++;
        }
    }

    /**
     * Adds an edge plus its two vertices to the graph if not already contained.
     *
     * @param previousId first partner vertex id;
     * @param currentId  second partner vertex id
     */
    private void addEdge(String previousId, String currentId) {

        ChebiEdge edge = new ChebiEdge(previousId,currentId);

        if (!edgeSet.contains(edge.getId())) {

            graph.addEdge(edge, vertexMap.get(previousId), vertexMap.get(currentId));
            edgeSet.add(edge.getId());
        }
    }

    /**
     * Layouts the graph using a Spring layout.
     *
     * TODO This should be injected rather than decided here.
     */
    private void layoutGraph() {
        layout = new SpringLayout2<ChebiVertex, ChebiEdge>(graph);
        ((SpringLayout2)layout).setForceMultiplier(0.1);
        layout.setSize(new Dimension(1920, 1100));
    }

    /**
     * Gets the visualisation viewer to display the graph.
     *
     * @param dimension size of the viewer window
     * @return the visualisation viewer
     */
    public VisualizationViewer<ChebiVertex, ChebiEdge> getVisualizationViewer(Dimension dimension) {
        layoutGraph();
        VisualizationViewer<ChebiVertex, ChebiEdge> bvs = new VisualizationViewer<ChebiVertex, ChebiEdge>(layout);
        bvs.setSize(dimension);

        setTransformer(bvs);
        setMouse(bvs);

        return bvs;
    }

    /**
     * Gets the visualisation server to write the graph.
     *
     * @return the visualisation server
     */
    public VisualizationImageServer<ChebiVertex, ChebiEdge> getVisualisationServer() {
        layoutGraph();
        VisualizationImageServer<ChebiVertex, ChebiEdge> vis =
                new VisualizationImageServer<ChebiVertex, ChebiEdge>(layout, layout.getSize());
        setTransformer(vis);

        return vis;
    }

    /**
     * Sets all vertex and edge render parameters (transformers).
     *
     * @param bvs the visualisation server
     */
    private void setTransformer(BasicVisualizationServer<ChebiVertex, ChebiEdge> bvs) {

        bvs.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<ChebiVertex>());
        bvs.getRenderContext().setVertexFillPaintTransformer(getVertexTransformer());

        bvs.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<ChebiEdge>());
        bvs.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());

        bvs.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.S);
    }

    /**
     * Method to return the color transformer of a node, working on its internal color value.
     *
     * @return the transformer
     */
    private Transformer<ChebiVertex, Paint> getVertexTransformer() {

        Transformer<ChebiVertex, Paint> vertexPaint = new Transformer<ChebiVertex, Paint>() {
            public Paint transform(ChebiVertex vertex) {
                return vertex.getColor();
            }
        };
        return vertexPaint;
    }

    /**
     * Adds default mouse functionality to the graph.
     *
     * @param bvs the visualisation viewer
     */
    private void setMouse(VisualizationViewer<ChebiVertex, ChebiEdge> bvs) {
        DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        bvs.setGraphMouse(gm);
    }

    /**
     * Retrieves all vertices of the graph, for iteration purposes.
     *
     * @return
     */
    public Iterable<ChebiVertex> getVertices() {
        return graph.getVertices();
    }

    /**
     * Checks whether the given vertex is a leaf (in-degree == 0) in the graph.
     *
     * @param vertex the node to check
     * @return true if vertex is a leaf.
     */
    public boolean isLeaf(ChebiVertex vertex) {
        Collection<ChebiEdge> inEdges = getInEdges(vertex);
        return inEdges.isEmpty();
    }

    /**
     * Removes a vertex and its connected edges.
     * @param chebiVertex
     */
    public void removeVertex(ChebiVertex chebiVertex) {
        Collection<ChebiEdge> toRemove = new LinkedList<ChebiEdge>(getInEdges(chebiVertex));
        toRemove.addAll(getOutEdges(chebiVertex));

        for (ChebiEdge chebiEdge : toRemove) {
            graph.removeEdge(chebiEdge);
        }

        graph.removeVertex(chebiVertex);
    }

    /**
     * Returns the number of nodes in the graph.
     *
     * @return the count of vertex in the graph.
     */
    public Integer getVertexCount() {
        return graph.getVertexCount();
    }

    /**
     * Returns the out edges for a {@link ChebiVertex}.
     *
     * @param chebiVertex to obtain out edges for.
     * @return a collection containing all edges that depart from chebiVertex.
     */
    public Collection<ChebiEdge> getOutEdges(ChebiVertex chebiVertex) {
        Collection<ChebiEdge> outEdges = graph.getOutEdges(chebiVertex);
        if(outEdges==null)
            return new ArrayList<ChebiEdge>();
        return outEdges;
    }

    /**
     * Returns the in edges for a {@link ChebiVertex}.
     *
     * @param chebiVertex to obtain in edges for.
     * @return a collection containing all edges that go to the chebiVertex.
     */
    public Collection<ChebiEdge> getInEdges(ChebiVertex chebiVertex) {
        Collection<ChebiEdge> inEdges = graph.getInEdges(chebiVertex);
        if(inEdges==null)
            return new ArrayList<ChebiEdge>();
        return inEdges;
    }

    public Collection<ChebiVertex> getChildren(ChebiVertex node) {
        Collection<ChebiVertex> children = graph.getPredecessors(node);
        if(children==null)
            return new ArrayList<ChebiVertex>();
        return children;
    }

    /**
     * @deprecated now the vertex pValue can be obtained directly from the node.
     * @param node
     * @return 
     */
    @Deprecated
    public Double getVertexPValue(ChebiVertex node) {
        return node.getCorrPValue()!=null ? node.getCorrPValue() : node.getpValue();
    }

    /**
     * Adds an edge to the graph where the direction goes from child (less general) to parent (more general).
     *
     * @param parent
     * @param child
     */
    public void addEdge(ChebiVertex parent, ChebiVertex child) {
        addEdge(child.getChebiId(), parent.getChebiId());
    }

    /**
     * Retrieves a collection with all the edges of the graph.
     *
     * @return a collection with all edges.
     */
    public Collection<ChebiEdge> getEdges() {
        return graph.getEdges();
    }
    /**
     * Returns the root of the graph, the ChebiVertex which only has incoming edges (the most general ChEBI node).
     *
     * @deprecated use {@link #getRoots() } instead.
     * @return root vertex or null if the graph has no root. 
     */
    @Deprecated
    public ChebiVertex getRoot() {
        for (ChebiVertex chebiVertex : graph.getVertices()) {
            if(getOutEdges(chebiVertex).isEmpty() && !getInEdges(chebiVertex).isEmpty()) {
                return chebiVertex;
            }
        }
        return null;
    }
    
    /**
     * Returns the roots of the graph, the ChebiVertexes which only have incoming edges (the most general ChEBI nodes).
     * @return 
     */
    public Collection<ChebiVertex> getRoots() {
        Collection<ChebiVertex> roots = new HashSet<ChebiVertex>();
        for (ChebiVertex chebiVertex : graph.getVertices()) {
            if(getOutEdges(chebiVertex).isEmpty() && !getInEdges(chebiVertex).isEmpty()) {
                roots.add(chebiVertex);
            }
        }
        return roots;
    }

    /**
     * Removes the CHEBI: part from CHEBI:\d+
     * @param nodes
     * @return 
     */
    private Set<String> formatNodeNames(Set<String> nodes) {
        // extract numeral ChEBI ID
        Set<String> nodesMod = new HashSet<String>();
        for (String chebiId : nodes) {
                if (chebiId.indexOf(":")>0)
                        nodesMod.add(chebiId.split(":")[1]);
                else 
                        nodesMod.add(chebiId);
        }
        return nodesMod;
    }

    /**
     * This will be part of a different class, something like a BiNCheNode list processor.
     * 
     * @param enrichmentNodes
     * @return 
     */
    private Collection<Double> getListOfPValues(List<BiNChENode> enrichmentNodes) {
        // for this application, we only need the different values.
        Set<Double> pValues = new HashSet<Double>();
        boolean usedCorr = false;
        // either we use all corrected or all non-corrected, but never a mixture!!!
        if(enrichmentNodes.size()>0 && enrichmentNodes.get(0).getCorrPValue()!=null) {
            usedCorr=true;
        }
        for (BiNChENode biNChENode : enrichmentNodes) {
            if(usedCorr) {
                pValues.add(biNChENode.getCorrPValue());
            }
            else {
                pValues.add(biNChENode.getPValue());                
            }
        }
        
        return pValues;
    }

    private void addTermWithRelationsToGraph(Ontology ontology, BiNChENode nodeToAdd) {
        BiNChENode previousNode;
        BiNChENode currentNode;
        int[][] hierarchy = ontology.getAllHierarchyPaths(Integer.parseInt(nodeToAdd.getIdentifier()));
        for (int row = 0; row < hierarchy.length; row++) {

            int previousId = hierarchy[row][hierarchy[row].length - 1];
            previousNode = tmpBiNCheNodeMap.get(previousId+"");             
            addVertex(previousNode);

            for (int col = hierarchy[row].length - 2; col >= 0; col--) {

                int currentId = hierarchy[row][col];
                currentNode = tmpBiNCheNodeMap.get(currentId+"");
                addVertex(currentNode);
                addEdge(previousNode.getIdentifier(), currentNode.getIdentifier());

                previousId = currentId;   
                previousNode = tmpBiNCheNodeMap.get(previousId+"");
            }
        }
    }

    private void addTermsToTmpMap(List<BiNChENode> enrichmentNodes) {
        for (BiNChENode biNChENode : enrichmentNodes) {
            this.tmpBiNCheNodeMap.put(biNChENode.getIdentifier(), biNChENode);
        }
    }

}

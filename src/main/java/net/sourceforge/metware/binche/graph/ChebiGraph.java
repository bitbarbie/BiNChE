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

/**
 * Class providing access to a rooted acyclic minimum spanning tree plus visualisation functionality required to write
 * and display the graph. The graph is tailored to deal with the ChEBI ontology.
 */
public class ChebiGraph {

    private static final Logger LOGGER = Logger.getLogger(ChebiGraph.class);

    private Graph<ChebiVertex, ChebiEdge> graph;
    private Map<Integer, ChebiVertex> vertexMap;
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
    private Map<Integer, Double> pValueMap;

    private Layout<ChebiVertex, ChebiEdge> layout;

    /**
     * Constructs the ChEBI graph.
     *
     * @param pValueMap the p values and their corresponding ChEBI ids from the enrichment analysis
     * @param ontology  the parsed ontology
     * @param nodes     the set of input ChEBI ids
     */
    public ChebiGraph(Map<Integer, Double> pValueMap, Ontology ontology, HashSet<String> nodes) {

        this.ontology = ontology;
        this.pValueMap = pValueMap;

        this.gradient = new ColorGradient(pValueMap.values(),pValueThreshold);

        // extract numeral ChEBI ID
        HashSet<String> nodesMod = new HashSet<String>();
        for (String chebiId : nodes) {
            nodesMod.add(chebiId.split(":")[1]);
        }
        nodes = nodesMod;

        populateGraph(pValueMap, ontology, nodes);
        //layoutGraph();
    }

    /**
     * Creates and populates an undirected sparse multigraph.
     *
     * @param pValueMap the p values and their corresponding ChEBI ids from the enrichment analysis
     * @param ontology  the parsed ontology
     * @param nodes     the set of input ChEBI ids
     */
    private void populateGraph(Map<Integer, Double> pValueMap, Ontology ontology, HashSet<String> nodes) {

        //graph = new UndirectedOrderedSparseMultigraph<ChebiVertex, ChebiEdge>();
        graph = new DirectedOrderedSparseMultigraph<ChebiVertex, ChebiEdge>();

        vertexMap = new HashMap<Integer, ChebiVertex>();
        edgeSet = new HashSet<String>();

        int previousId;
        int currentId;

//        for (String node : nodes) {

        //Initially iterated over the the input nodes. Doesn't work when the ontology and annotation were split into
        //two separate files, because the ontology no longer contains the chemicals.
        //Now iterates over the pValue Map.
        for (Integer node : pValueMap.keySet())    {

            int[][] hierarchy = ontology.getAllHierarchyPaths(node);

            for (int row = 0; row < hierarchy.length; row++) {

                previousId = hierarchy[row][hierarchy[row].length - 1];
                addVertex(previousId);

                for (int col = hierarchy[row].length - 2; col >= 0; col--) {

                    currentId = hierarchy[row][col];
                    addVertex(currentId);
                    addEdge(previousId, currentId);

                    previousId = currentId;
                }
            }
        }
    }

    /**
     * Adds a vertex to the vertex map if not already contained and sets its color depending on the estimated p value
     * from the enrichment analysis.
     *
     * @param id the id of the vertex to be added
     */
    private void addVertex(int id) {

        if (!vertexMap.containsKey(id)) {
            ChEBIOntologyTerm term = (ChEBIOntologyTerm) ontology.getTerm(id);
            ChebiVertex v = new ChebiVertex(vertexId, "" + id, term.getName(),term.isMolecule());
            v.setpValue(pValueMap.get(id));
            vertexMap.put(id, v);
            // We only color the node if it has a pValue and the pValue is below the threshold.
            if (pValueMap.containsKey(id))
                vertexMap.get(id).setColor(gradient.getGradientColor(pValueMap.get(id)));

            vertexId++;
        }
    }

    /**
     * Adds an edge plus its two vertices to the graph if not already contained.
     *
     * @param previousId first partner vertex id;
     * @param currentId  second partner vertex id
     */
    private void addEdge(int previousId, int currentId) {

        ChebiEdge edge = new ChebiEdge(previousId + "-" + currentId, 0d);

        if (!edgeSet.contains(edge.getId())) {

            graph.addEdge(edge, vertexMap.get(previousId), vertexMap.get(currentId));
            edgeSet.add(edge.getId());
        }
    }

    /**
     * Creates a rooted acyclic tree from the undirected sparse multigraph by calculating its minimum spanning trees.
     * The resulting forest should ideally contain a single tree since the called ontology hierarchies should be
     * interconnected.
     * <p/>
     * The size of the graph is determined by the values x, y in the tree layout.
     * <p/>
     * The tree is rooted to ChEBI:24431 "chemical entity" using custom implementations of the MinimumSpanningForest2
     * (here, SpanningForest) and PrimSpanningTree (here, SpanningTree) classes.
     */
    private void layoutGraph() {

        //SpanningForest<ChebiVertex, ChebiEdge> prim =
        //        new SpanningForest<ChebiVertex, ChebiEdge>(graph, new DelegateForest<ChebiVertex, ChebiEdge>(),
        //                DelegateTree.<ChebiVertex, ChebiEdge>getFactory(), new ConstantTransformer(1.0));

        //Forest<ChebiVertex, ChebiEdge> forest = prim.getForest();
        //layout = new TreeLayout<ChebiVertex, ChebiEdge>(forest, 80, 80);
        //layout = new DAGLayout<ChebiVertex, ChebiEdge>(graph);
        layout = new SpringLayout2<ChebiVertex, ChebiEdge>(graph);
        ((SpringLayout2)layout).setForceMultiplier(0.1);
        layout.setSize(new Dimension(1920, 1100));

        // re-creates the original graph with the forest node coordinates
        // Layout<ChebiVertex, ChebiEdge> treeLayout = new TreeLayout<ChebiVertex, ChebiEdge>(forest);
        // layout = new StaticLayout<ChebiVertex, ChebiEdge>(graph, treeLayout);
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

    public Integer getVertexCount() {
        return graph.getVertexCount();
    }

    public Collection<ChebiEdge> getOutEdges(ChebiVertex chebiVertex) {
        Collection<ChebiEdge> outEdges = graph.getOutEdges(chebiVertex);
        if(outEdges==null)
            return new ArrayList<ChebiEdge>();
        return outEdges;
    }

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

    public Double getVertexPValue(ChebiVertex node) {
        return pValueMap.get(Integer.valueOf(node.getChebiId()));
    }

    /**
     * Adds an edge to the graph where the direction goes from child (less general) to parent (more general).
     *
     * @param parent
     * @param child
     */
    public void addEdge(ChebiVertex parent, ChebiVertex child) {
        addEdge(Integer.valueOf(child.getChebiId()), Integer.valueOf(parent.getChebiId()));
    }

    public Collection<ChebiEdge> getEdges() {
//    	Graph<ChebiVertex, ChebiEdge> temp = layout.getGraph();
//    	return temp.getEdges();
        return graph.getEdges();
    }
    /**
     * Returns the root of the graph, the ChebiVertex which only has incoming edges (the most general ChEBI node).
     *
     * @return root vertex or null if the graph has no root. 
     */
    public ChebiVertex getRoot() {
        for (ChebiVertex chebiVertex : graph.getVertices()) {
            if(getOutEdges(chebiVertex).isEmpty() && !getInEdges(chebiVertex).isEmpty()) {
                return chebiVertex;
            }
        }
        return null;
    }

}

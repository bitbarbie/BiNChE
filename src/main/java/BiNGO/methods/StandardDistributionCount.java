package BiNGO.methods;

/* * Copyright (c) 2005 Flanders Interuniversitary Institute for Biotechnology (VIB)
 * *
 * * Authors : Steven Maere, Karel Heymans
 * *
 * * This program is free software; you can redistribute it and/or modify
 * * it under the terms of the GNU General Public License as published by
 * * the Free Software Foundation; either version 2 of the License, or
 * * (at your option) any later version.
 * *
 * * This program is distributed in the hope that it will be useful,
 * * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * * The software and documentation provided hereunder is on an "as is" basis,
 * * and the Flanders Interuniversitary Institute for Biotechnology
 * * has no obligations to provide maintenance, support,
 * * updates, enhancements or modifications.  In no event shall the
 * * Flanders Interuniversitary Institute for Biotechnology
 * * be liable to any party for direct, indirect, special,
 * * incidental or consequential damages, including lost profits, arising
 * * out of the use of this software and its documentation, even if
 * * the Flanders Interuniversitary Institute for Biotechnology
 * * has been advised of the possibility of such damage. See the
 * * GNU General Public License for more details.
 * *
 * * You should have received a copy of the GNU General Public License
 * * along with this program; if not, write to the Free Software
 * * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * *
 * * Authors: Steven Maere, Karel Heymans
 * * Date: Mar.25.2005
 * * Description: class that counts the small n, big N, small x, big X which serve as input for the statistical tests.     
 **/

import BiNGO.interfaces.DistributionCount;
import cytoscape.data.annotation.Annotation;
import cytoscape.data.annotation.Ontology;
import cytoscape.task.TaskMonitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * ************************************************************
 * DistributionCount.java   Steven Maere & Karel Heymans (c) March 2005
 * ----------------------
 * <p/>
 * class that counts the small n, big N, small x, big X which serve as input for the statistical tests.
 * *************************************************************
 */


public class StandardDistributionCount implements DistributionCount {

    /*--------------------------------------------------------------
    FIELDS.
    --------------------------------------------------------------*/

    /**
     * the annotation.
     */
    private Annotation annotation;
    /**
     * the ontology.
     */
    private Ontology ontology;

    private Map<String, Set<String>> alias;

    /**
     * the scores or weights that accompany a set of elements.
     */
    private Map<String, Double> weights;
    /**
     * HashSet of selected nodes
     */
    private Set selectedNodes;
    /**
     * HashSet of reference nodes
     */
    private Set refNodes;
    /**
     * hashmap with values of small n ; keys GO labels.
     */
    private Map mapSmallN;
    /**
     * hashmap with values of small x ; keys GO labels.
     */
    private Map mapSmallX;
    /**
     * hashmap with values of big N.
     */
    private Map mapBigN;
    /**
     * hashmap with values of big X.
     */
    private Map mapBigX;

    /**
     *
     */
    private Map<Integer, Double> mapWeightSums;

    /**
     * @return the weights
     */
    public Map<String, Double> getWeights() {

        return weights;
    }

    /**
     * @param aWeights the weights to set
     */
    public void setWeights(Map<String, Double> aWeights) {

        weights = aWeights;
    }

    // Keep track of progress for monitoring:
    private int maxValue;
    private boolean interrupted = false;

    /*--------------------------------------------------------------
    CONSTRUCTOR.
    --------------------------------------------------------------*/

    public StandardDistributionCount(Annotation annotation, Ontology ontology, Set selectedNodes, Set refNodes,
                                     Map alias) {

        this.annotation = annotation;
        this.ontology = ontology;
        this.alias = alias;
        annotation.setOntology(ontology);

        this.selectedNodes = selectedNodes;
        this.refNodes = refNodes;


        //System.out.println(selectedNodes);


        this.weights = new HashMap<String, Double>();

    }

    public StandardDistributionCount(Annotation annotation, Ontology ontology, Set selectedNodes, Set refNodes,
                                     Map alias, Map<String, Double> weights) {

        this(annotation, ontology, selectedNodes, refNodes, alias);

        /* Set<Integer> keys =weights.keySet();
        
        this.weights= new HashMap<Integer, Double>();
        for (Integer key : keys) {
            
            String name[]=new String[key.split(":").length];
            name = key.split(":");
            
            this.weights.put(Integer.parseInt(name[1]), weights.get(key));
            
        }*/

        this.weights = weights;
        //System.out.println(weights);
        //System.out.println(alias.keySet());

    }
    /*--------------------------------------------------------------
      METHODS.
    --------------------------------------------------------------*/

    /**
     * method for compiling GO classifications for given node
     */


    @Override
    public Set<String> getNodeClassifications(String node) {

        // HashSet for the classifications of a particular node
        Set<String> classifications = new HashSet();
        Set<String> identifiers = alias.get(node + "");

        if (identifiers != null) {
            Iterator it = identifiers.iterator();
            while (it.hasNext()) {
                int[] goID = annotation.getClassifications(it.next() + "");
                for (int t = 0; t < goID.length; t++) {
                    classifications.add(goID[t] + "");
//			omitted : all parent classes of GO class that node is assigned to are also explicitly included in classifications from the start
//			up(goID[t], classifications) ;	
                }
            }
        }

        return classifications;


    }

    /**
     * method for recursing through tree to root
     */

/*  public void up (int goID, HashSet classifications){	
	    OntologyTerm child  = ontology.getTerm(goID);	
		  int [] parents =  child.getParentsAndContainers ();	
			for(int t = 0; t < parents.length; t++){
				classifications.add(parents[t] + "");
				up(parents[t],classifications);
			}	
	}
*/

    /**
     * method for making the hashmap for small n.
     */
    @Override
    public void countSmallN() {

        mapSmallN = this.count(refNodes);
        //System.out.println(mapSmallN.size());
    }


    /**
     * method for making the hashmap for the small x.
     */
    @Override
    public void countSmallX() {

        mapSmallX = this.count(selectedNodes);
        //System.out.println(mapSmallX);
    }


    /**
     * method that counts for small n and small x.
     */
    public Map count(Set nodes) {

        Map map = new HashMap();

        Iterator i = nodes.iterator();
        while (i.hasNext()) {  // foreach node in nodes
            Set classifications = getNodeClassifications(i.next().toString()); // S:get ALL classes the node is assigned to (also transitive!)
            Iterator iterator = classifications.iterator();
            Integer id;

            // puts the classification counts in a map
            // each node is stored with the number of leafs assigned under it
            while (iterator.hasNext()) {
                id = new Integer(iterator.next().toString());
                if (map.containsKey(id)) {
                    map.put(id, new Integer(new Integer(map.get(id).toString()).intValue() + 1));
                } else {
                    map.put(id, new Integer(1));
                }
            }

        }

        return map;
    }

    /**
     * counts big N. unclassified nodes are not counted ; no correction for function_unknown nodes (yet)(requires user input)
     */
    @Override
    public void countBigN() {

        mapBigN = new HashMap();
        int bigN = refNodes.size();
        Iterator i = refNodes.iterator();

        // S: remove unclassified nodes
        while (i.hasNext()) {
            Set<String> classifications = getNodeClassifications(i.next().toString());
            Iterator iterator = classifications.iterator();
            if (!iterator.hasNext()) {
                bigN--;
            }
        }
        for (Object id : this.mapSmallX.keySet()) {
            mapBigN.put((Integer) id, new Integer(bigN));
        }
        //System.out.println(mapBigN);
    }

    /**
     * counts big X. unclassified nodes are not counted ; no correction for function_unknown nodes (yet)(requires user input)
     */
    public void countBigX() {

        mapBigX = new HashMap();
        int bigX = selectedNodes.size();
        Iterator i = selectedNodes.iterator();
        while (i.hasNext()) {
            Set<String> classifications = getNodeClassifications(i.next().toString());
            Iterator iterator = classifications.iterator();
            if (!iterator.hasNext()) {
                bigX--;
            }
        }
        for (Object id : this.mapSmallX.keySet()) {
            mapBigX.put((Integer) id, new Integer(bigX));
        }
    }

    public void countWeight() {

        mapWeightSums = new HashMap<Integer, Double>();
        Iterator nodeIterator = selectedNodes.iterator();

        while (nodeIterator.hasNext()) {

            String node = nodeIterator.next().toString();
            Set<String> classifications = getNodeClassifications(node);
//            System.out.println(node);
            for (String nodeClass : classifications) {
                
                // walk only the parents for each node!
                if(Integer.parseInt(nodeClass) != Integer.parseInt(node.substring(6, node.length()))){ 
              
                               
                    if (!mapWeightSums.containsKey(Integer.parseInt(nodeClass))) {
                        //System.out.println(Integer.parseInt(nodeClass)+" : "+ Integer.parseInt(node.substring(6, node.length())));
                        //if(Integer.parseInt(nodeClass) == 6888){ System.out.println("PUT 6888 during classification of "+node);}

                            mapWeightSums.put(Integer.parseInt(nodeClass), weights.get(node));

                    } else {
                        Double current = mapWeightSums.get(Integer.parseInt(nodeClass));
                        mapWeightSums.put(Integer.parseInt(nodeClass), current + weights.get(node));
                    }
                }

            }
        }


        /*mapWeightSums = new HashMap<Integer,ArrayList<Double>>();
                      
        Iterator nodeIterator = selectedNodes.iterator();
      
        while(nodeIterator.hasNext()) {
                        
            String node = nodeIterator.next().toString();
            
            HashSet<String> classifications = getNodeClassifications(node);
             
            
            for (String nodeClass : classifications) {
                
               if(weights.get(node)!= 0.0){                                         //Knoten mit Gewichten = 0.0 sollen nicht in der map auftauchen
                if(!mapWeightSums.containsKey(Integer.parseInt(nodeClass))) {       //wenn es den Knoten noch nicht gibt,
                                                                                    //
                    ArrayList<Double> helpArray = new ArrayList<Double>();          //lege hilfsarray neu an                                                                      
                    helpArray.add(weights.get(node));                               //und packe gewicht des aktuellen knotens ins Array
                                                                                    //
                    mapWeightSums.put(Integer.parseInt(nodeClass), helpArray);      // hashMap bekommt eintrag mit ID(=key) und (einelementiges) hilfsarray(=arrayList)
                } 
                else {                                                              //wenn es die ID (den Knoten) schon in der map gibt
                    if(!mapWeightSums.get(Integer.parseInt(nodeClass)).contains(weights.get(node))){ // und das gewicht des aktuellen selectedNode noch nicht mit der ID assoziiert ist
                                                                                                     //
                        mapWeightSums.get(Integer.parseInt(nodeClass)).add(weights.get(node));       //dann füge zum array der aktuellen ID das gewicht hinzu                  
                    }
                     
                }
                
                Collections.sort(mapWeightSums.get(Integer.parseInt(nodeClass)));     //sortiert array für später besser num. stabilität der berechnung von rho(t) und rho'(t)
            }}            
        }*/
        //System.out.println(mapWeightSums);
        //System.out.println(mapWeightSums.size());
    }

    /*--------------------------------------------------------------
      GETTERS.
    --------------------------------------------------------------*/

    @Override
    public Map getTestMap() {

        return mapSmallX;
    }

    /**
     * returns small n hashmap.
     *
     * @return hashmap mapSmallN
     */
    @Override
    public Map getMapSmallN() {

        return mapSmallN;
    }

    /**
     * returns small x hashmap.
     *
     * @return hashmap mapSmallX
     */
    public Map getMapSmallX() {

        return mapSmallX;
    }

    public Map getMapBigN() {

        return mapBigN;
    }

    public Map getMapBigX() {

        return mapBigX;
    }

    public void calculate() {

        countSmallX();
        countSmallN();
        countBigX();
        countBigN();

        if (weights.size() > 0) countWeight();
        System.out.println("root weight = "+mapWeightSums.get(24431));
    }

    /**
     * Run the Task.
     */
    public void run() {

        calculate();
    }

    /**
     * Non-blocking call to interrupt the task.
     */
    @Override
    public void halt() {
        this.interrupted = true;
    }

    @Override
    public String getTitle() {
        return "Counting genes in GO categories...";
    }

    @Override
    public Map<Integer, Double> getMapWeights() {
        return mapWeightSums;
    }

    @Override
    public void setTaskMonitor(TaskMonitor tm) throws IllegalThreadStateException {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

}

/**
 * BiNChENode.java
 *
 * 2013.02.07
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

package net.sourceforge.metware.binche;


import org.apache.log4j.Logger;

/**
 * This class is used to represent each of the nodes produced by the enrichment analysis, containing p-value and other
 * indicators of the level of enrichment on the node.
 *
 * @name    BiNChENode
 * @date    2013.02.07
 * @author  Pablo Moreno
 */
public class BiNChENode {

    private static final Logger LOGGER = Logger.getLogger( BiNChENode.class );

    private Double pValue;
    private Double foldOfEnrichment;
    private Double samplePercentage;

    private Double corrPValue;

    /**
     * Get the value of corrected P-Value (after multiple hypothesis correction). This value can be null if no
     * correction was used.
     *
     * @return the value of corrPValue
     */
    public Double getCorrPValue() {
        return corrPValue;
    }


    private String identifier;

    /**
     * Constructor for a ChEBI Node, which is the main object in the graph produced as result.
     *
     * @param pValue The p-value for the enrichment on this node, before any FDR correction.
     * @param corrPValue The p-value of the enrichment on this node, after FDR correction.
     * @param bigX The number of total entities on the given sample.
     * @param bigN The number of total entities on the complete population.
     * @param smallX The number of entities of the sample that fall in (or are children of) this node of the ontology.
     * @param smallN The number of entities of the complete population that fall in (or are children of) this node of
     *               the ontology.
     * @param identifier The identitifier for this node of the ontology.
     */
    public BiNChENode(Double pValue, Double corrPValue, Integer bigX, Integer bigN, Integer smallX, Integer smallN, String identifier) {
        this.pValue = pValue;
        this.corrPValue = corrPValue;
        this.foldOfEnrichment = (smallX*1.0/bigX) / (smallN*1.0/bigN);
        this.samplePercentage = smallX*1.0/bigX;
        this.identifier = identifier;
    }
    
    /**
     * Get the value of samplePercentage
     *
     * @return the value of samplePercentage
     */
    public Double getSamplePercentage() {
        return samplePercentage;
    }
    

    /**
     * Get the value of foldOfEnrichment
     *
     * @return the value of foldOfEnrichment
     */
    public Double getFoldOfEnrichment() {
        return foldOfEnrichment;
    }


    /**
     * Get the value of pValue
     *
     * @return the value of pValue
     */
    public Double getPValue() {
        return pValue;
    }

    /**
     * Set the value of pValue
     *
     * @param pValue new value of pValue
     */
    public void setPValue(Double pValue) {
        this.pValue = pValue;
    }

    /**
     * Gets the ChEBI Identifier for this node.
     *
     * @return ChEBI Identifier as an String.
     */
    public String getIdentifier() {
        return identifier;
    }


}
